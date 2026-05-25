package com.BreadRes.astronautics.network;

import com.BreadRes.astronautics.Astronautics;
import com.BreadRes.astronautics.content.planets.render.PlanetTextureCache;
import com.BreadRes.astronautics.content.planets.render.PlanetTexturePayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class AstronauticsNetwork {

    public static void register(IEventBus modBus) {
        modBus.addListener(AstronauticsNetwork::onRegisterPayloads);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                ControlPanelPayload.TYPE,
                ControlPanelPayload.STREAM_CODEC,
                ControlPanelPayload::handle
        );

        registrar.playToClient(
                PlanetTexturePayload.TYPE,
                PlanetTexturePayload.CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        PlanetTextureCache.update(payload.planetId(), payload.data())
                )
        );

        registrar.playToClient(
                AstroTeleportPayload.TYPE,
                AstroTeleportPayload.CODEC,
                AstroTeleportPayload::handle
        );
    }
}