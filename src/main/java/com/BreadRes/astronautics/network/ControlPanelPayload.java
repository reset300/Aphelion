package com.BreadRes.astronautics.network;

import com.BreadRes.astronautics.Astronautics;
import com.BreadRes.astronautics.content.blocks.control_panel.ControlPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ControlPanelPayload(
    BlockPos panelPos,
    BlockPos enginePos,
    Action action,
    boolean boolValue,
    int intValue,
    String strValue
) implements CustomPacketPayload {

    public enum Action { SET_ACTIVE, SET_THROTTLE, SET_NAME }

    public static final Type<ControlPanelPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(Astronautics.MOD_ID, "control_panel")
    );

    public static final StreamCodec<FriendlyByteBuf, ControlPanelPayload> STREAM_CODEC =
        StreamCodec.of(ControlPanelPayload::encode, ControlPanelPayload::decode);

    public static void encode(FriendlyByteBuf buf, ControlPanelPayload p) {
        buf.writeBlockPos(p.panelPos());
        buf.writeBlockPos(p.enginePos());
        buf.writeEnum(p.action());
        buf.writeBoolean(p.boolValue());
        buf.writeInt(p.intValue());
        buf.writeUtf(p.strValue());
    }

    public static ControlPanelPayload decode(FriendlyByteBuf buf) {
        return new ControlPanelPayload(
            buf.readBlockPos(),
            buf.readBlockPos(),
            buf.readEnum(Action.class),
            buf.readBoolean(),
            buf.readInt(),
            buf.readUtf()
        );
    }

    public static void sendSetActive(BlockPos enginePos, BlockPos panelPos, boolean active) {
        PacketDistributor.sendToServer(new ControlPanelPayload(
            panelPos, enginePos, Action.SET_ACTIVE, active, 0, ""
        ));
    }

    public static void sendSetThrottle(BlockPos enginePos, BlockPos panelPos, int throttle) {
        PacketDistributor.sendToServer(new ControlPanelPayload(
            panelPos, enginePos, Action.SET_THROTTLE, false, throttle, ""
        ));
    }

    public static void sendSetName(BlockPos enginePos, BlockPos panelPos, String name) {
        PacketDistributor.sendToServer(new ControlPanelPayload(
            panelPos, enginePos, Action.SET_NAME, false, 0, name
        ));
    }

    public static void handle(ControlPanelPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            if (player == null) return;

            var level = player.level();

            var sub = dev.ryanhcode.sable.Sable.HELPER.getContaining(level, payload.panelPos());
            if (!(sub instanceof dev.ryanhcode.sable.sublevel.ServerSubLevel serverSub)) return;

            var be = serverSub.getLevel().getBlockEntity(payload.panelPos());
            if (!(be instanceof ControlPanelBlockEntity panel)) return;

            switch (payload.action()) {
                case SET_ACTIVE -> panel.setEngineActive(payload.enginePos(), payload.boolValue());
                case SET_THROTTLE -> panel.setEngineThrottle(payload.enginePos(), payload.intValue());
                case SET_NAME -> panel.setEngineName(payload.enginePos(), payload.strValue());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}