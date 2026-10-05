package com.loadingprotection.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.loadingprotection.LoadingProtectionMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LoadingProtectionConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("loadingprotection.json");

    private static int protectionDuration = 60;
    private static boolean showMessages = true;

    private LoadingProtectionConfig() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) {
                save();
                return;
            }
            if (root.has("protectionDuration")) {
                protectionDuration = root.get("protectionDuration").getAsInt();
            }
            if (root.has("showMessages")) {
                showMessages = root.get("showMessages").getAsBoolean();
            }
        } catch (IOException e) {
            LoadingProtectionMod.LOGGER.warn("Failed to load config, using defaults", e);
        }
    }

    private static void save() {
        JsonObject root = new JsonObject();
        root.addProperty("protectionDuration", protectionDuration);
        root.addProperty("showMessages", showMessages);
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            LoadingProtectionMod.LOGGER.warn("Failed to save config", e);
        }
    }

    public static int getProtectionDuration() {
        return protectionDuration;
    }

    public static boolean showMessages() {
        return showMessages;
    }
}
