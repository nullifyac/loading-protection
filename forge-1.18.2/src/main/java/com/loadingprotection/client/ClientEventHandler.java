package com.loadingprotection.client;

import com.loadingprotection.LoadingProtectionMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LoadingProtectionMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientEventHandler {
    private static final ResourceLocation PROTECTION_FONT = new ResourceLocation(LoadingProtectionMod.MODID, "protection");
    private static final String ICON_CHAR = "\uE000";

    private ClientEventHandler() {
    }

    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        Player player = event.getPlayer();
        if (!player.level.isClientSide) {
            return;
        }
        if (!ClientProtectionState.isProtected(player.getUUID())) {
            return;
        }
        Component base = event.getDisplayname();
        MutableComponent icon = new TextComponent(ICON_CHAR).setStyle(Style.EMPTY.withFont(PROTECTION_FONT));
        MutableComponent updated = new TextComponent("").append(icon).append(new TextComponent(" ")).append(base);
        event.setDisplayname(updated);
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        ClientProtectionState.clear();
    }
}
