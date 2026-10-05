package com.loadingprotection.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class LoadingProtectionConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue PROTECTION_DURATION;
    public static final ModConfigSpec.BooleanValue SHOW_MESSAGES;

    static {
        BUILDER.push("Loading Protection Configuration");

        PROTECTION_DURATION = BUILDER
            .comment("Duration of protection in seconds after joining a world/server (default: 60)")
            .defineInRange("protectionDuration", 60, 1, 300);

        SHOW_MESSAGES = BUILDER
            .comment("Show messages to players when protection is active (default: true)")
            .define("showMessages", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
