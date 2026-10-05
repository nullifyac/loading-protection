package com.loadingprotection.mixin;

import com.loadingprotection.handler.ProtectionManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityTargetMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void loadingProtection$setTarget(LivingEntity target, CallbackInfo ci) {
        if (target instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) target;
            if (ProtectionManager.isProtected(player.getUuid())) {
                ((MobEntityAccessor) this).setTarget(null);
                ci.cancel();
            }
        }
    }
}
