package com.loadingprotection.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

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
        if (minecraft.level == null) {
            return;
        }
        Player player = minecraft.level.getPlayerByUUID(playerId);
        if (player != null) {
            player.refreshDisplayName();
        }
    }
}
