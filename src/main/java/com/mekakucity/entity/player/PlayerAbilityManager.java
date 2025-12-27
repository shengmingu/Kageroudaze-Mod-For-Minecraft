package com.mekakucity.entity.player;

import com.mekakucity.util.PlayerDataHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerAbilityManager {
    private static final Set<UUID> playersWithAbility = new HashSet<>();

    public static boolean toggleAbility(PlayerEntity player) {
        UUID playerId = player.getUuid();

        if (playersWithAbility.contains(playerId)) {
            // 禁用能力
            disableAbility(player);
            playersWithAbility.remove(playerId);
            return false;
        } else {
            // 启用能力
            enableAbility(player);
            playersWithAbility.add(playerId);
            return true;
        }
    }

    private static void enableAbility(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            // 存储原始飞行能力状态
            boolean couldFly = serverPlayer.getAbilities().allowFlying;
            boolean wasFlying = serverPlayer.getAbilities().flying;

            // 使用自定义数据存储原始飞行状态
            PlayerDataHelper.setBoolean(player, "hadFlight", couldFly);
            PlayerDataHelper.setBoolean(player, "wasFlying", wasFlying);

            // 给予效果
            serverPlayer.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
            serverPlayer.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SATURATION, Integer.MAX_VALUE, 0, false, false, false));
            serverPlayer.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.RESISTANCE, Integer.MAX_VALUE, 255, false, false, false));
            serverPlayer.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false, false));
            serverPlayer.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOW_FALLING, Integer.MAX_VALUE, 0, false, false, false));

            // 允许飞行
            serverPlayer.getAbilities().allowFlying = true;
            serverPlayer.getAbilities().flying = true;
            serverPlayer.sendAbilitiesUpdate();
        }
    }

    private static void disableAbility(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            // 移除效果
            serverPlayer.removeStatusEffect(StatusEffects.INVISIBILITY);
            serverPlayer.removeStatusEffect(StatusEffects.SATURATION);
            serverPlayer.removeStatusEffect(StatusEffects.RESISTANCE);
            serverPlayer.removeStatusEffect(StatusEffects.FIRE_RESISTANCE);
            serverPlayer.removeStatusEffect(StatusEffects.SLOW_FALLING);

            // 从自定义数据中读取原始飞行状态
            boolean hadFlight = PlayerDataHelper.getBoolean(player, "hadFlight", false);
            boolean wasFlying = PlayerDataHelper.getBoolean(player, "wasFlying", false);

            // 恢复飞行能力到之前的状态
            serverPlayer.getAbilities().allowFlying = hadFlight;
            serverPlayer.getAbilities().flying = wasFlying;
            serverPlayer.sendAbilitiesUpdate();

            // 清除自定义数据
            PlayerDataHelper.setBoolean(player, "hadFlight", false);
            PlayerDataHelper.setBoolean(player, "wasFlying", false);
        }
    }

    public static boolean hasAbility(PlayerEntity player) {
        return playersWithAbility.contains(player.getUuid());
    }
}