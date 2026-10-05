package com.loadingprotection;

import com.loadingprotection.config.LoadingProtectionConfig;
import com.loadingprotection.handler.ProtectionHandler;
import com.loadingprotection.network.LoadingProtectionNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(LoadingProtectionMod.MODID)
public class LoadingProtectionMod {
    public static final String MODID = "loadingprotection";
    public static final Logger LOGGER = LoggerFactory.getLogger(LoadingProtectionMod.class);

    public LoadingProtectionMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("[LoadingProtection] Starting mod initialization...");

        // Register config
        modContainer.registerConfig(ModConfig.Type.COMMON, LoadingProtectionConfig.SPEC);
        LOGGER.info("[LoadingProtection] Config registered");

        modEventBus.addListener(LoadingProtectionNetwork::register);

        // Register event handler
        ProtectionHandler handler = new ProtectionHandler();
        NeoForge.EVENT_BUS.register(handler);
        LOGGER.info("[LoadingProtection] Event handler registered: {}", handler.getClass().getName());

        LOGGER.info("[LoadingProtection] Mod initialized successfully!");
    }
}
