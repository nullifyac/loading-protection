package com.loadingprotection.network;

import com.loadingprotection.client.ClientProtectionState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ProtectionStatusMessage {
    private final UUID playerId;
    private final boolean active;

    public ProtectionStatusMessage(UUID playerId, boolean active) {
        this.playerId = playerId;
        this.active = active;
    }

    public static void encode(ProtectionStatusMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.playerId);
        buffer.writeBoolean(message.active);
    }

    public static ProtectionStatusMessage decode(FriendlyByteBuf buffer) {
        return new ProtectionStatusMessage(buffer.readUUID(), buffer.readBoolean());
    }

    public static void handle(ProtectionStatusMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientProtectionState.setProtected(message.playerId, message.active));
        context.get().setPacketHandled(true);
    }
}
