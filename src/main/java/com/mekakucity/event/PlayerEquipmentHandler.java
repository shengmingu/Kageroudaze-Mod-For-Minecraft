package com.mekakucity.event;

import com.mekakucity.entity.player.HeadAbilitySystem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerEquipmentHandler {
    public static void register() {
        // 监听玩家装备变化
        ServerEntityEvents.EQUIPMENT_CHANGE.register((entity, slot, previous, current) -> {
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                if (slot == EquipmentSlot.HEAD) {
                    HeadAbilitySystem.updatePlayerHeadType(player);
                }
            }
        });

        // 监听玩家加入游戏
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof PlayerEntity) {
                HeadAbilitySystem.updatePlayerHeadType((PlayerEntity) entity);
            }
        });

        // 定期更新玩家装备状态（每秒一次）
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 == 0) { // 每秒一次
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    HeadAbilitySystem.updatePlayerHeadType(player);
                }
            }
        });

        // 监听玩家断开连接
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof PlayerEntity) {
                HeadAbilitySystem.clearPlayerData(entity.getUuid());
            }
        });
    }
}