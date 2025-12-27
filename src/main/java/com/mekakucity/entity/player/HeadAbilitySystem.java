package com.mekakucity.entity.player;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeadAbilitySystem {
    private static final Map<UUID, EntityType<?>> PLAYER_HEAD_TYPES = new HashMap<>();

    public static void updatePlayerHeadType(PlayerEntity player) {
        if (player == null) return;

        ItemStack helmet = player.getInventory().getArmorStack(3); // 头盔槽位

        if (!helmet.isEmpty() && helmet.hasNbt()) {
            NbtCompound tag = helmet.getNbt();
            if (tag != null && tag.getBoolean("HeadCollectorItem")) {
                // 获取头颅对应的实体类型
                String entityTypeId = tag.getString("HeadCollectorEntityType");
                if (!entityTypeId.isEmpty()) {
                    try {
                        EntityType<?> entityType = Registries.ENTITY_TYPE.get(new Identifier(entityTypeId));
                        if (entityType != null) {
                            PLAYER_HEAD_TYPES.put(player.getUuid(), entityType);
                            System.out.println("玩家 " + player.getName().getString() + " 穿戴了 " + entityTypeId + " 的头颅");
                            return;
                        }
                    } catch (Exception e) {
                        System.err.println("无效的实体类型ID: " + entityTypeId);
                    }
                }
            }
        }

        // 如果没有穿戴特殊头颅或无法识别类型，移除记录
        PLAYER_HEAD_TYPES.remove(player.getUuid());

    }

    public static boolean shouldIgnorePlayer(MobEntity entity, PlayerEntity player) {
        if (entity == null || player == null) return false;

        EntityType<?> headType = PLAYER_HEAD_TYPES.get(player.getUuid());
        if (headType != null) {
            // 如果生物的类型与玩家穿戴的头颅类型匹配，则忽略玩家
            boolean shouldIgnore = entity.getType().equals(headType);
            if (shouldIgnore) {
                System.out.println("生物 " + entity.getType().getTranslationKey() + " 应该忽略玩家 " + player.getName().getString());
            }
            return shouldIgnore;
        }
        return false;
    }

    public static void clearPlayerData(UUID playerId) {
        if (playerId != null) {
            PLAYER_HEAD_TYPES.remove(playerId);
        }
    }

    // 获取玩家当前穿戴的头颅类型（用于调试）
    public static EntityType<?> getPlayerHeadType(UUID playerId) {
        return PLAYER_HEAD_TYPES.get(playerId);
    }
}