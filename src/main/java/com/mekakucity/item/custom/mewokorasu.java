package com.mekakucity.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// 你的自定义物品类，例如名为ItemSpectacle
public class mewokorasu extends Item {
    public mewokorasu(Settings settings) {
        super(settings);
    }
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.kageroudaze.mewokorasu.text").formatted(Formatting.AQUA));

    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) { // 确保只在服务器端执行
            // 1. 获取以玩家为中心，半径100格内的所有实体
            Box areaOfEffect = new Box(user.getBlockPos()).expand(100.0);
            List<Entity> entities = world.getOtherEntities(user, areaOfEffect);

            // 2. 遍历实体，施加发光效果
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity livingEntity) { // 通常只为生物实体施加
                    // 施加发光效果，持续时间10秒（200 tick），效果等级1
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 0));
                }
            }

            // 3. 添加冷却或消耗物品（可选）
            user.getItemCooldownManager().set(this, 20); // 设置1秒冷却（20游戏刻）
            stack.damage(1, user, (p) -> p.sendToolBreakStatus(hand)); // 如果物品可损坏，则消耗耐久
            // 如果希望一次性消耗整件物品，可以使用：
            // return TypedActionResult.consume(stack);
        }

        // 4. 播放使用音效或粒子效果（可选）
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5F, 1.0F);

        return TypedActionResult.success(stack, world.isClient());
    }
}