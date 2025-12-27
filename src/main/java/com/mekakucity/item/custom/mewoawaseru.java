package com.mekakucity.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class mewoawaseru extends Item {
    public mewoawaseru(Settings settings) {
        super(settings);
    }
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.kageroudaze.mewoawaseru.text").formatted(Formatting.WHITE));

    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            // 获取以玩家为中心，半径100格内的所有生物
            Vec3d playerPos = user.getPos();
            double radius = 100.0;
            Box area = new Box(
                    playerPos.x - radius, playerPos.y - radius, playerPos.z - radius,
                    playerPos.x + radius, playerPos.y + radius, playerPos.z + radius
            );

            List<LivingEntity> entities = world.getEntitiesByClass(
                    LivingEntity.class, area, entity ->
                            entity != user && // 排除玩家自己
                                    entity.distanceTo(user) <= radius // 确保在圆形范围内
            );

            // 对每个生物施加极强的迟缓效果和无法攻击的效果
            for (LivingEntity entity : entities) {
                // 等级255的迟缓效果（最高级别，几乎无法移动）
                StatusEffectInstance slowness = new StatusEffectInstance(
                        StatusEffects.SLOWNESS, // 迟缓效果
                        1200,   // 持续时间（60秒）
                        255,    // 效果等级（最高级别）
                        false,  // 环境粒子效果
                        true,   // 显示图标
                        true    // 显示粒子效果
                );

                // 等级255的挖掘疲劳效果（使实体无法攻击）
                StatusEffectInstance miningFatigue = new StatusEffectInstance(
                        StatusEffects.MINING_FATIGUE, // 挖掘疲劳效果
                        1200,   // 持续时间（60秒）
                        255,    // 效果等级（最高级别）
                        false,  // 环境粒子效果
                        true,   // 显示图标
                        true    // 显示粒子效果
                );

                // 虚弱效果（降低攻击伤害至几乎为零）
                StatusEffectInstance weakness = new StatusEffectInstance(
                        StatusEffects.WEAKNESS, // 虚弱效果
                        1200,   // 持续时间（60秒）
                        255,    // 效果等级（最高级别）
                        false,  // 环境粒子效果
                        true,   // 显示图标
                        true    // 显示粒子效果
                );

                entity.addStatusEffect(slowness);
                entity.addStatusEffect(miningFatigue);
                entity.addStatusEffect(weakness);

                // 如果是敌对生物，清除其攻击目标
                if (entity instanceof net.minecraft.entity.mob.HostileEntity) {
                    ((net.minecraft.entity.mob.HostileEntity) entity).setTarget(null);
                }
            }



            // 添加冷却时间，防止滥用
            user.getItemCooldownManager().set(this, 20*180); // 5秒冷却（20 ticks/秒）

            // 播放使用音效
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BLOCK_BEACON_ACTIVATE, user.getSoundCategory(),
                    1.0F, 1.0F);
        }

        return TypedActionResult.success(stack, world.isClient());
    }
}