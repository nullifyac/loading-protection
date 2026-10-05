package com.loadingprotection.network;

import com.loadingprotection.LoadingProtectionMod;
import com.loadingprotection.client.ClientProtectionState;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;

public final class LoadingProtectionNetwork {
    public static final Identifier PROTECTION_STATUS_ID = new Identifier(LoadingProtectionMod.MODID, "protection_status");

    private LoadingProtectionNetwork() {
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(PROTECTION_STATUS_ID, (client, handler, buf, responseSender) -> {
            UUID playerId = buf.readUuid();
            boolean active = buf.readBoolean();
            client.execute(() -> ClientProtectionState.setProtected(playerId, active));
        });
    }

    public static void sendProtectionStatusToAll(MinecraftServer server, UUID playerId, boolean active) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendProtectionStatus(player, playerId, active);
        }
    }

    public static void sendProtectionStatus(ServerPlayerEntity player, UUID playerId, boolean active) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(playerId);
        buf.writeBoolean(active);
        ServerPlayNetworking.send(player, PROTECTION_STATUS_ID, buf);
    }
}
