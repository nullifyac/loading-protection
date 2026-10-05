package com.loadingprotection.handler;

import com.loadingprotection.LoadingProtectionMod;
import com.loadingprotection.config.LoadingProtectionConfig;
import com.loadingprotection.network.LoadingProtectionNetwork;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class ProtectionManager {
    private static final float LOOK_THRESHOLD_DEGREES = 1.0F;

    private static final Map<UUID, ProtectionData> PROTECTED_PLAYERS = new HashMap<>();
    private static final Map<UUID, Integer> LAST_ANNOUNCED_SECONDS = new HashMap<>();

    private ProtectionManager() {
    }

    public static void onPlayerJoin(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        PROTECTED_PLAYERS.put(uuid, new ProtectionData(System.currentTimeMillis(), player.getYaw(), player.getPitch()));
        LAST_ANNOUNCED_SECONDS.remove(uuid);

        LoadingProtectionNetwork.sendProtectionStatusToAll(player.getServer(), uuid, true);
        syncProtectionStateTo(player);

        LoadingProtectionMod.LOGGER.info("Player {} logged in, protection active for {} seconds",
            player.getEntityName(), LoadingProtectionConfig.getProtectionDuration());
    }

    public static void onPlayerLeave(UUID playerId) {
        PROTECTED_PLAYERS.remove(playerId);
        LAST_ANNOUNCED_SECONDS.remove(playerId);
    }

    public static boolean isProtected(UUID playerId) {
        return PROTECTED_PLAYERS.containsKey(playerId);
    }

    public static void tick(MinecraftServer server) {
        long currentTime = System.currentTimeMillis();

        Iterator<Map.Entry<UUID, ProtectionData>> iterator = PROTECTED_PLAYERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ProtectionData> entry = iterator.next();
            UUID uuid = entry.getKey();
            ProtectionData data = entry.getValue();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);

            if (player == null) {
                iterator.remove();
                LAST_ANNOUNCED_SECONDS.remove(uuid);
                continue;
            }

            if (hasPlayerLooked(player, data)) {
                iterator.remove();
                finalizeProtection(server, uuid, player, "Loading protection disabled because you moved your view.");
                continue;
            }

            long elapsedSeconds = (currentTime - data.startTime) / 1000;
            int remainingSeconds = LoadingProtectionConfig.getProtectionDuration() - (int) elapsedSeconds;

            if (remainingSeconds <= 0) {
                iterator.remove();
                finalizeProtection(server, uuid, player, "Loading protection expired.");
                continue;
            }

            notifyProtectionTime(uuid, player, remainingSeconds);
        }
    }

    private static void notifyProtectionTime(UUID uuid, ServerPlayerEntity player, int remainingSeconds) {
        if (!LoadingProtectionConfig.showMessages() || remainingSeconds <= 0) {
            return;
        }

        if (remainingSeconds > 15) {
            if (remainingSeconds % 15 != 0) {
                return;
            }
        } else if (remainingSeconds % 5 != 0) {
            return;
        }

        int lastRemaining = LAST_ANNOUNCED_SECONDS.getOrDefault(uuid, -1);
        if (lastRemaining == remainingSeconds) {
            return;
        }

        LAST_ANNOUNCED_SECONDS.put(uuid, remainingSeconds);
        Text message = Text.literal("Loading protection: " + remainingSeconds + " seconds remaining.")
            .formatted(Formatting.GREEN);
        player.sendMessage(message, false);
    }

    private static boolean hasPlayerLooked(ServerPlayerEntity player, ProtectionData data) {
        float yawDelta = MathHelper.wrapDegrees(player.getYaw() - data.originYaw);
        float pitchDelta = player.getPitch() - data.originPitch;
        return Math.abs(yawDelta) > LOOK_THRESHOLD_DEGREES || Math.abs(pitchDelta) > LOOK_THRESHOLD_DEGREES;
    }

    private static void finalizeProtection(MinecraftServer server, UUID uuid, ServerPlayerEntity player, String message) {
        if (LoadingProtectionConfig.showMessages()) {
            player.sendMessage(Text.literal(message).formatted(Formatting.YELLOW), false);
        }
        LoadingProtectionMod.LOGGER.info("{} (player {})", message, player.getEntityName());
        LoadingProtectionNetwork.sendProtectionStatusToAll(server, uuid, false);
        LAST_ANNOUNCED_SECONDS.remove(uuid);
    }

    private static void syncProtectionStateTo(ServerPlayerEntity player) {
        for (UUID protectedId : PROTECTED_PLAYERS.keySet()) {
            if (!protectedId.equals(player.getUuid())) {
                LoadingProtectionNetwork.sendProtectionStatus(player, protectedId, true);
            }
        }
    }

    private static final class ProtectionData {
        private final long startTime;
        private final float originYaw;
        private final float originPitch;

        private ProtectionData(long startTime, float originYaw, float originPitch) {
            this.startTime = startTime;
            this.originYaw = originYaw;
            this.originPitch = originPitch;
        }
    }
}
