package com.mekakucity.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class mewonusumu extends Item {
    private static final double MAX_DISTANCE = 100.0; // 100米距离

    public mewonusumu(Settings settings) {
        super(settings);
    }
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.kageroudaze.mewonusumu.text").formatted(Formatting.DARK_GREEN));

    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient) return TypedActionResult.pass(stack);

        // 计算玩家视线方向
        Vec3d eyePos = user.getEyePos();
        Vec3d lookVec = user.getRotationVec(1.0F);
        Vec3d endPos = eyePos.add(lookVec.multiply(MAX_DISTANCE));

        // 检测视线中的实体
        Box box = user.getBoundingBox().stretch(lookVec.multiply(MAX_DISTANCE)).expand(1.0);
        List<Entity> entities = world.getOtherEntities(user, box);

        Entity target = null;
        double closestDistance = MAX_DISTANCE * MAX_DISTANCE;

        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity)) continue;

            Box entityBox = entity.getBoundingBox();
            if (entityBox.raycast(eyePos, endPos).isPresent()) {
                double distanceSq = user.squaredDistanceTo(entity);
                if (distanceSq < closestDistance) {
                    closestDistance = distanceSq;
                    target = entity;
                }
            }
        }

        if (target != null) {
            scanEntity(user, (LivingEntity) target);
            return TypedActionResult.success(stack);
        }

        user.sendMessage(Text.translatable("item.kageroudaze.mewonusumu.fail"), false);
        return TypedActionResult.pass(stack);
    }

    private void scanEntity(PlayerEntity user, LivingEntity target) {
        // 基本信息
        user.sendMessage(Text.translatable("item.kageroudaze.mewonusumu.detail"), false);
        user.sendMessage(Text.literal(target.getName().getString()), false);
        user.sendMessage(Text.literal(String.valueOf(target.getHealth())), false);

        // 护甲值（仅对玩家有效）
        if (target instanceof PlayerEntity) {
            PlayerEntity playerTarget = (PlayerEntity) target;


            // 物品栏扫描
            user.sendMessage(Text.translatable("item.kageroudaze.mewonusumu.item"), false);
            PlayerInventory inventory = playerTarget.getInventory();
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.getStack(i);
                if (!stack.isEmpty()) {
                    user.sendMessage(Text.translatable(
                            stack.getName().getString()+
                                    stack.getCount()

                    ), false);
                }
            }
        } else {
            user.sendMessage(Text.translatable("item.kageroudaze.mewonusumu.type"), false);
        }

        user.sendMessage(Text.translatable("item.kageroudaze.mewonusumu.start"), false);
    }
}