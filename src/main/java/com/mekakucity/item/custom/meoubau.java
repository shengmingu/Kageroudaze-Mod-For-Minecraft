package com.mekakucity.item.custom;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class meoubau extends Item {
    // 存储玩家及其控制的实体列表
    private static final Map<PlayerEntity, List<EntityData>> ACTIVE_CONTROLS = new WeakHashMap<>();
    private static boolean eventRegistered = false;

    public meoubau(Settings settings) {
        super(settings);
        if (!eventRegistered) {
            registerTickEvent();
            eventRegistered = true;
        }
    }
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.kageroudaze.meoubau.text").formatted(Formatting.GOLD));

    }

    private static void registerTickEvent() {
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            if (ACTIVE_CONTROLS.isEmpty()) return;

            // 遍历所有玩家及其控制的实体
            Iterator<Map.Entry<PlayerEntity, List<EntityData>>> iterator = ACTIVE_CONTROLS.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<PlayerEntity, List<EntityData>> entry = iterator.next();
                PlayerEntity player = entry.getKey();

                // 如果玩家无效，移除记录
                if (player == null || !player.isAlive()) {
                    iterator.remove();
                    continue;
                }

                // 更新玩家控制的每个实体
                Vec3d playerPos = player.getPos();
                for (EntityData entityData : entry.getValue()) {
                    LivingEntity entity = entityData.entity;

                    // 检查实体是否有效且在范围内
                    if (entity != null && entity.isAlive() && entity.getPos().isInRange(playerPos, 100.0)) {
                        forceLookAtPlayer(player, entity);
                    }
                }
            }
        });
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            // 检查玩家是否已激活控制
            boolean isActive = ACTIVE_CONTROLS.containsKey(user);

            if (!isActive) {
                // 激活控制模式
                Vec3d userPos = user.getPos();
                Box area = new Box(
                        userPos.add(-100, -100, -100),
                        userPos.add(100, 100, 100)
                );

                // 收集玩家周围100格内的实体
                List<EntityData> controlledEntities = new ArrayList<>();
                for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, area, e ->
                        e != user && !isEntityControlled(e)
                )) {
                    // 保存原始角度数据并立即看向玩家
                    EntityData data = new EntityData(
                            entity,
                            entity.getHeadYaw(),
                            entity.getPitch(),
                            entity.getBodyYaw()
                    );
                    controlledEntities.add(data);
                    forceLookAtPlayer(user, entity);
                }

                if (!controlledEntities.isEmpty()) {
                    ACTIVE_CONTROLS.put(user, controlledEntities);
                    user.sendMessage(Text.translatable("item.kageroudaze.meoubau.start"), true);
                }
            } else {
                // 解除控制模式
                List<EntityData> controlledEntities = ACTIVE_CONTROLS.remove(user);
                for (EntityData data : controlledEntities) {
                    if (data.entity != null && data.entity.isAlive()) {
                        // 恢复实体原始角度
                        data.entity.setHeadYaw(data.headYaw);
                        data.entity.setBodyYaw(data.bodyYaw);
                        data.entity.setPitch(data.pitch);
                    }
                }
                user.sendMessage(Text.translatable("item.kageroudaze.meoubau.over"), true);
            }
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    // 强制实体看向玩家
    private static void forceLookAtPlayer(PlayerEntity player, LivingEntity entity) {
        Vec3d playerEye = player.getEyePos();
        Vec3d entityEye = entity.getEyePos();
        Vec3d direction = playerEye.subtract(entityEye).normalize();

        // 计算需要看向的方向
        double pitchRad = Math.asin(direction.y);
        double yawRad = Math.atan2(direction.z, direction.x);

        // 设置实体的方向
        entity.setPitch((float) Math.toDegrees(pitchRad));
        entity.setHeadYaw((float) (Math.toDegrees(yawRad) - 90));
    }

    // 检查实体是否已被控制
    private static boolean isEntityControlled(LivingEntity entity) {
        for (List<EntityData> entities : ACTIVE_CONTROLS.values()) {
            for (EntityData data : entities) {
                if (data.entity == entity) {
                    return true;
                }
            }
        }
        return false;
    }

    // 存储实体及其原始角度数据
    private static class EntityData {
        final LivingEntity entity;
        final float headYaw;
        final float pitch;
        final float bodyYaw;

        EntityData(LivingEntity entity, float headYaw, float pitch, float bodyYaw) {
            this.entity = entity;
            this.headYaw = headYaw;
            this.pitch = pitch;
            this.bodyYaw = bodyYaw;
        }
    }
}