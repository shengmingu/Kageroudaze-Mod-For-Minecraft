package com.mekakucity.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class mekaku extends Item {

    public mekaku(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.kageroudaze.mekaku.text").formatted(Formatting.LIGHT_PURPLE));

    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);



            if (!world.isClient()) {
                // 获取物品NBT数据
                NbtCompound nbt = stack.getOrCreateNbt();
                boolean isInvisible = nbt.getBoolean("isInvisible");

                if (isInvisible) {
                    // 解除隐身
                    user.removeStatusEffect(StatusEffects.INVISIBILITY);
                    nbt.putBoolean("isInvisible", false);

                    // 播放解除音效
                    world.playSound(
                            null,
                            user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_BEACON_DEACTIVATE,
                            SoundCategory.PLAYERS,
                            1.0f,
                            1.0f
                    );

                    // 发送消息
                    user.sendMessage(Text.translatable("item.kageroudaze.mekaku.over"));
                } else {
                    // 给予无限隐身效果
                    user.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.INVISIBILITY,
                            Integer.MAX_VALUE, // 无限持续时间
                            0,                // 等级0
                            false,             // 不显示粒子
                            false,             // 不显示图标
                            true               // 环境效果
                    ));

                    nbt.putBoolean("isInvisible", true);

                    // 播放使用音效
                    world.playSound(
                            null,
                            user.getX(), user.getY(), user.getZ(),
                            SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,
                            SoundCategory.PLAYERS,
                            1.0f,
                            1.0f
                    );

                    // 发送消息
                    user.sendMessage(Text.translatable("item.kageroudaze.mekaku.start"));
                }
            }

            return TypedActionResult.success(stack, world.isClient());

    }
}