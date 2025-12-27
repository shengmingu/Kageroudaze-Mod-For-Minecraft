package com.mekakucity.entity.player;

import net.minecraft.nbt.NbtCompound;

public interface PlayerDataStorage {
    NbtCompound getMyModData();
    void setMyModData(NbtCompound nbt);
}