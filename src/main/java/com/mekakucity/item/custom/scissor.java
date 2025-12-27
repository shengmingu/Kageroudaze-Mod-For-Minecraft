package com.mekakucity.item.custom;

import com.mekakucity.util.CalendarUsageData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class scissor extends Item {

    // 自定义维度的ID
    private static final Identifier CUSTOM_DIMENSION_ID =
            new Identifier("kageroudaze", "kageroudazeworld_dimension");

    // 物品ID
    private static final Identifier SCARF_ITEM_ID = new Identifier("kageroudaze", "scarf.json");
    private static final Identifier MENIYAKITSUKERU_ITEM_ID = new Identifier("kageroudaze", "meniyakitsukeru");

    // 存储需要传送的玩家，避免立即传送
    private static final Map<UUID, TeleportData> pendingTeleports = new HashMap<>();

    public scissor(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && user instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) user;

            // 检查玩家是否使用了日历并在有效时间内
            if (!CalendarUsageData.isValidDeathTime(player)) {
                // 没有使用日历或日历效果已过期
                player.sendMessage(Text.translatable("item.kageroudaze.scissor.not"), false);
                return TypedActionResult.fail(stack);
            }

            // 检查玩家物品栏中是否有scarf
            boolean hasScarf = hasItemInInventory(player, SCARF_ITEM_ID);

            // 将传送任务加入队列，延迟执行
            pendingTeleports.put(player.getUuid(), new TeleportData(hasScarf));

            // 给玩家一个短暂的无敌效果，防止传送过程中受伤
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.RESISTANCE,
                    60, // 3秒
                    4,  // 等级5，基本上无敌
                    false,
                    false
            ));

            // 发送消息
            player.sendMessage(Text.translatable("item.kageroudaze.scissor.arrive"), false);

            // 延迟执行传送
            player.getServer().execute(() -> {
                processTeleport(player);
            });

            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }

    // 处理传送
    private void processTeleport(ServerPlayerEntity player) {
        TeleportData data = pendingTeleports.remove(player.getUuid());
        if (data == null) return;

        try {
            // 获取自定义维度
            RegistryKey<World> customDimensionKey = RegistryKey.of(RegistryKeys.WORLD, CUSTOM_DIMENSION_ID);
            ServerWorld customWorld = player.getServer().getWorld(customDimensionKey);

            if (customWorld == null) {

                return;
            }

            // 查找安全的重生位置
            BlockPos spawnPos = findSafeSpawnLocation(customWorld);

            // 如果玩家有scarf，给予meniyakitsukeru
            if (data.hasScarf) {
                giveMeniYakitsukeru(player);
            }

            // 安全传送玩家
            safeTeleport(player, customWorld, spawnPos);



            // 清除日历使用记录
            CalendarUsageData.removePlayerRecord(player.getUuid());

        } catch (Exception e) {
            System.err.println("Error in scissor teleport: " + e.getMessage());
            e.printStackTrace();

        }
    }

    // 安全传送玩家
    private void safeTeleport(ServerPlayerEntity player, ServerWorld targetWorld, BlockPos targetPos) {
        try {
            // 保存玩家数据
            float yaw = player.getYaw();
            float pitch = player.getPitch();

            // 检查是否已经在目标维度
            if (player.getWorld().getRegistryKey().equals(targetWorld.getRegistryKey())) {
                // 在同一维度内传送
                teleportInSameDimension(player, targetWorld, targetPos, yaw, pitch);
            } else {
                // 跨维度传送 - 使用更安全的方式
                teleportToDifferentDimension(player, targetWorld, targetPos, yaw, pitch);
            }

            // 设置重生点
            setRespawnPoint(player, targetWorld, targetPos);

        } catch (Exception e) {
            System.err.println("Error in safeTeleport: " + e.getMessage());
            throw e;
        }
    }

    // 在同一维度内传送
    private void teleportInSameDimension(ServerPlayerEntity player, ServerWorld world, BlockPos pos, float yaw, float pitch) {
        // 直接传送
        player.teleport(
                world,
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5,
                yaw,
                pitch
        );

        // 重置摔落距离
        player.fallDistance = 0;
    }

    // 跨维度传送
    private void teleportToDifferentDimension(ServerPlayerEntity player, ServerWorld targetWorld, BlockPos targetPos, float yaw, float pitch) {
        // 使用服务器的world changer来安全地切换维度
        player.getServer().execute(() -> {
            try {
                // 这里使用更保守的传送方式
                player.teleport(
                        targetWorld,
                        targetPos.getX() + 0.5,
                        targetPos.getY(),
                        targetPos.getZ() + 0.5,
                        yaw,
                        pitch
                );

                // 重置摔落距离
                player.fallDistance = 0;

            } catch (Exception e) {
                System.err.println("Error in teleportToDifferentDimension: " + e.getMessage());

                // 如果上面的方法失败，尝试备用方法
                try {
                    // 备用传送方法
                    ServerPlayerEntity movedPlayer = (ServerPlayerEntity) player.moveToWorld(targetWorld);
                    if (movedPlayer != null) {
                        movedPlayer.teleport(
                                targetPos.getX() + 0.5,
                                targetPos.getY(),
                                targetPos.getZ() + 0.5
                        );
                        movedPlayer.fallDistance = 0;
                    }
                } catch (Exception ex) {
                    System.err.println("Backup teleport also failed: " + ex.getMessage());
                }
            }
        });
    }

    // 设置重生点
    private void setRespawnPoint(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        player.setSpawnPoint(
                world.getRegistryKey(),
                pos,
                0.0f,
                true,
                false
        );



        System.out.println("成功为玩家 " + player.getName().getString() +
                " 在维度 " + world.getRegistryKey().getValue() +
                " 设置重生点于 " + pos);
    }

    // 检查玩家物品栏中是否有特定物品
    private boolean hasItemInInventory(ServerPlayerEntity player, Identifier itemId) {
        Item item = Registries.ITEM.get(itemId);
        if (item == null) {
            System.err.println("找不到物品: " + itemId);
            return false;
        }

        // 检查主物品栏和快捷栏
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == item && stack.getCount() > 0) {
                return true;
            }
        }

        return false;
    }

    // 给予玩家meniyakitsukeru物品
    private void giveMeniYakitsukeru(ServerPlayerEntity player) {
        Item meniItem = Registries.ITEM.get(MENIYAKITSUKERU_ITEM_ID);
        if (meniItem != null) {
            ItemStack meniStack = new ItemStack(meniItem);

            // 尝试添加到玩家物品栏
            if (!player.getInventory().insertStack(meniStack)) {
                // 如果物品栏满了，掉落在地上
                player.dropItem(meniStack, false);
            }

            player.sendMessage(Text.translatable("item.kageroudaze.scissor.start"), false);
        } else {
            System.err.println("找不到物品: " + MENIYAKITSUKERU_ITEM_ID);
        }
    }

    // 查找安全的生成位置
    private BlockPos findSafeSpawnLocation(ServerWorld world) {
        try {
            // 使用世界出生点
            BlockPos spawnPos = world.getSpawnPos();

            // 确保Y坐标在合理范围内
            if (spawnPos.getY() < world.getBottomY() + 5) {
                spawnPos = new BlockPos(spawnPos.getX(), 65, spawnPos.getZ());
            } else if (spawnPos.getY() > world.getTopY() - 5) {
                spawnPos = new BlockPos(spawnPos.getX(), world.getTopY() - 10, spawnPos.getZ());
            }

            // 寻找安全位置
            return findSafePositionAround(world, spawnPos);

        } catch (Exception e) {
            // 如果出错，返回默认安全位置
            return new BlockPos(0, 65, 0);
        }
    }

    // 在周围寻找安全位置
    private BlockPos findSafePositionAround(ServerWorld world, BlockPos center) {
        // 从中心点开始向外寻找
        for (int radius = 0; radius <= 10; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                        // 检查这个位置
                        BlockPos checkPos = center.add(dx, 0, dz);

                        // 从合适的高度开始向下寻找
                        for (int y = Math.min(world.getTopY(), center.getY() + 20);
                             y > world.getBottomY() + 1; y--) {
                            BlockPos pos = new BlockPos(checkPos.getX(), y, checkPos.getZ());

                            if (isSafePosition(world, pos)) {
                                return pos;
                            }
                        }
                    }
                }
            }
        }

        // 如果找不到安全位置，返回默认位置
        return center;
    }

    // 检查位置是否安全
    private boolean isSafePosition(ServerWorld world, BlockPos pos) {
        // 检查脚下是否有固体方块
        if (!world.getBlockState(pos.down()).isSolid()) {
            return false;
        }

        // 检查上方是否有足够空间
        if (!world.getBlockState(pos).isAir()) {
            return false;
        }

        if (!world.getBlockState(pos.up()).isAir()) {
            return false;
        }

        return true;
    }

    // 传送数据类
    private static class TeleportData {
        final boolean hasScarf;

        TeleportData(boolean hasScarf) {
            this.hasScarf = hasScarf;
        }
    }
}