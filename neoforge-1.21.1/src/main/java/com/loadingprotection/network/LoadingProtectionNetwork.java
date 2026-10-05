package com.loadingprotection.network;

import com.loadingprotection.client.ClientProtectionState;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.UUID;

public final class LoadingProtectionNetwork {
    private static final String PROTOCOL_VERSION = "1";

    private LoadingProtectionNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(ProtectionStatusPayload.TYPE, ProtectionStatusPayload.STREAM_CODEC, LoadingProtectionNetwork::handleProtectionStatus);
    }

    public static void sendProtectionStatusToAll(UUID playerId, boolean active) {
        PacketDistributor.sendToAllPlayers(new ProtectionStatusPayload(playerId, active));
    }

    public static void sendProtectionStatus(ServerPlayer player, UUID playerId, boolean active) {
        PacketDistributor.sendToPlayer(player, new ProtectionStatusPayload(playerId, active));
    }

    private static void handleProtectionStatus(ProtectionStatusPayload payload, IPayloadContext context) {
        ClientProtectionState.setProtected(payload.playerId(), payload.active());
    }
}
