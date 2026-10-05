package com.loadingprotection.client;

import com.loadingprotection.LoadingProtectionMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = LoadingProtectionMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ClientEventHandler {
    private static final ResourceLocation PROTECTION_FONT =
        ResourceLocation.fromNamespaceAndPath(LoadingProtectionMod.MODID, "protection");
    private static final String ICON_CHAR = "\uE000";

    private ClientEventHandler() {
    }

    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        if (!event.getEntity().level().isClientSide()) {
            return;
        }
        if (!ClientProtectionState.isProtected(event.getEntity().getUUID())) {
            return;
        }
        Component base = event.getDisplayname();
        MutableComponent icon = Component.literal(ICON_CHAR).setStyle(Style.EMPTY.withFont(PROTECTION_FONT));
        MutableComponent updated = Component.literal("").append(icon).append(Component.literal(" ")).append(base);
        event.setDisplayname(updated);
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientProtectionState.clear();
    }
}
