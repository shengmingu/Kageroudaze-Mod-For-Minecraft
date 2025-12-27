package com.mekakucity.event;

import com.mekakucity.util.CalendarUsageData;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

import java.util.List;

public class PlayerDeathHandler implements ServerPlayerEvents.AfterRespawn {

    // 自定义物品的标识符 - 请替换为你的实际物品ID
    private static final Identifier MEKAKU_ITEM_ID = new Identifier("kageroudaze", "mekaku");
    private static final Identifier MEWONUSUMU_ITEM_ID = new Identifier("kageroudaze", "mewonusumu");
    private static final Identifier MEWOAZAMUKU_ITEM_ID = new Identifier("kageroudaze", "mewoazamuku");
    private static final Identifier MEOUBAU_ITEM_ID = new Identifier("kageroudaze", "meoubau");

    private static final Identifier MEWOSAMASU_ITEM_ID = new Identifier("kageroudaze", "mewosamasu");
    private static final Identifier MEGASAMERU_ITEM_ID = new Identifier("kageroudaze", "megasameru");
    private static final Identifier MEWOKORASU_ITEM_ID = new Identifier("kageroudaze", "mewokorasu");
    private static final Identifier MEWOKAKERU_ITEM_ID = new Identifier("kageroudaze", "mewokakeru");

    @Override
    public void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        // 获取死亡原因
        DamageSource deathSource = oldPlayer.getRecentDamageSource();
        if (deathSource == null) return;

        // 检查玩家是否有特定物品
        boolean hasMekaku = hasItemInInventory(newPlayer, MEKAKU_ITEM_ID);
        boolean hasMewonusumu = hasItemInInventory(newPlayer, MEWONUSUMU_ITEM_ID);
        boolean hasMeoubau = hasItemInInventory(newPlayer, MEOUBAU_ITEM_ID);

        boolean hasMewosamasu = hasItemInInventory(newPlayer, MEWOSAMASU_ITEM_ID);
        boolean hasMegasameru = hasItemInInventory(newPlayer, MEGASAMERU_ITEM_ID);
        boolean hasMewoazamuku = hasItemInInventory(newPlayer, MEWOAZAMUKU_ITEM_ID);
        boolean hasMewokorasu = hasItemInInventory(newPlayer, MEWOKORASU_ITEM_ID);
        boolean hasMewokakeru = hasItemInInventory(newPlayer, MEWOKAKERU_ITEM_ID);


