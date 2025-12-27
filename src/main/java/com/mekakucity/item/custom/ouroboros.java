package com.mekakucity.item.custom;

import net.minecraft.client.item.TooltipContext;
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
import net.minecraft.world.World;
import net.minecraft.util.Formatting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ouroboros extends Item {

    // 维度ID
    private static final Identifier CUSTOM_DIMENSION_ID =
            new Identifier("kageroudaze", "kageroudazeworld_dimension");

    // 主世界维度ID
    private static final RegistryKey<World> OVERWORLD_KEY = World.OVERWORLD;

    // 存储需要传送的玩家，避免立即传送
    private static final Map<UUID, TeleportData> pendingTeleports = new HashMap<>();

    public ouroboros(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && user instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) user;

            // 获取当前维度
            RegistryKey<World> currentDimension = player.getWorld().getRegistryKey();
            RegistryKey<World> customDimensionKey = RegistryKey.of(RegistryKeys.WORLD, CUSTOM_DIMENSION_ID);

            // 检查是否在自定义维度中
            if (!currentDimension.equals(customDimensionKey)) {
                // 不在自定义维度中，无法使用

                return TypedActionResult.fail(stack);
            }

            // 获取主世界
            ServerWorld overworld = player.getServer().getWorld(OVERWORLD_KEY);

            if (overworld == null) {

                return TypedActionResult.fail(stack);
            }

            // 查找主世界的安全重生位置
            BlockPos spawnPos = findSafeSpawnLocation(overworld, player);

            // 将传送任务加入队列，延迟执行
            pendingTeleports.put(player.getUuid(), new TeleportData(spawnPos));

            // 给玩家一个短暂的无敌效果，防止传送过程中受伤
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.RESISTANCE,
                    60, // 3秒
                    4,  // 等级5，基本上无敌
                    false,
                    false
            ));

            // 播放使用动画或效果（可选）
            player.getItemCooldownManager().set(this, 20); // 1秒冷却

            // 发送开始传送的消息


            // 延迟执行传送
            player.getServer().execute(() -> {
                processTeleport(player, overworld);
            });

            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.kageroudaze.ouroboros.text").formatted(Formatting.RED));



    }

    // 处理传送
    private void processTeleport(ServerPlayerEntity player, ServerWorld overworld) {
        TeleportData data = pendingTeleports.remove(player.getUuid());
        if (data == null) return;

        try {
            // 安全传送玩家到主世界
            safeTeleport(player, overworld, data.spawnPos);




            player.getMainHandStack().decrement(1);

        } catch (Exception e) {
            System.err.println("Error in ouroboros teleport: " + e.getMessage());
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
                // 跨维度传送
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



        System.out.println("衔尾蛇：成功为玩家 " + player.getName().getString() +
                " 在维度 " + world.getRegistryKey().getValue() +
                " 设置重生点于 " + pos);
    }

    // 查找安全的生成位置（现在接受玩家参数）
    private BlockPos findSafeSpawnLocation(ServerWorld world, ServerPlayerEntity player) {
        try {
            // 尝试使用玩家的原始重生点（如果存在且在主世界）
            BlockPos playerSpawnPos = player.getSpawnPointPosition();
            RegistryKey<World> playerSpawnDimension = player.getSpawnPointDimension();

            if (playerSpawnPos != null && playerSpawnDimension != null &&
                    playerSpawnDimension.equals(OVERWORLD_KEY)) {
                // 玩家在主世界有设置重生点，尝试使用它
                return findSafePositionAround(world, playerSpawnPos);
            }

            // 使用世界出生点
            BlockPos worldSpawnPos = world.getSpawnPos();

            // 确保Y坐标在合理范围内
            if (worldSpawnPos.getY() < world.getBottomY() + 5) {
                worldSpawnPos = new BlockPos(worldSpawnPos.getX(), 65, worldSpawnPos.getZ());
            } else if (worldSpawnPos.getY() > world.getTopY() - 5) {
                worldSpawnPos = new BlockPos(worldSpawnPos.getX(), world.getTopY() - 10, worldSpawnPos.getZ());
            }

            // 寻找安全位置
            return findSafePositionAround(world, worldSpawnPos);

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
        final BlockPos spawnPos;

        TeleportData(BlockPos spawnPos) {
            this.spawnPos = spawnPos;
        }
    }
}