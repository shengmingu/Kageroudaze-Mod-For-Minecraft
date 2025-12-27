package com.mekakucity.item.custom;

import com.mekakucity.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class phone extends Item {
    private static final Identifier ADVANCEMENT_ID =
            new Identifier("kageroudaze", "eneteleport");



    public phone(Settings settings) {
        super(settings);
    }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            ServerPlayerEntity serverUser = (ServerPlayerEntity) user;

            // 获取所有在线玩家
            List<ServerPlayerEntity> players = ((ServerWorld) world).getPlayers();

            boolean teleported = false;
            for (ServerPlayerEntity player : players) {
                // 跳过使用者自己
                if (player == serverUser) {

                    continue;
                }

                // 检查玩家是否持有megasameru
                if (hasMegasameru(player)) {
                    // 传送玩家到使用者位置
                    teleportPlayer(player, serverUser);
                    teleported = true;

                    // 发送消息通知

                }
            }


        }


        return TypedActionResult.success(user.getStackInHand(hand));
    }

    private boolean hasMegasameru(PlayerEntity player) {
        // 检查主手
        if (player.getMainHandStack().getItem() == ModItems.megasameru) {
            return true;
        }

        // 检查副手
        if (player.getOffHandStack().getItem() == ModItems.megasameru) {
            return true;
        }

        // 检查背包
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == ModItems.megasameru) {
                return true;
            }
        }

        return false;
    }

    private void teleportPlayer(ServerPlayerEntity target, ServerPlayerEntity destination) {
        ServerWorld world = (ServerWorld) destination.getWorld();

        // 保存目标玩家的朝向
        float yaw = target.getYaw();
        float pitch = target.getPitch();

        // 传送到目的地玩家位置
        target.teleport(
                world,
                destination.getX(),
                destination.getY(),
                destination.getZ(),
                yaw,
                pitch
        );
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.kageroudaze.phone.text").formatted(Formatting.BLUE));
        tooltip.add(Text.translatable("item.kageroudaze.phone.info"));


    }


}