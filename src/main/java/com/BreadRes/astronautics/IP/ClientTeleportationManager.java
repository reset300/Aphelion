package com.BreadRes.astronautics.IP;

import com.BreadRes.astronautics.ducks.IEAbstractClientPlayer;
import com.BreadRes.astronautics.ducks.IEClientPlayNetworkHandler;
import com.BreadRes.astronautics.ducks.IEEntity;
import com.BreadRes.astronautics.ducks.IEMinecraftClient;
import com.BreadRes.astronautics.ducks.IEParticleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientTeleportationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientTeleportationManager.class);
    private static final Minecraft CLIENT = Minecraft.getInstance();

    public static void forceTeleportPlayer(ResourceKey<Level> toDimension, Vec3 destination) {
        LOGGER.info("[Astronautics] Client force teleport to {} {}", toDimension.location(), destination);

        ClientLevel fromWorld = CLIENT.level;
        if (fromWorld == null) return;

        LocalPlayer player = CLIENT.player;
        if (player == null) return;

        if (fromWorld.dimension() != toDimension) {
            ClientLevel toWorld = ClientWorldLoader.getWorld(toDimension);
            Vec3 eyeOffset = new Vec3(0, player.getEyeHeight(), 0);
            changePlayerDimension(player, fromWorld, toWorld, destination.add(eyeOffset));
        }

        player.setPos(destination.x, destination.y, destination.z);
        player.xo = destination.x;
        player.yo = destination.y;
        player.zo = destination.z;
        player.xOld = destination.x;
        player.yOld = destination.y;
        player.zOld = destination.z;
    }

    public static void changePlayerDimension(
            LocalPlayer player,
            ClientLevel fromWorld,
            ClientLevel toWorld,
            Vec3 newEyePos
    ) {
        ResourceKey<Level> fromDim = fromWorld.dimension();
        ResourceKey<Level> toDim = toWorld.dimension();

        Entity vehicle = player.getVehicle();
        player.unRide();

        ((IEClientPlayNetworkHandler) CLIENT.getConnection()).astro_setLevel(toWorld);

        fromWorld.removeEntity(player.getId(), Entity.RemovalReason.CHANGED_DIMENSION);

        ((IEEntity) player).astro_setLevel(toWorld);

        Vec3 footPos = newEyePos.subtract(0, player.getEyeHeight(), 0);
        player.setPos(footPos.x, footPos.y, footPos.z);
        player.xo = footPos.x;
        player.yo = footPos.y;
        player.zo = footPos.z;
        player.xOld = footPos.x;
        player.yOld = footPos.y;
        player.zOld = footPos.z;

        ((IEEntity) player).astro_unsetRemoved();

        toWorld.addEntity(player);

        LevelRenderer newRenderer = ClientWorldLoader.getWorldRenderer(toDim);

        CLIENT.level = toWorld;
        ((IEMinecraftClient) CLIENT).astro_setWorldRenderer(newRenderer);

        if (CLIENT.particleEngine != null) {
            ((IEParticleManager) CLIENT.particleEngine).astro_setLevel(toWorld);
        }

        CLIENT.getBlockEntityRenderDispatcher().setLevel(toWorld);

        newRenderer.allChanged();

        if (vehicle != null) {
            moveEntityAcrossDimension(vehicle, toWorld, player.position());
            player.startRiding(vehicle, true);
        }

        LOGGER.info("[Astronautics] Client dimension change: {} -> {}", fromDim.location(), toDim.location());
    }

    public static void moveEntityAcrossDimension(
            Entity entity,
            ClientLevel toWorld,
            Vec3 newPos
    ) {
        ClientLevel oldWorld = (ClientLevel) entity.level();
        oldWorld.removeEntity(entity.getId(), Entity.RemovalReason.CHANGED_DIMENSION);
        ((IEEntity) entity).astro_setLevel(toWorld);
        entity.setPos(newPos.x, newPos.y, newPos.z);
        ((IEEntity) entity).astro_unsetRemoved();
        toWorld.addEntity(entity);
    }
}