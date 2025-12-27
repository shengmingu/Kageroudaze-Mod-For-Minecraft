package com.mekakucity.event;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;

public class HeadPlacementHandler {
    public static void register() {
        // 拦截在方块上使用物品（放置头颅）
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack stack = player.getStackInHand(hand);
            NbtCompound tag = stack.getNbt();

            // 检查是否是头颅并且有不能放置的标记
            if (tag != null && tag.getBoolean("CannotBePlaced")) {
                // 取消放置行为并显示消息
                if (!world.isClient) {
                    player.sendMessage(Text.translatable("item.head_collector.cannot_place"), true);
                }
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });

        // 拦截物品使用（右键空气）
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            NbtCompound tag = stack.getNbt();

            // 检查是否是头颅并且有不能放置的标记
            if (tag != null && tag.getBoolean("CannotBePlaced")) {
                // 取消使用行为并显示消息
                if (!world.isClient) {
                    player.sendMessage(Text.translatable("item.head_collector.cannot_place"), true);
                }
                return TypedActionResult.fail(stack);
            }

            return TypedActionResult.pass(stack);
        });

        // 拦截攻击方块（可能在某些情况下用于放置）
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            ItemStack stack = player.getStackInHand(hand);
            NbtCompound tag = stack.getNbt();

            // 检查是否是头颅并且有不能放置的标记
            if (tag != null && tag.getBoolean("CannotBePlaced")) {
                // 取消攻击行为
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });
    }
}