package com.loadingprotection.mixin.client;

import com.loadingprotection.LoadingProtectionMod;
import com.loadingprotection.client.ClientProtectionState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityNameMixin {
    private static final Identifier PROTECTION_FONT = new Identifier(LoadingProtectionMod.MODID, "protection");
    private static final String ICON_CHAR = "";

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void loadingProtection$getDisplayName(CallbackInfoReturnable<Text> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!player.getWorld().isClient()) {
            return;
        }
        if (!ClientProtectionState.isProtected(player.getUuid())) {
            return;
        }
        Text base = cir.getReturnValue();
        MutableText icon = new LiteralText(ICON_CHAR).setStyle(Style.EMPTY.withFont(PROTECTION_FONT));
        MutableText updated = new LiteralText("").append(icon).append(new LiteralText(" ")).append(base);
        cir.setReturnValue(updated);
    }
}
