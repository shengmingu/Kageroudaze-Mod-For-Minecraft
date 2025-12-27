package com.mekakucity.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class mewosamasu extends Item {

    public mewosamasu(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.kageroudaze.mewosamasu.text").formatted(Formatting.GREEN));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            // 给予玩家5分钟的各种增益效果
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 6000, 4));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 6000, 0));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 6000, 1));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 6000, 4));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 6000, 7));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 6000, 0));

            user.sendMessage(Text.translatable("item.kageroudaze.mewosamasu.use").formatted(Formatting.GREEN), false);

            // 添加冷却时间（1秒）
            user.getItemCooldownManager().set(this, 1200*20);
        }

        return TypedActionResult.success(user.getStackInHand(hand));
    }
}