package com.loadingprotection.handler;

import com.loadingprotection.LoadingProtectionMod;
import com.loadingprotection.config.LoadingProtectionConfig;
import com.loadingprotection.network.LoadingProtectionNetwork;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
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
    private final Map<UUID, ServerPlayerEntity> onlineProtectedPlayers = new HashMap<>();
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
        PlayerEntity player = event.getPlayer();
        long currentTime = System.currentTimeMillis();
        UUID uuid = player.getGameProfile().getId();
        protectedPlayers.put(uuid, new ProtectionData(currentTime, player.rotationYaw, player.rotationPitch));
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
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
        PlayerEntity player = event.getPlayer();
        UUID uuid = player.getGameProfile().getId();
        protectedPlayers.remove(uuid);
        onlineProtectedPlayers.remove(uuid);
        lastAnnouncedSeconds.remove(uuid);
    }

    /**
     * Check if a player is currently protected
     */
    private boolean isProtected(PlayerEntity player) {
        UUID uuid = player.getGameProfile().getId();
        ProtectionData data = protectedPlayers.get(uuid);
        if (data == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - data.startTime) / 1000;

        // Check if protection duration has expired
        if (elapsedSeconds >= LoadingProtectionConfig.PROTECTION_DURATION.get()) {
            protectedPlayers.remove(uuid);
            ServerPlayerEntity serverPlayer = player instanceof ServerPlayerEntity
                ? (ServerPlayerEntity) player
                : null;
            finalizeProtection(uuid, serverPlayer, "Loading protection expired.");
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
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            if (isProtected(player)) {
                event.setCanceled(true);
                return;
            }
        }

        // If a protected player is trying to attack something, cancel it
        if (source.getTrueSource() instanceof PlayerEntity) {
            PlayerEntity attacker = (PlayerEntity) source.getTrueSource();
            if (isProtected(attacker)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    /**
     * Prevent mobs from targeting protected players (1.16.5 uses LivingSetAttackTargetEvent)
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMobTarget(LivingSetAttackTargetEvent event) {
        LivingEntity target = event.getTarget();

        // If a mob is trying to target a protected player, we can't cancel this event
        // but we can track it - the attack will still be cancelled by onLivingAttack
        if (target instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) target;
            if (isProtected(player)) {
                // Note: LivingSetAttackTargetEvent cannot be cancelled in 1.16.5
                // Protection is handled through LivingAttackEvent instead
            }
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
                ServerPlayerEntity serverPlayer = onlineProtectedPlayers.get(uuid);

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

    private void notifyProtectionTime(UUID uuid, ServerPlayerEntity player, int remainingSeconds) {
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
        player.sendMessage(new StringTextComponent("Loading protection: " + remainingSeconds + " seconds remaining.")
            .mergeStyle(TextFormatting.GREEN), uuid);
    }

    private boolean hasPlayerLooked(ServerPlayerEntity player, ProtectionData data) {
        float yawDelta = MathHelper.wrapDegrees(player.rotationYaw - data.originYaw);
        float pitchDelta = player.rotationPitch - data.originPitch;
        return Math.abs(yawDelta) > LOOK_THRESHOLD_DEGREES || Math.abs(pitchDelta) > LOOK_THRESHOLD_DEGREES;
    }

    private void finalizeProtection(UUID uuid, ServerPlayerEntity serverPlayer, String message) {
        if (serverPlayer != null) {
            if (LoadingProtectionConfig.SHOW_MESSAGES.get()) {
                playerSendMessage(serverPlayer, uuid, new StringTextComponent(message).mergeStyle(TextFormatting.YELLOW));
            }
            LoadingProtectionMod.LOGGER.info("{} (player {})", message, serverPlayer.getName().getString());
        } else {
            LoadingProtectionMod.LOGGER.info("{} (player UUID {})", message, uuid);
        }
        onlineProtectedPlayers.remove(uuid);
        lastAnnouncedSeconds.remove(uuid);
        LoadingProtectionNetwork.sendProtectionStatusToAll(uuid, false);
    }

    private void syncProtectionStateTo(ServerPlayerEntity player, UUID joiningId) {
        for (UUID protectedId : protectedPlayers.keySet()) {
            if (!protectedId.equals(joiningId)) {
                LoadingProtectionNetwork.sendProtectionStatus(player, protectedId, true);
            }
        }
    }

    private void playerSendMessage(ServerPlayerEntity player, UUID uuid, ITextComponent message) {
        player.sendMessage(message, uuid);
    }
}
