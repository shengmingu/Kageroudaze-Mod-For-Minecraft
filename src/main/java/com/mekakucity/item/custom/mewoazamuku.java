package com.mekakucity.item.custom;

import com.mekakucity.entity.player.HeadAbilitySystem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;

import java.util.UUID;

public class mewoazamuku extends Item {

    public mewoazamuku(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        World world = user.getWorld();

        if (!world.isClient) {
            // 获取实体头颅
            ItemStack headStack = getHeadForEntity(entity);

            // 确保头颅被正确标记
            markHeadAsUnplaceable(headStack, entity.getType());

            // 添加到玩家库存
            if (!user.getInventory().insertStack(headStack)) {
                // 如果库存已满，掉落在地上
                user.dropItem(headStack, false);
            }

            // 发送反馈消息
            user.sendMessage(Text.translatable("目欺开始", entity.getName()), true);

            // 立即更新玩家装备状态
            HeadAbilitySystem.updatePlayerHeadType(user);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && user.isSneaking()) {
            // 当潜行右键时，删除所有收集的头颅
            int removed = removeCollectedHeads(user);
            user.sendMessage(Text.translatable("item.kageroudaze.mewoazamuku.over", removed), true);

            // 立即更新玩家装备状态
            HeadAbilitySystem.updatePlayerHeadType(user);

            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }

    private ItemStack getHeadForEntity(LivingEntity entity) {
        ItemStack headStack;

        if (entity instanceof PlayerEntity) {
            // 玩家头颅
            headStack = new ItemStack(net.minecraft.item.Items.PLAYER_HEAD);
            NbtCompound nbt = new NbtCompound();
            nbt.putString("SkullOwner", entity.getName().getString());
            headStack.setNbt(nbt);
        } else {
            // 生物头颅 - 这里需要根据实体类型返回对应的头颅
            headStack = getMobHead(entity.getType());
        }

        // 设置自定义名称
        headStack.setCustomName(Text.translatable("item.kageroudaze.mewoazamuku.start", entity.getName()));

        return headStack;
    }

    private void markHeadAsUnplaceable(ItemStack headStack, EntityType<?> entityType) {
        // 添加标记以便识别这是通过此物品获得的头颅
        NbtCompound tag = headStack.getOrCreateNbt();
        tag.putBoolean("HeadCollectorItem", true);
        tag.putUuid("CollectorUUID", UUID.randomUUID());
        tag.putBoolean("CannotBePlaced", true); // 标记表示不能放置

        // 记录实体类型以便后续使用
        Identifier entityId = Registries.ENTITY_TYPE.getId(entityType);
        if (entityId != null) {
            tag.putString("HeadCollectorEntityType", entityId.toString());
            System.out.println("标记头颅: " + entityId.toString());
        }
    }

    private ItemStack getMobHead(EntityType<?> entityType) {
        // 这里可以扩展更多生物的头颅
        if (entityType == EntityType.ZOMBIE) return new ItemStack(net.minecraft.item.Items.ZOMBIE_HEAD);
        if (entityType == EntityType.SKELETON) return new ItemStack(net.minecraft.item.Items.SKELETON_SKULL);
        if (entityType == EntityType.CREEPER) return new ItemStack(net.minecraft.item.Items.CREEPER_HEAD);
        if (entityType == EntityType.ENDER_DRAGON) return new ItemStack(net.minecraft.item.Items.DRAGON_HEAD);
        if (entityType == EntityType.WITHER_SKELETON) return new ItemStack(net.minecraft.item.Items.WITHER_SKELETON_SKULL);
        if (entityType == EntityType.PIGLIN) return new ItemStack(net.minecraft.item.Items.PIGLIN_HEAD);

        // 默认返回玩家头颅（可以自定义为未知生物头颅）
        ItemStack head = new ItemStack(net.minecraft.item.Items.PLAYER_HEAD);
        // 为未知生物头颅设置特殊纹理
        NbtCompound nbt = new NbtCompound();
        nbt.putString("SkullOwner", "MHF_Question");
        head.setNbt(nbt);
        return head;
    }

    private int removeCollectedHeads(PlayerEntity player) {
        int count = 0;

        // 检查主手和副手
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (isCollectedHead(stack)) {
                player.setStackInHand(hand, ItemStack.EMPTY);
                count++;
            }
        }

        // 检查物品栏
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (isCollectedHead(stack)) {
                player.getInventory().setStack(i, ItemStack.EMPTY);
                count++;
            }
        }

        // 检查装备栏（包括头盔）
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack stack = player.getInventory().armor.get(i);
            if (isCollectedHead(stack)) {
                player.getInventory().armor.set(i, ItemStack.EMPTY);
                count++;
            }
        }

        return count;
    }

    private boolean isCollectedHead(ItemStack stack) {
        if (stack.isEmpty()) return false;

        NbtCompound tag = stack.getNbt();
        return tag != null && tag.getBoolean("HeadCollectorItem");
    }

    // 添加工具提示
    @Override
    public void appendTooltip(ItemStack stack, World world, java.util.List<Text> tooltip, net.minecraft.client.item.TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.kageroudaze.mewoazamuku.text").formatted(Formatting.BLACK));

    }
}