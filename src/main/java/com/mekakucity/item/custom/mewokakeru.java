package com.mekakucity.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class mewokakeru extends Item {

    private static final double RANGE = 4.5; // 最大使用距离

    public mewokakeru(Settings settings) {
        super(settings.maxCount(1).maxDamage(0));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // 检查冷却时间
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }

        // 进行射线检测，寻找玩家准星指向的实体
        EntityHitResult hitResult = raycastEntity(world, user);

        if (hitResult != null && hitResult.getEntity() instanceof PlayerEntity target) {
            // 检查是否为目标自己
            if (target == user) {
                return TypedActionResult.fail(stack);
            }

            // 设置7秒冷却时间 (20 ticks/秒 * 7秒 = 140 ticks)
            user.getItemCooldownManager().set(this, 140);

            // 只在服务器端应用效果
            if (!world.isClient) {
                applyEffects(target);


            }

            // 成功使用
            return TypedActionResult.success(stack, world.isClient());
        }

        // 未指向玩家，使用失败
        return TypedActionResult.fail(stack);
    }

    // 射线检测方法
    private EntityHitResult raycastEntity(World world, PlayerEntity player) {
        // 计算起点和终点
        Vec3d start = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0F);
        Vec3d end = start.add(look.multiply(RANGE));

        // 进行方块射线检测（防止穿墙）
        HitResult blockHit = world.raycast(new RaycastContext(
                start, end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                player
        ));

        double maxDistance = RANGE;
        if (blockHit.getType() != HitResult.Type.MISS) {
            maxDistance = blockHit.getPos().distanceTo(start);
        }

        // 实体射线检测
        EntityHitResult entityHit = raycastEntities(player, start, end, maxDistance);

        return entityHit;
    }

    // 实体射线检测
    private EntityHitResult raycastEntities(PlayerEntity player, Vec3d start, Vec3d end, double maxDistance) {
        EntityHitResult closestHit = null;
        double closestDistance = maxDistance;

        // 创建一个搜索边界框，从起点扩展到终点
        Box searchBox = new Box(start, end).expand(1.0, 1.0, 1.0);

        // 遍历边界框内的所有实体
        for (LivingEntity entity : player.getWorld().getEntitiesByClass(
                LivingEntity.class,
                searchBox,
                e -> e != player && e instanceof PlayerEntity
        )) {
            // 检查实体是否在视线内
            if (entity instanceof PlayerEntity target) {
                // 计算实体边界箱
                Box box = entity.getBoundingBox();

                // 检查射线与边界箱的相交
                Vec3d intersection = box.raycast(start, end).orElse(null);
                if (intersection != null) {
                    double distance = start.distanceTo(intersection);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestHit = new EntityHitResult(entity);
                    }
                }
            }
        }

        return closestHit;
    }

    // 应用效果的方法
    private void applyEffects(PlayerEntity target) {
        // 生命恢复 - 3秒 (60 ticks)
        target.addStatusEffect(new StatusEffectInstance(
                StatusEffects.REGENERATION,
                60,    // 3秒
                0,     // 等级 I
                true,  // 显示粒子
                true   // 显示图标
        ));

        // 抗性提升 - 3秒
        target.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE,
                60,    // 3秒
                0,     // 等级 I
                true,
                true
        ));

        // 力量 - 5秒 (100 ticks)，等级 V
        target.addStatusEffect(new StatusEffectInstance(
                StatusEffects.STRENGTH,
                100,   // 5秒
                4,     // 等级 V (力量V)
                true,
                true
        ));

        // 可选：添加粒子效果
        spawnParticles(target);
    }

    // 生成粒子效果（可选）
    private void spawnParticles(PlayerEntity target) {
        World world = target.getWorld();
        if (world.isClient) return;

        // 在玩家周围生成心形粒子
        for (int i = 0; i < 10; i++) {
            double x = target.getX() + (world.random.nextDouble() - 0.5) * 2;
            double y = target.getY() + 1.0 + world.random.nextDouble();
            double z = target.getZ() + (world.random.nextDouble() - 0.5) * 2;

            // 发送粒子数据包（在服务器端）
            ((net.minecraft.server.world.ServerWorld) world).spawnParticles(
                    net.minecraft.particle.ParticleTypes.HEART,
                    x, y, z,
                    1, 0, 0, 0, 0
            );
        }

        // 生成力量粒子
        for (int i = 0; i < 5; i++) {
            double x = target.getX() + (world.random.nextDouble() - 0.5) * 2;
            double y = target.getY() + world.random.nextDouble() * 2;
            double z = target.getZ() + (world.random.nextDouble() - 0.5) * 2;

            ((net.minecraft.server.world.ServerWorld) world).spawnParticles(
                    net.minecraft.particle.ParticleTypes.CRIT,
                    x, y, z,
                    3, 0.1, 0.1, 0.1, 0.05
            );
        }
    }

    // 添加工具提示
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.kageroudaze.mewokakeru.text").formatted(Formatting.RED));

    }
}