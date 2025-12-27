package com.mekakucity.mixin;

import com.mekakucity.entity.player.PlayerAbilityManager;
import com.mekakucity.entity.player.PlayerDataStorage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements PlayerDataStorage {
    @Unique
    private NbtCompound mymodData = new NbtCompound();

    @Override
    public NbtCompound getMyModData() {
        return mymodData;
    }

    @Override
    public void setMyModData(NbtCompound nbt) {
        this.mymodData = nbt;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.put("mymod:data", mymodData);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("mymod:data", 10)) { // 10 is COMPOUND type
            mymodData = nbt.getCompound("mymod:data");
        }
    }
    // 限制交互的混入方法
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (PlayerAbilityManager.hasAbility(player)) {
            ci.cancel(); // 取消攻击
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteract(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (PlayerAbilityManager.hasAbility(player)) {
            // 使用 ActionResult 枚举值，而不是布尔值
            cir.setReturnValue(ActionResult.FAIL); // 或者 ActionResult.PASS
        }
    }

    @Inject(method = "canHarvest", at = @At("HEAD"), cancellable = true)
    private void onCanHarvest(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (PlayerAbilityManager.hasAbility(player)) {
            cir.setReturnValue(false); // 不能破坏方块
        }
    }

    @Inject(method = "isBlockBreakingRestricted", at = @At("HEAD"), cancellable = true)
    private void onIsBlockBreakingRestricted(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (PlayerAbilityManager.hasAbility(player)) {
            cir.setReturnValue(true); // 限制破坏方块
        }
    }

    @Inject(method = "getBlockBreakingSpeed", at = @At("HEAD"), cancellable = true)
    private void onGetBlockBreakingSpeed(CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (PlayerAbilityManager.hasAbility(player)) {
            cir.setReturnValue(0.0F); // 设置破坏方块速度为0
        }
    }
}