        // 检查玩家是否使用了日历并在有效时间内死亡
        boolean validCalendarDeath = CalendarUsageData.isValidDeathTime(oldPlayer);
        if(validCalendarDeath) {

            // 根据死亡原因给予物品
            if (isFireDamage(deathSource) && !hasMekaku) {
                // 被烧死给予mekaku道具
                giveItemToPlayer(newPlayer, MEKAKU_ITEM_ID);
            }
            if (isDrowningDamage(deathSource) && !hasMewonusumu) {
                // 检查是否有驯服的狼
                boolean hasTamedWolves = hasTamedWolves(oldPlayer);
                if (hasTamedWolves) {
                    giveItemToPlayer(newPlayer, MEWONUSUMU_ITEM_ID);
                }
            }
            if (isDrowningDamage(deathSource) && !hasMeoubau) {
                giveItemToPlayer(newPlayer, MEOUBAU_ITEM_ID);
            }
            if (isRaidDamage(deathSource) && !hasMewoazamuku) {
                giveItemToPlayer(newPlayer, MEWOAZAMUKU_ITEM_ID);
            }
            if (isWitherDamage(deathSource) && !hasMewosamasu) {
                giveItemToPlayer(newPlayer, MEWOSAMASU_ITEM_ID);
            }
            if (wasPoisoned(oldPlayer) && !hasMegasameru) {
                giveItemToPlayer(newPlayer, MEGASAMERU_ITEM_ID);
            }
            if(!hasMewokorasu){
                boolean hasTamedCats = hasTamedCats(oldPlayer);
                if(hasTamedCats){
                    giveItemToPlayer(newPlayer, MEWOKORASU_ITEM_ID);
                }

            }
            if(!hasMewokakeru&&isFallingDamage(deathSource)){
                giveItemToPlayer(newPlayer, MEWOKAKERU_ITEM_ID);
            }
        }


    }

    // 检查是否是火焰伤害
    private boolean isFireDamage(DamageSource damageSource) {
        // 检查多种火焰伤害类型
        return damageSource.isOf(DamageTypes.IN_FIRE) ||
                damageSource.isOf(DamageTypes.ON_FIRE) ||
                damageSource.isOf(DamageTypes.LAVA) ||
                damageSource.isOf(DamageTypes.HOT_FLOOR);
    }

    // 检查是否是溺水伤害
    private boolean isDrowningDamage(DamageSource damageSource) {
        return damageSource.isOf(DamageTypes.DROWN);
    }
    private boolean isFallingDamage(DamageSource damageSource) {
        return damageSource.isOf(DamageTypes.FALL);
    }
    private boolean isRaidDamage(DamageSource damageSource) {
        Entity attacker = damageSource.getSource();
        if (attacker != null) {

            return attacker instanceof PillagerEntity ||      // 劫掠者
                    attacker instanceof VindicatorEntity ||    // 卫道士
                    attacker instanceof EvokerEntity ||        // 唤魔者
                    attacker instanceof IllusionerEntity ||    // 幻术师
                    attacker instanceof RavagerEntity;         // 劫掠兽
        }
        return false;
    }
    private boolean isWitherDamage(DamageSource damageSource) {
        Entity attacker=damageSource.getSource();
        if(attacker!=null){
            if(attacker instanceof WitherEntity||attacker instanceof WitherSkeletonEntity){
                return true;
            }
        }
        return damageSource.isOf(DamageTypes.WITHER);
    }
    private boolean wasPoisoned(ServerPlayerEntity player) {
        // 获取玩家的中毒效果
        StatusEffectInstance poisonEffect = player.getStatusEffect(StatusEffects.POISON);

        // 如果玩家有中毒效果，返回true
        return poisonEffect != null;
    }

    // 检查玩家物品栏中是否有特定物品
    private boolean hasItemInInventory(ServerPlayerEntity player, Identifier itemId) {
        Item item = Registries.ITEM.get(itemId);
        if (item == null) return false;

        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == item) {
                return true;
            }
        }
        return false;
    }

    // 给予玩家物品
    private void giveItemToPlayer(ServerPlayerEntity player, Identifier itemId) {
        Item item = Registries.ITEM.get(itemId);
        if (item != null) {
            player.getInventory().insertStack(new ItemStack(item));


        }
    }

    // 检查玩家周围是否有驯服的狼
    private boolean hasTamedWolves(ServerPlayerEntity player) {
        // 获取玩家周围10格内的所有狼
        List<WolfEntity> wolves = player.getWorld().getEntitiesByClass(
                WolfEntity.class,
                player.getBoundingBox().expand(10.0),
                EntityPredicates.VALID_ENTITY
        );

        for (WolfEntity wolf : wolves) {
            // 检查狼是否被驯服且主人是该玩家
            if (wolf.isTamed() && wolf.getOwnerUuid() != null &&
                    wolf.getOwnerUuid().equals(player.getUuid())) {
                System.out.println("有狼存在，并是你的宠物");
                return true;
            }
            System.out.println("有狼存在，并非你的宠物");
        }
        return false;
    }
    private boolean hasTamedCats(ServerPlayerEntity player) {
        // 获取玩家周围10格内的所有狼
        List<CatEntity> cats = player.getWorld().getEntitiesByClass(
                CatEntity.class,
                player.getBoundingBox().expand(10.0),
                EntityPredicates.VALID_ENTITY
        );

        for (CatEntity cat : cats) {
            // 检查狼是否被驯服且主人是该玩家
            if (cat.isTamed() && cat.getOwnerUuid() != null &&
                    cat.getOwnerUuid().equals(player.getUuid())) {
                System.out.println("有猫存在，并是你的宠物");
                return true;
            }
            System.out.println("有猫存在，并非你的宠物");
        }
        return false;
    }
}