package com.mekakucity.util;

import com.mekakucity.entity.player.PlayerDataStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public class PlayerDataHelper {
    public static NbtCompound getMyModData(PlayerEntity player) {
        if (player instanceof PlayerDataStorage) {
            return ((PlayerDataStorage) player).getMyModData();
        }
        return new NbtCompound();
    }

    public static void setMyModData(PlayerEntity player, NbtCompound nbt) {
        if (player instanceof PlayerDataStorage) {
            ((PlayerDataStorage) player).setMyModData(nbt);
        }
    }

    public static boolean getBoolean(PlayerEntity player, String key, boolean defaultValue) {
        NbtCompound nbt = getMyModData(player);
        return nbt.contains(key) ? nbt.getBoolean(key) : defaultValue;
    }

    public static void setBoolean(PlayerEntity player, String key, boolean value) {
        NbtCompound nbt = getMyModData(player);
        nbt.putBoolean(key, value);
        setMyModData(player, nbt);
    }

    public static int getInt(PlayerEntity player, String key, int defaultValue) {
        NbtCompound nbt = getMyModData(player);
        return nbt.contains(key) ? nbt.getInt(key) : defaultValue;
    }

    public static void setInt(PlayerEntity player, String key, int value) {
        NbtCompound nbt = getMyModData(player);
        nbt.putInt(key, value);
        setMyModData(player, nbt);
    }

    public static float getFloat(PlayerEntity player, String key, float defaultValue) {
        NbtCompound nbt = getMyModData(player);
        return nbt.contains(key) ? nbt.getFloat(key) : defaultValue;
    }

    public static void setFloat(PlayerEntity player, String key, float value) {
        NbtCompound nbt = getMyModData(player);
        nbt.putFloat(key, value);
        setMyModData(player, nbt);
    }

    public static String getString(PlayerEntity player, String key, String defaultValue) {
        NbtCompound nbt = getMyModData(player);
        return nbt.contains(key) ? nbt.getString(key) : defaultValue;
    }

    public static void setString(PlayerEntity player, String key, String value) {
        NbtCompound nbt = getMyModData(player);
        nbt.putString(key, value);
        setMyModData(player, nbt);
    }
}