package com.loadingprotection;

import com.loadingprotection.config.LoadingProtectionConfig;
import com.loadingprotection.handler.ProtectionManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoadingProtectionMod implements ModInitializer {
    public static final String MODID = "loadingprotection";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Override
    public void onInitialize() {
        LoadingProtectionConfig.load();
        PayloadTypeRegistry.playS2C().register(
            com.loadingprotection.network.ProtectionStatusPayload.ID,
            com.loadingprotection.network.ProtectionStatusPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            ProtectionManager.onPlayerJoin(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            ProtectionManager.onPlayerLeave(handler.getPlayer().getUuid()));
        ServerTickEvents.END_SERVER_TICK.register(ProtectionManager::tick);
    }
}
