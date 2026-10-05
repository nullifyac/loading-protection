package com.loadingprotection.handler;

import com.loadingprotection.LoadingProtectionMod;
import com.loadingprotection.config.LoadingProtectionConfig;
import com.loadingprotection.network.LoadingProtectionNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ProtectionHandler {
    private static final float LOOK_THRESHOLD_DEGREES = 1.0F;

    // Map to track when players joined and their protection status
    private final Map<UUID, ProtectionData> protectedPlayers = new HashMap<>();
    private final Map<UUID, ServerPlayer> onlineProtectedPlayers = new HashMap<>();
    private final Map<UUID, Integer> lastAnnouncedSeconds = new HashMap<>();

    private static class ProtectionData {
        private final long startTime;
        private final float originYaw;
        private final float originPitch;

        private ProtectionData(long startTime, float originYaw, float originPitch) {
            this.startTime = startTime;
            this.originYaw = originYaw;
            this.originPitch = originPitch;
        }
    }

    /**
     * Called when a player logs into the server
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getPlayer();
        long currentTime = System.currentTimeMillis();
        UUID uuid = player.getUUID();
        protectedPlayers.put(uuid, new ProtectionData(currentTime, player.getYRot(), player.getXRot()));
        if (player instanceof ServerPlayer serverPlayer) {
            onlineProtectedPlayers.put(uuid, serverPlayer);
            LoadingProtectionNetwork.sendProtectionStatusToAll(uuid, true);
            syncProtectionStateTo(serverPlayer, uuid);
        }
        lastAnnouncedSeconds.remove(uuid);

        LoadingProtectionMod.LOGGER.info("Player {} logged in, protection active for {} seconds",
            player.getName().getString(),
            LoadingProtectionConfig.PROTECTION_DURATION.get());
    }

    /**
     * Called when a player logs out - clean up the map
     */
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUUID();
        protectedPlayers.remove(uuid);
        onlineProtectedPlayers.remove(uuid);
        lastAnnouncedSeconds.remove(uuid);
    }

    /**
     * Check if a player is currently protected
     */
    private boolean isProtected(Player player) {
        UUID uuid = player.getUUID();
        ProtectionData data = protectedPlayers.get(uuid);
        if (data == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - data.startTime) / 1000;

        // Check if protection duration has expired
        if (elapsedSeconds >= LoadingProtectionConfig.PROTECTION_DURATION.get()) {
            protectedPlayers.remove(uuid);
            finalizeProtection(uuid, player instanceof ServerPlayer serverPlayer ? serverPlayer : null, "Loading protection expired.");
            return false;
        }

        return true;
    }

    /**
     * Prevent protected players from taking damage
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingAttack(LivingAttackEvent event) {
        LivingEntity entity = event.getEntityLiving();
        DamageSource source = event.getSource();

        // If the entity being attacked is a protected player, cancel the damage
        if (entity instanceof Player player && isProtected(player)) {
            event.setCanceled(true);
            return;
        }

        // If a protected player is trying to attack something, cancel it
        if (source.getEntity() instanceof Player attacker && isProtected(attacker)) {
            event.setCanceled(true);
            return;
        }
    }

    /**
     * Prevent mobs from targeting protected players
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMobTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewTarget();

        // If a mob is trying to target a protected player, cancel it
        if (newTarget instanceof Player player && isProtected(player)) {
            event.setCanceled(true);
        }
    }

    /**
     * Periodic tick to check for expired protections
     * This ensures we don't keep players in the map forever
     */
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            long currentTime = System.currentTimeMillis();

            Iterator<Map.Entry<UUID, ProtectionData>> iterator = protectedPlayers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, ProtectionData> entry = iterator.next();
                UUID uuid = entry.getKey();
                ProtectionData data = entry.getValue();
                ServerPlayer serverPlayer = onlineProtectedPlayers.get(uuid);

                if (serverPlayer != null && hasPlayerLooked(serverPlayer, data)) {
                    iterator.remove();
                    finalizeProtection(uuid, serverPlayer, "Loading protection disabled because you moved your view.");
                    continue;
                }

                long elapsedSeconds = (currentTime - data.startTime) / 1000;
                int remainingSeconds = LoadingProtectionConfig.PROTECTION_DURATION.get() - (int) elapsedSeconds;

                if (remainingSeconds <= 0) {
                    iterator.remove();
                    finalizeProtection(uuid, serverPlayer, "Loading protection expired.");
                    continue;
                }

                if (serverPlayer != null) {
                    notifyProtectionTime(uuid, serverPlayer, remainingSeconds);
                }
            }
        }
    }

    private void notifyProtectionTime(UUID uuid, ServerPlayer player, int remainingSeconds) {
        if (!LoadingProtectionConfig.SHOW_MESSAGES.get() || remainingSeconds <= 0) {
            return;
        }

        if (remainingSeconds > 15) {
            if (remainingSeconds % 15 != 0) {
                return;
            }
        } else if (remainingSeconds % 5 != 0) {
            return;
        }

        int lastRemaining = lastAnnouncedSeconds.getOrDefault(uuid, -1);
        if (lastRemaining == remainingSeconds) {
            return;
        }

        lastAnnouncedSeconds.put(uuid, remainingSeconds);
        Component message = new TextComponent("Loading protection: " + remainingSeconds + " seconds remaining.")
            .withStyle(ChatFormatting.GREEN);
        player.sendMessage(message, uuid);
    }

    private boolean hasPlayerLooked(ServerPlayer player, ProtectionData data) {
        float yawDelta = Mth.wrapDegrees(player.getYRot() - data.originYaw);
        float pitchDelta = player.getXRot() - data.originPitch;
        return Math.abs(yawDelta) > LOOK_THRESHOLD_DEGREES || Math.abs(pitchDelta) > LOOK_THRESHOLD_DEGREES;
    }

    private void finalizeProtection(UUID uuid, ServerPlayer serverPlayer, String message) {
        if (serverPlayer != null) {
            if (LoadingProtectionConfig.SHOW_MESSAGES.get()) {
                sendSystemMessage(serverPlayer, uuid, new TextComponent(message).withStyle(ChatFormatting.YELLOW));
            }
            LoadingProtectionMod.LOGGER.info("{} (player {})", message, serverPlayer.getName().getString());
        } else {
            LoadingProtectionMod.LOGGER.info("{} (player UUID {})", message, uuid);
        }
        onlineProtectedPlayers.remove(uuid);
        lastAnnouncedSeconds.remove(uuid);
        LoadingProtectionNetwork.sendProtectionStatusToAll(uuid, false);
    }

    private void syncProtectionStateTo(ServerPlayer player, UUID joiningId) {
        for (UUID protectedId : protectedPlayers.keySet()) {
            if (!protectedId.equals(joiningId)) {
                LoadingProtectionNetwork.sendProtectionStatus(player, protectedId, true);
            }
        }
    }

    private void sendSystemMessage(ServerPlayer player, UUID uuid, Component message) {
        player.sendMessage(message, uuid);
    }
}
