package com.loadingprotection.network;

import com.loadingprotection.LoadingProtectionMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.UUID;

public final class LoadingProtectionNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(LoadingProtectionMod.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    private static int packetId;

    private LoadingProtectionNetwork() {
    }

    public static void init() {
        CHANNEL.registerMessage(packetId++, ProtectionStatusMessage.class,
            ProtectionStatusMessage::encode,
            ProtectionStatusMessage::decode,
            ProtectionStatusMessage::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendProtectionStatusToAll(UUID playerId, boolean active) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), new ProtectionStatusMessage(playerId, active));
    }

    public static void sendProtectionStatus(ServerPlayerEntity player, UUID playerId, boolean active) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ProtectionStatusMessage(playerId, active));
    }
}
