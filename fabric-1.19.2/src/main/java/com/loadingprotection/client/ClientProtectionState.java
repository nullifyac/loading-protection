package com.loadingprotection.client;

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
    }

    public static void clear() {
        PROTECTED.clear();
    }
}
