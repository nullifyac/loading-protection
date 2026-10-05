package com.loadingprotection.network;

import com.loadingprotection.LoadingProtectionMod;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record ProtectionStatusPayload(UUID playerId, boolean active) implements CustomPacketPayload {
    public static final Type<ProtectionStatusPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LoadingProtectionMod.MODID, "protection_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProtectionStatusPayload> STREAM_CODEC =
        StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ProtectionStatusPayload::playerId,
            ByteBufCodecs.BOOL, ProtectionStatusPayload::active,
            ProtectionStatusPayload::new);

    @Override
    public Type<ProtectionStatusPayload> type() {
        return TYPE;
    }
}
