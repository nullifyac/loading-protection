package com.loadingprotection;

import com.loadingprotection.config.LoadingProtectionConfig;
import com.loadingprotection.handler.ProtectionHandler;
import com.loadingprotection.network.LoadingProtectionNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(LoadingProtectionMod.MODID)
public class LoadingProtectionMod {
    public static final String MODID = "loadingprotection";
    public static final Logger LOGGER = LoggerFactory.getLogger(LoadingProtectionMod.class);

    public LoadingProtectionMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, LoadingProtectionConfig.SPEC);

        LoadingProtectionNetwork.init();

        // Register event handler
        MinecraftForge.EVENT_BUS.register(new ProtectionHandler());

        LOGGER.info("Loading Protection Mod initialized!");
    }
}
