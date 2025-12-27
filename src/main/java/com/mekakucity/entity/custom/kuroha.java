package com.mekakucity.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.List;

public class kuroha extends HostileEntity {
    private final ServerBossBar bossBar;
    private boolean hasClearedInventories = false;

    // 用于存储被豁免的玩家，避免重复检查
    private final List<PlayerEntity> exemptedPlayers = new ArrayList<>();

    public kuroha(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.bossBar = new ServerBossBar(
                this.getDisplayName(),
                BossBar.Color.YELLOW,
                BossBar.Style.PROGRESS
        );
        this.setHealth(120.0f);
    }

    public static DefaultAttributeContainer.Builder createkurohaAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 120.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.GENERIC_ARMOR, 4.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(5, new LookAroundGoal(this));

        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        // 这里可以添加Boss的战斗AI目标
    }

    @Override
    public void tick() {
        super.tick();

        // 更新Boss血条
        if (!this.getWorld().isClient) {
            this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
        }

        // 持续给予Boss效果
        if (!this.getWorld().isClient) {
            // 每100 tick检查一次效果（5秒）
            if (this.age % 100 == 0) {
                // 添加力量效果
                this.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                        net.minecraft.entity.effect.StatusEffects.STRENGTH,
                        200, // 持续时间：10秒
                        0, // 等级：1（0表示等级1）
                        false,
                        false,
                        true
                ));

                // 添加抗性提升效果
                this.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                        net.minecraft.entity.effect.StatusEffects.RESISTANCE,
                        200,
                        0,
                        false,
                        false,
                        true
                ));

                // 添加速度效果
                this.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                        net.minecraft.entity.effect.StatusEffects.SPEED,
                        200,
                        0,
                        false,
                        false,
                        true
                ));
            }
        }

        // 当Boss生成时，清空所有玩家的物品栏（只执行一次）
        if (!this.getWorld().isClient && !hasClearedInventories) {
            clearPlayerInventories();
            hasClearedInventories = true;
        }
    }

    private void clearPlayerInventories() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;

        for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            // 检查玩家是否持有豁免物品
            boolean hasExemptItem = false;

            // 检查主手和副手
            ItemStack mainHand = player.getMainHandStack();
            ItemStack offHand = player.getOffHandStack();

            // 检查物品标签或名称（这里假设物品名为"meniyakitsukeru"）
            if (isMeniyakitsukeruItem(mainHand) || isMeniyakitsukeruItem(offHand)) {
                hasExemptItem = true;
            }

            // 检查物品栏中的所有物品
            if (!hasExemptItem) {
                for (int i = 0; i < player.getInventory().size(); i++) {
                    ItemStack stack = player.getInventory().getStack(i);
                    if (isMeniyakitsukeruItem(stack)) {
                        hasExemptItem = true;
                        break;
                    }
                }
            }

            // 如果玩家没有豁免物品，清空物品栏
            if (!hasExemptItem) {
                player.getInventory().clear();

            } else {
                exemptedPlayers.add(player);

            }
        }
    }

    private boolean isMeniyakitsukeruItem(ItemStack stack) {
        // 这里需要根据你的物品注册方式来判断
        // 方法1：通过物品ID或注册名
        String itemId = stack.getItem().toString().toLowerCase();
        if (itemId.contains("meniyakitsukeru")) {
            return true;
        }
        return false;




    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (this.hasCustomName()) {
            this.bossBar.setName(this.getDisplayName());
        }
        hasClearedInventories = nbt.getBoolean("HasClearedInventories");
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("HasClearedInventories", hasClearedInventories);
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        // 可以在这里添加Boss的特殊行为
    }
}