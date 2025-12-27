package com.mekakucity.mixin;

import com.mekakucity.entity.player.HeadAbilitySystem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {

    @Inject(
            method = "setTarget",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSetTarget(LivingEntity target, CallbackInfo ci) {
        MobEntity mob = (MobEntity) (Object) this;

        // 检查目标是否为玩家并且玩家是否穿戴了对应类型的头颅
        if (target instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) target;
            if (HeadAbilitySystem.shouldIgnorePlayer(mob, player)) {
                ci.cancel(); // 取消设置目标

                // 发送消息给玩家
                if (!mob.getWorld().isClient) {
                    player.sendMessage(net.minecraft.text.Text.translatable(
                            "已伪装",
                            mob.getName().getString()
                    ), true);
                }
            }
        }
    }
}