package com.loadingprotection.client;

import com.loadingprotection.LoadingProtectionMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
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
        PlayerEntity player = event.getPlayer();
        if (!player.world.isRemote) {
            return;
        }
        if (!ClientProtectionState.isProtected(player.getUniqueID())) {
            return;
        }
        ITextComponent base = event.getDisplayname();
        IFormattableTextComponent icon = new StringTextComponent(ICON_CHAR)
            .setStyle(Style.EMPTY.setFontId(PROTECTION_FONT));
        IFormattableTextComponent updated = new StringTextComponent("")
            .appendSibling(icon)
            .appendSibling(new StringTextComponent(" "))
            .appendSibling(base);
        event.setDisplayname(updated);
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        ClientProtectionState.clear();
    }
}
