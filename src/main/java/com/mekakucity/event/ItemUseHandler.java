package com.mekakucity.event;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import net.minecraft.util.TypedActionResult;

public class ItemUseHandler {
    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            NbtCompound tag = stack.getNbt();

            // 检查是否是头颅并且有不能放置的标记
            if (tag != null && tag.getBoolean("CannotBePlaced")) {
                // 取消使用行为并显示消息
                if (!world.isClient) {
                    player.sendMessage(Text.translatable("message.kageroudaze.item.useplace"), true);
                }
                return TypedActionResult.fail(stack);
            }

            return TypedActionResult.pass(stack);
        });
    }
}