package com.BreadRes.astronautics.client;

import com.BreadRes.astronautics.content.blocks.rocket_engine.RocketEngineBlockEntity;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientRocketEngines {

    public static final Set<RocketEngineBlockEntity> ENGINES =
            ConcurrentHashMap.newKeySet();

}