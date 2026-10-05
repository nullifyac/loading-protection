package com.loadingprotection.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientProtectionState {
    private static final Set<UUID> PROTECTED = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private ClientProtectionState() {
    }

    public static boolean isProtected(UUID playerId) {
        return PROTECTED.contains(playerId);
    }

    public static void setProtected(UUID playerId, boolean active) {
        if (active) {
            PROTECTED.add(playerId);
        } else {
            PROTECTED.remove(playerId);
        }
        refreshDisplayName(playerId);
    }

    public static void clear() {
        PROTECTED.clear();
    }

    private static void refreshDisplayName(UUID playerId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.world == null) {
            return;
        }
        for (PlayerEntity player : minecraft.world.getPlayers()) {
            if (player.getUniqueID().equals(playerId)) {
                try {
                    player.getClass().getMethod("refreshDisplayName").invoke(player);
                } catch (ReflectiveOperationException ignored) {
                }
                break;
            }
        }
    }
}
