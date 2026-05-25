package com.BreadRes.astronautics.network;

import com.BreadRes.astronautics.Astronautics;
import com.BreadRes.astronautics.IP.ClientTeleportationManager;
import com.BreadRes.astronautics.IP.ClientWorldLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AstroTeleportPayload(
        ResourceKey<Level> dimension,
        double x, double y, double z
) implements CustomPacketPayload {

    public static final Type<AstroTeleportPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Astronautics.MOD_ID, "teleport")
    );

    public static final StreamCodec<FriendlyByteBuf, AstroTeleportPayload> CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeResourceLocation(p.dimension().location());
                        buf.writeDouble(p.x());
                        buf.writeDouble(p.y());
                        buf.writeDouble(p.z());
                    },
                    buf -> new AstroTeleportPayload(
                            ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation()),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AstroTeleportPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;

            ClientWorldLoader.initializeIfNeeded();

            ResourceKey<Level> toDim = payload.dimension();
            Vec3 dest = new Vec3(payload.x(), payload.y(), payload.z());

            if (mc.level.dimension().equals(toDim)) {
                mc.player.setPos(dest.x, dest.y, dest.z);
                mc.player.xo = dest.x;
                mc.player.yo = dest.y;
                mc.player.zo = dest.z;
                mc.player.xOld = dest.x;
                mc.player.yOld = dest.y;
                mc.player.zOld = dest.z;
                return;
            }

            ClientLevel toWorld = ClientWorldLoader.getOptionalWorld(toDim);
            if (toWorld == null) return;

            ClientTeleportationManager.changePlayerDimension(
                    mc.player,
                    mc.level,
                    toWorld,
                    dest.add(0, mc.player.getEyeHeight(), 0)
            );
        });
    }
}