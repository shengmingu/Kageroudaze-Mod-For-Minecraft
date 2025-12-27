package com.mekakucity.event;

import com.mekakucity.util.CalendarUsageData;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import net.minecraft.predicate.entity.EntityPredicates;

import java.util.List;

public class SafeDeathTeleporter implements ServerPlayerEvents.AfterRespawn {

    // 自定义维度的ID
    private static final Identifier CUSTOM_DIMENSION_ID = new Identifier("kageroudaze", "kageroudazeworld_dimension");

    @Override
    public void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        // 延迟2个tick执行，确保玩家完全重生
        scheduleTask(newPlayer.getServer(), () -> {
            try {
                processDeathTeleport(oldPlayer, newPlayer);
            } catch (Exception e) {
                System.err.println("Error processing death teleport: " + e.getMessage());
                e.printStackTrace();
            }
        }, 2);
    }

    // 安全地调度任务
    private void scheduleTask(net.minecraft.server.MinecraftServer server, Runnable task, int ticksDelay) {
        if (server != null) {
            for (int i = 0; i < ticksDelay; i++) {
                server.execute(task);
            }
        }
    }

    private void processDeathTeleport(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer) {
        // 获取死亡原因
        DamageSource deathSource = oldPlayer.getRecentDamageSource();
        if (deathSource == null) return;

        // 检查玩家是否使用了日历并在有效时间内
        boolean calendarValid = CalendarUsageData.isValidDeathTime(oldPlayer);

        if (!calendarValid) {
            return; // 没有有效使用日历，不触发任何传送
        }

        // 条件A：使用日历后摔死（无论是否养猫）
        boolean isFallDamage = deathSource.isOf(DamageTypes.FALL);
        boolean conditionA = isFallDamage; // 使用日历后摔死

        // 条件B：养有猫并使用日历后以任意方式死亡
        boolean hasTamedCat = hasTamedCat(oldPlayer);
        boolean conditionB = hasTamedCat; // 养有猫并使用日历后死亡（任意方式）

        // 两个条件满足其中一个即可触发传送
        if (conditionA || conditionB) {
            // 获取自定义维度
            RegistryKey<World> customDimensionKey = RegistryKey.of(
                    RegistryKeys.WORLD,
                    CUSTOM_DIMENSION_ID
            );

            ServerWorld customWorld = newPlayer.getServer().getWorld(customDimensionKey);

            if (customWorld != null) {
                // 安全地查找重生位置
                BlockPos spawnPos = findSpawnPositionSafe(customWorld);

                // 使用安全的传送方法
                safeTeleportToWorld(newPlayer, customWorld, spawnPos);

                // 根据触发条件发送不同消息
                if (conditionA && conditionB) {
                    // 同时满足两个条件（养猫并使用日历后摔死）
                    newPlayer.sendMessage(Text.translatable("message.kageroudaze.death.teleport.reason1"), false);
                } else if (conditionA) {
                    // 仅满足条件A（使用日历后摔死）
                    newPlayer.sendMessage(Text.translatable("message.kageroudaze.death.teleport.reason2"), false);
                } else if (conditionB) {
                    // 仅满足条件B（养有猫并使用日历后任意方式死亡）
                    newPlayer.sendMessage(Text.translatable("message.kageroudaze.death.teleport.reason3"), false);
                }

                // 清除日历使用记录
                CalendarUsageData.removePlayerRecord(newPlayer.getUuid());
            }
        }
    }

    // 检查玩家是否驯养有猫
    private boolean hasTamedCat(ServerPlayerEntity player) {
        try {
            // 获取玩家所在世界的所有猫实体
            List<CatEntity> cats = player.getWorld().getEntitiesByClass(
                    CatEntity.class,
                    player.getBoundingBox().expand(50.0), // 搜索50格范围内的猫
                    EntityPredicates.VALID_ENTITY
            );

            for (CatEntity cat : cats) {
                // 检查猫是否被驯服且主人是该玩家
                if (cat.isTamed() && cat.getOwnerUuid() != null &&
                        cat.getOwnerUuid().equals(player.getUuid())) {
                    System.out.println("玩家 " + player.getName().getString() + " 拥有驯服的猫: " + cat.getName().getString());
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            System.err.println("Error checking for tamed cats: " + e.getMessage());
            return false;
        }
    }

    // 安全地查找重生位置
    private BlockPos findSpawnPositionSafe(ServerWorld world) {
        try {
            // 使用世界出生点
            BlockPos spawnPos = world.getSpawnPos();

            // 确保Y坐标在合理范围内
            if (spawnPos.getY() < world.getBottomY() + 5) {
                spawnPos = new BlockPos(spawnPos.getX(), 65, spawnPos.getZ());
            } else if (spawnPos.getY() > world.getTopY() - 5) {
                spawnPos = new BlockPos(spawnPos.getX(), world.getTopY() - 10, spawnPos.getZ());
            }

            // 检查并调整到安全位置
            spawnPos = adjustToSafePosition(world, spawnPos);

            return spawnPos;
        } catch (Exception e) {
            // 如果出错，返回默认安全位置
            return new BlockPos(0, 65, 0);
        }
    }

    // 调整到安全位置
    private BlockPos adjustToSafePosition(ServerWorld world, BlockPos startPos) {
        // 从当前位置向下寻找地面
        for (int y = Math.min(startPos.getY(), world.getTopY() - 2); y > world.getBottomY() + 1; y--) {
            BlockPos checkPos = new BlockPos(startPos.getX(), y, startPos.getZ());

            // 检查脚下是否有固体方块
            if (world.getBlockState(checkPos.down()).isSolid()) {
                // 检查上方是否有足够空间
                if (world.getBlockState(checkPos).isAir() &&
                        world.getBlockState(checkPos.up()).isAir() &&
                        world.getBlockState(checkPos.up(2)).isAir()) {
                    return checkPos;
                }
            }
        }

        // 如果找不到，返回原始位置
        return startPos;
    }

    // 安全地传送玩家到另一个世界
    private void safeTeleportToWorld(ServerPlayerEntity player, ServerWorld targetWorld, BlockPos targetPos) {
        try {
            // 检查玩家是否已经断开连接
            if (player.isDisconnected()) {
                return;
            }

            // 保存玩家数据
            float yaw = player.getYaw();
            float pitch = player.getPitch();

            // 获取当前世界
            ServerWorld currentWorld = (ServerWorld) player.getWorld();

            // 如果已经在目标世界，直接传送
            if (currentWorld.getRegistryKey().equals(targetWorld.getRegistryKey())) {
                teleportInSameWorld(player, targetWorld, targetPos, yaw, pitch);
            } else {
                // 跨维度传送
                teleportAcrossDimensions(player, targetWorld, targetPos, yaw, pitch);
            }

            // 延迟设置重生点，避免与传送冲突
            scheduleTask(player.getServer(), () -> {
                setRespawnPointSafely(player, targetWorld, targetPos);
            }, 1);

        } catch (Exception e) {
            System.err.println("Error in safeTeleportToWorld: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 在同一世界内传送
    private void teleportInSameWorld(ServerPlayerEntity player, ServerWorld world, BlockPos pos, float yaw, float pitch) {
        try {
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

            // 给予短暂无敌时间
            player.timeUntilRegen = 40;
        } catch (Exception e) {
            System.err.println("Error teleporting in same world: " + e.getMessage());
        }
    }

    // 跨维度传送 - 使用更安全的方法
    private void teleportAcrossDimensions(ServerPlayerEntity player, ServerWorld targetWorld,
                                          BlockPos targetPos, float yaw, float pitch) {
        try {
            // 使用服务器线程安全地传送
            player.getServer().execute(() -> {
                try {
                    // 直接使用teleport方法进行跨维度传送
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

                    // 给予短暂无敌时间
                    player.timeUntilRegen = 40;

                } catch (Exception e) {
                    System.err.println("Error in cross-dimension teleport: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("Error scheduling cross-dimension teleport: " + e.getMessage());
        }
    }

    // 安全地设置重生点
    private void setRespawnPointSafely(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        try {
            // 检查玩家是否还在线
            if (player.isDisconnected()) {
                return;
            }

            // 设置重生点
            player.setSpawnPoint(
                    world.getRegistryKey(),
                    pos,
                    0.0f,
                    true,
                    false
            );

            // 通知玩家


            System.out.println("成功为玩家 " + player.getName().getString() +
                    " 在维度 " + world.getRegistryKey().getValue() +
                    " 设置重生点于 " + pos);

        } catch (Exception e) {
            System.err.println("Error setting respawn point: " + e.getMessage());
        }
    }
}