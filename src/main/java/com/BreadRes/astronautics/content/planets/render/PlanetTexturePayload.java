package com.BreadRes.astronautics.content.planets.render;

import com.BreadRes.astronautics.Astronautics;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PlanetTexturePayload(String planetId, byte[] data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PlanetTexturePayload> TYPE = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath(Astronautics.MOD_ID, "planet_texture")
    );

    public static final StreamCodec<ByteBuf, PlanetTexturePayload> CODEC = StreamCodec.of(
        (buf, payload) -> {
            int idLen = payload.planetId().length();
            buf.writeInt(idLen);
            buf.writeBytes(payload.planetId().getBytes());
            buf.writeBytes(payload.data());
        },
        buf -> {
            int idLen = buf.readInt();
            byte[] idBytes = new byte[idLen];
            buf.readBytes(idBytes);
            String planetId = new String(idBytes);
            byte[] data = new byte[PlanetTextureGenerator.SIZE * PlanetTextureGenerator.SIZE];
            buf.readBytes(data);
            return new PlanetTexturePayload(planetId, data);
        }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}