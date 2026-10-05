package com.loadingprotection.client;

import com.loadingprotection.network.LoadingProtectionNetwork;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class LoadingProtectionClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LoadingProtectionNetwork.registerClient();
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientProtectionState.clear());
    }
}
