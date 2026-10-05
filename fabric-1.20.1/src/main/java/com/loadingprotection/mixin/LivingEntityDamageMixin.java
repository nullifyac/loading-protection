package com.loadingprotection.mixin;

import com.loadingprotection.handler.ProtectionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void loadingProtection$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            if (ProtectionManager.isProtected(player.getUuid())) {
                cir.setReturnValue(false);
                return;
            }
        }

        Entity attacker = source.getAttacker();
        if (attacker instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) attacker;
            if (ProtectionManager.isProtected(player.getUuid())) {
                cir.setReturnValue(false);
            }
        }
    }
}
