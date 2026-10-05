package com.loadingprotection.network;

import com.loadingprotection.LoadingProtectionMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record ProtectionStatusPayload(UUID playerId, boolean active) implements CustomPayload {
    public static final Id<ProtectionStatusPayload> ID = new Id<>(Identifier.of(LoadingProtectionMod.MODID, "protection_status"));
    public static final PacketCodec<PacketByteBuf, ProtectionStatusPayload> CODEC = PacketCodec.of(
        (payload, buf) -> {
            buf.writeUuid(payload.playerId());
            buf.writeBoolean(payload.active());
        },
        buf -> new ProtectionStatusPayload(buf.readUuid(), buf.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
