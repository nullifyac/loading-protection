package com.loadingprotection.network;

import com.loadingprotection.client.ClientProtectionState;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public final class LoadingProtectionNetwork {
    private LoadingProtectionNetwork() {
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(ProtectionStatusPayload.ID, (payload, context) -> {
            UUID playerId = payload.playerId();
            boolean active = payload.active();
            context.client().execute(() -> ClientProtectionState.setProtected(playerId, active));
        });
    }

    public static void sendProtectionStatusToAll(MinecraftServer server, UUID playerId, boolean active) {
        ProtectionStatusPayload payload = new ProtectionStatusPayload(playerId, active);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void sendProtectionStatus(ServerPlayerEntity player, UUID playerId, boolean active) {
        ServerPlayNetworking.send(player, new ProtectionStatusPayload(playerId, active));
    }
}
