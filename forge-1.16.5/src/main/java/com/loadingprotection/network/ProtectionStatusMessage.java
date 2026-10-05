package com.loadingprotection.network;

import com.loadingprotection.client.ClientProtectionState;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ProtectionStatusMessage {
    private final UUID playerId;
    private final boolean active;

    public ProtectionStatusMessage(UUID playerId, boolean active) {
        this.playerId = playerId;
        this.active = active;
    }

    public static void encode(ProtectionStatusMessage message, PacketBuffer buffer) {
        buffer.writeUniqueId(message.playerId);
        buffer.writeBoolean(message.active);
    }

    public static ProtectionStatusMessage decode(PacketBuffer buffer) {
        return new ProtectionStatusMessage(buffer.readUniqueId(), buffer.readBoolean());
    }

    public static void handle(ProtectionStatusMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientProtectionState.setProtected(message.playerId, message.active));
        context.get().setPacketHandled(true);
    }
}
