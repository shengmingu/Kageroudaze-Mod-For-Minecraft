package com.mekakucity.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CalendarUsageData {
    private static final Map<UUID, Long> playerUsageTimes = new HashMap<>();

    // 记录玩家使用日历的时间
    public static void recordUsage(ServerPlayerEntity player) {
        playerUsageTimes.put(player.getUuid(), player.getWorld().getTimeOfDay());
    }

    // 检查玩家是否在有效时间内死亡
    public static boolean isValidDeathTime(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        if (!playerUsageTimes.containsKey(playerId)) {
            return false;
        }

        long usageTime = playerUsageTimes.get(playerId);
        long deathTime = player.getWorld().getTimeOfDay();
        long worldTime = player.getWorld().getTime();

        // 计算下一次日出时间
        long nextSunrise = calculateNextSunrise(worldTime);

        // 检查死亡时间是否在使用时间和下一次日出之间
        return deathTime >= usageTime && deathTime <= nextSunrise;
    }

    // 移除玩家的使用记录
    public static void removePlayerRecord(UUID playerId) {
        playerUsageTimes.remove(playerId);
    }

    // 计算下一次日出时间
    public static long calculateNextSunrise(long worldTime) {
        // Minecraft中一天有24000刻，日出大约在0刻
        long currentDay = worldTime / 24000;
        return (currentDay + 1) * 24000; // 返回下一天的0刻（日出时间）
    }
}