package com.BreadRes.astronautics.IP;

import com.BreadRes.astronautics.ducks.IEClientPlayNetworkHandler;
import com.BreadRes.astronautics.ducks.IEMinecraftClient;
import com.BreadRes.astronautics.ducks.IEParticleManager;
import com.BreadRes.astronautics.mixin.accessor.BiomeManagerAccessor;
import com.BreadRes.astronautics.mixin.accessor.IsFlatAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ClientWorldLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientWorldLoader.class);
    private static final Minecraft CLIENT = Minecraft.getInstance();

    private static final Map<ResourceKey<Level>, ClientLevel> CLIENT_WORLD_MAP =
            new Object2ObjectOpenHashMap<>();
    public static final Map<ResourceKey<Level>, LevelRenderer> WORLD_RENDERER_MAP =
            new Object2ObjectOpenHashMap<>();

    private static boolean isInitialized = false;
    private static boolean isCreatingClientWorld = false;
    private static boolean isWorldSwitched = false;

    public static boolean getIsInitialized() {
        return isInitialized;
    }

    public static boolean getIsCreatingClientWorld() {
        return isCreatingClientWorld;
    }

    public static boolean getIsWorldSwitched() {
        return isWorldSwitched;
    }

    public static void initializeIfNeeded() {
        if (isInitialized) return;
        if (CLIENT.level == null) return;
        if (CLIENT.levelRenderer == null) return;
        if (CLIENT.player == null) return;
        if (CLIENT.player.level() != CLIENT.level) return;

        ResourceKey<Level> playerDimension = CLIENT.level.dimension();
        CLIENT_WORLD_MAP.put(playerDimension, CLIENT.level);
        WORLD_RENDERER_MAP.put(playerDimension, CLIENT.levelRenderer);

        isInitialized = true;
    }

    @NotNull
    public static ClientLevel getWorld(ResourceKey<Level> dimension) {
        Validate.notNull(dimension);
        Validate.isTrue(CLIENT.isSameThread());

        initializeIfNeeded();

        if (!CLIENT_WORLD_MAP.containsKey(dimension)) {
            return createSecondaryClientWorld(dimension);
        }

        return CLIENT_WORLD_MAP.get(dimension);
    }

    @Nullable
    public static ClientLevel getOptionalWorld(ResourceKey<Level> dimension) {
        Validate.notNull(dimension);
        Validate.isTrue(CLIENT.isSameThread(), "not on client thread");

        if (getServerDimensions().contains(dimension)) {
            return getWorld(dimension);
        }

        return null;
    }

    @NotNull
    public static LevelRenderer getWorldRenderer(ResourceKey<Level> dimension) {
        initializeIfNeeded();

        LevelRenderer result = WORLD_RENDERER_MAP.get(dimension);
        if (result == null) {
            getWorld(dimension);
            result = WORLD_RENDERER_MAP.get(dimension);
        }
        if (result == null) {
            throw new RuntimeException("Unable to get LevelRenderer of " + dimension.location());
        }
        return result;
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientWorldLoader.onDisconnect();
    }

    private static ClientLevel createSecondaryClientWorld(ResourceKey<Level> dimension) {
        Validate.notNull(CLIENT.player, "player is null");
        Validate.isTrue(CLIENT.isSameThread(), "not on client thread");

        Set<ResourceKey<Level>> dimIds = getServerDimensions();
        if (!dimIds.contains(dimension)) {
            throw new RuntimeException("Cannot create invalid client dimension " + dimension.location());
        }

        isCreatingClientWorld = true;
        CLIENT.getProfiler().push("astro_create_world");

        LevelRenderer worldRenderer = new LevelRenderer(
                CLIENT,
                CLIENT.getEntityRenderDispatcher(),
                CLIENT.getBlockEntityRenderDispatcher(),
                CLIENT.renderBuffers()
        );

        ClientLevel newWorld;
        try {
            ClientPacketListener conn = CLIENT.player.connection;

            Holder<DimensionType> dimTypeHolder = resolveDimensionTypeHolder(dimension, conn);
            if (dimTypeHolder == null) {
                throw new IllegalStateException("Cannot find DimensionType for " + dimension.location());
            }

            LOGGER.info("[Astronautics] Creating world {} with dim type effects: {}",
                    dimension.location(),
                    dimTypeHolder.value().effectsLocation());

            ClientLevel.ClientLevelData currentData =
                    (ClientLevel.ClientLevelData) CLIENT.level.getLevelData();

            ClientLevel.ClientLevelData newData = new ClientLevel.ClientLevelData(
                    currentData.getDifficulty(),
                    currentData.isHardcore(),
                    ((IsFlatAccessor) CLIENT.level.getLevelData()).isFlat()
            );

            newWorld = new ClientLevel(
                    conn,
                    newData,
                    dimension,
                    dimTypeHolder,
                    3,
                    CLIENT.level.getServerSimulationDistance(),
                    CLIENT::getProfiler,
                    worldRenderer,
                    CLIENT.level.isDebug(),
                    ((BiomeManagerAccessor) CLIENT.level.getBiomeManager()).getBiomeZoomSeed()
            );

            worldRenderer.setLevel(newWorld);
            worldRenderer.onResourceManagerReload(CLIENT.getResourceManager());

            CLIENT_WORLD_MAP.put(dimension, newWorld);
            WORLD_RENDERER_MAP.put(dimension, worldRenderer);

            LOGGER.info("[Astronautics] Client world created: {}", dimension.location());
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Creating client world " + dimension.location() + " " + CLIENT_WORLD_MAP.keySet(), e
            );
        } finally {
            isCreatingClientWorld = false;
            CLIENT.getProfiler().pop();
        }

        return newWorld;
    }

    @Nullable
    private static Holder<DimensionType> resolveDimensionTypeHolder(
            ResourceKey<Level> dimension,
            ClientPacketListener conn
    ) {
        RegistryAccess registryAccess = conn.registryAccess();
        var dimTypeRegistry = registryAccess.registryOrThrow(Registries.DIMENSION_TYPE);

        try {
            var levelStemRegistry = registryAccess.registryOrThrow(Registries.LEVEL_STEM);
            var stemKey = ResourceKey.create(Registries.LEVEL_STEM, dimension.location());
            var stem = levelStemRegistry.get(stemKey);
            if (stem != null) {
                var typeKey = dimTypeRegistry.getResourceKey(stem.type().value()).orElse(null);
                if (typeKey != null) {
                    var holder = dimTypeRegistry.getHolder(typeKey).orElse(null);
                    if (holder != null) {
                        LOGGER.info("[Astronautics] Resolved dim type for {} via LEVEL_STEM: {}",
                                dimension.location(), typeKey.location());
                        return holder;
                    }
                }
                if (stem.type() instanceof Holder.Reference<DimensionType> ref) {
                    LOGGER.info("[Astronautics] Resolved dim type for {} via stem.type() reference",
                            dimension.location());
                    return ref;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("[Astronautics] LEVEL_STEM resolution failed for {}: {}", dimension.location(), e.getMessage());
        }
        try {
            ResourceLocation dimTypeLoc = dimension.location();
            var dimTypeKey = ResourceKey.create(Registries.DIMENSION_TYPE, dimTypeLoc);
            var holder = dimTypeRegistry.getHolder(dimTypeKey).orElse(null);
            if (holder != null) {
                LOGGER.info("[Astronautics] Resolved dim type for {} via direct key lookup", dimension.location());
                return holder;
            }
        } catch (Exception e) {
            LOGGER.warn("[Astronautics] Direct key lookup failed for {}: {}", dimension.location(), e.getMessage());
        }

        if (dimension.location().getNamespace().equals("astronautics")) {
            try {
                var spaceTypeKey = ResourceKey.create(Registries.DIMENSION_TYPE,
                        ResourceLocation.fromNamespaceAndPath("astronautics", "space"));
                var holder = dimTypeRegistry.getHolder(spaceTypeKey).orElse(null);
                if (holder != null) {
                    LOGGER.info("[Astronautics] Using space dim type fallback for {}", dimension.location());
                    return holder;
                }
            } catch (Exception e) {
                LOGGER.warn("[Astronautics] Space dim type fallback failed: {}", e.getMessage());
            }
        }

        try {
            ResourceLocation spaceEffects = ResourceLocation.fromNamespaceAndPath("astronautics", "space");
            for (var entry : dimTypeRegistry.entrySet()) {
                if (spaceEffects.equals(entry.getValue().effectsLocation())) {
                    var holder = dimTypeRegistry.getHolder(entry.getKey()).orElse(null);
                    if (holder != null) {
                        LOGGER.info("[Astronautics] Found dim type with space effects for {}: {}",
                                dimension.location(), entry.getKey().location());
                        return holder;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("[Astronautics] Effects-based dim type search failed: {}", e.getMessage());
        }

        LOGGER.error("[Astronautics] Could not resolve dim type for {} — falling back to overworld (skybox WILL be wrong)",
                dimension.location());
        return CLIENT.level.dimensionTypeRegistration();
    }

    public static <T> T withSwitchedWorld(ClientLevel newWorld, Supplier<T> supplier) {
        Validate.isTrue(CLIENT.isSameThread(), "not on client thread");
        Validate.isTrue(CLIENT.player != null, "player is null");

        ClientPacketListener conn = CLIENT.getConnection();
        assert conn != null;

        ClientLevel originalWorld = CLIENT.level;
        LevelRenderer originalRenderer = CLIENT.levelRenderer;
        ClientLevel originalConnWorld = conn.getLevel();
        boolean originalIsWorldSwitched = isWorldSwitched;

        LevelRenderer newRenderer = getWorldRenderer(newWorld.dimension());

        CLIENT.level = newWorld;
        ((IEMinecraftClient) CLIENT).astro_setWorldRenderer(newRenderer);
        ((IEClientPlayNetworkHandler) conn).astro_setLevel(newWorld);
        ((IEParticleManager) CLIENT.particleEngine).astro_setLevel(newWorld);
        CLIENT.getBlockEntityRenderDispatcher().setLevel(newWorld);
        isWorldSwitched = true;

        try {
            return supplier.get();
        } finally {
            if (CLIENT.level != newWorld) {
                LOGGER.error("[Astronautics] Respawn packet redirected during withSwitchedWorld");
                originalWorld = CLIENT.level;
                originalRenderer = CLIENT.levelRenderer;
            }

            CLIENT.level = originalWorld;
            ((IEMinecraftClient) CLIENT).astro_setWorldRenderer(originalRenderer);
            ((IEClientPlayNetworkHandler) conn).astro_setLevel(originalConnWorld);
            ((IEParticleManager) CLIENT.particleEngine).astro_setLevel(originalWorld);
            CLIENT.getBlockEntityRenderDispatcher().setLevel(originalWorld);
            isWorldSwitched = originalIsWorldSwitched;
        }
    }

    public static void withSwitchedWorld(ClientLevel newWorld, Runnable runnable) {
        withSwitchedWorld(newWorld, () -> {
            runnable.run();
            return null;
        });
    }

    public static void withSwitchedWorldFailSoft(ResourceKey<Level> dim, Runnable runnable) {
        ClientLevel world = getOptionalWorld(dim);
        if (world == null) {
            LOGGER.error("[Astronautics] Ignoring task for invalid dimension {}", dim.location());
            return;
        }
        withSwitchedWorld(world, runnable);
    }

    public static Set<ResourceKey<Level>> getServerDimensions() {
        assert CLIENT.player != null;
        return CLIENT.player.connection.levels();
    }

    public static Collection<ClientLevel> getClientWorlds() {
        return CLIENT_WORLD_MAP.values();
    }

    public static void cleanUp() {
        WORLD_RENDERER_MAP.forEach((dim, renderer) -> {
            renderer.setLevel(null);
            if (renderer != CLIENT.levelRenderer) {
                renderer.close();
            }
        });
        CLIENT_WORLD_MAP.clear();
        WORLD_RENDERER_MAP.clear();
        isInitialized = false;
    }

    public static void onDisconnect() {
        cleanUp();
    }
}