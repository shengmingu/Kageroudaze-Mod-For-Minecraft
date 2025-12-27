package com.mekakucity.item.custom;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class megasaeru extends Item {

    // 自定义维度的ID
    private static final Identifier CUSTOM_DIMENSION_ID =
            new Identifier("kageroudaze", "kageroudazeworld_dimension");

    // Boss实体的ID
    private static final Identifier BOSS_ENTITY_ID =
            new Identifier("kageroudaze", "kuroha");

    // 成就ID
    private static final Identifier ADVANCEMENT_ID =
            new Identifier("kageroudaze", "outer");

    public megasaeru(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && user instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) user;

            // 检查是否在自定义维度
            RegistryKey<World> currentDimension = player.getWorld().getRegistryKey();
            RegistryKey<World> customDimensionKey = RegistryKey.of(RegistryKeys.WORLD, CUSTOM_DIMENSION_ID);

            // 如果不是在自定义维度，禁止使用
            if (!currentDimension.equals(customDimensionKey)) {

                return TypedActionResult.fail(stack);
            }

            // 在自定义维度中，开始召唤Boss
            ServerWorld serverWorld = (ServerWorld) world;

            try {
                // 获取Boss实体类型
                EntityType<?> bossType = EntityType.get(BOSS_ENTITY_ID.toString()).orElse(null);



                if (bossType != null) {
                    // 计算生成位置（在玩家前方3-5格处）
                    Vec3d playerPos = player.getPos();
                    Vec3d lookVec = player.getRotationVec(1.0F);

                    // 基础生成距离
                    double spawnDistance = 4.0;
                    double spawnX = playerPos.x + lookVec.x * spawnDistance;
                    double spawnY = playerPos.y + 1.0;
                    double spawnZ = playerPos.z + lookVec.z * spawnDistance;

                    BlockPos spawnPos = new BlockPos((int)spawnX, (int)spawnY, (int)spawnZ);

                    // 寻找合适的生成位置
                    spawnPos = findSuitableSpawnPosition(serverWorld, spawnPos);

                    // 创建并生成Boss实体
                    Entity bossEntity = bossType.create(serverWorld);

                    if (bossEntity != null) {
                        bossEntity.refreshPositionAndAngles(
                                spawnPos.getX() + 0.5,
                                spawnPos.getY(),
                                spawnPos.getZ() + 0.5,
                                player.getYaw(),
                                0
                        );

                        serverWorld.spawnEntity(bossEntity);

                        // 播放召唤音效
                        serverWorld.playSound(null, spawnPos,
                                net.minecraft.sound.SoundEvents.ENTITY_WITHER_SPAWN,
                                net.minecraft.sound.SoundCategory.HOSTILE, 1.0F, 1.0F);

                        // 添加粒子效果（可选）
                        for (int i = 0; i < 20; i++) {
                            serverWorld.spawnParticles(
                                    net.minecraft.particle.ParticleTypes.PORTAL,
                                    spawnPos.getX() + 0.5,
                                    spawnPos.getY() + 1.5,
                                    spawnPos.getZ() + 0.5,
                                    5,
                                    0.5, 0.5, 0.5,
                                    0.05
                            );
                        }



                        // 消耗道具（除非在创造模式）
                        ItemStack newStack = stack.copy();
                        if (!player.isCreative()) {
                            stack.decrement(1);
                            newStack = stack; // 使用消耗后的堆栈
                        }
                        grantAdvancement(player);



                        return TypedActionResult.success(newStack, world.isClient);
                    } else {

                        return TypedActionResult.fail(stack);
                    }
                }
            } catch (Exception e) {

                return TypedActionResult.fail(stack);
            }
        }

        // 客户端端返回成功，播放动画
        return TypedActionResult.success(stack, world.isClient);
    }

    /**
     * 寻找合适的生成位置
     */
    private BlockPos findSuitableSpawnPosition(ServerWorld world, BlockPos originalPos) {
        // 检查当前位置是否合适
        if (isSuitableSpawnPosition(world, originalPos)) {
            return originalPos;
        }

        // 尝试在当前位置周围寻找合适的位置
        for (int y = 0; y <= 3; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos checkPos = originalPos.add(x, y, z);
                    if (isSuitableSpawnPosition(world, checkPos)) {
                        return checkPos;
                    }
                }
            }
        }

        // 如果都找不到，返回原位置上方3格
        return originalPos.up(3);
    }

    /**
     * 检查位置是否适合生成Boss
     */
    private boolean isSuitableSpawnPosition(ServerWorld world, BlockPos pos) {
        // 检查当前位置和上方2格是否有足够的空间
        return world.getBlockState(pos).isAir() &&
                world.getBlockState(pos.up()).isAir() &&
                world.getBlockState(pos.up(2)).isAir();
    }
    /**
     * 授予成就
     */
    private void grantAdvancement(ServerPlayerEntity player) {
        try {
            // 获取成就管理器
            ServerAdvancementLoader advancementManager = player.server.getAdvancementLoader();

            // 获取成就对象
            Advancement advancement = advancementManager.get(ADVANCEMENT_ID);

            if (advancement != null) {
                // 检查是否已经获得该成就
                AdvancementProgress progress =
                        player.getAdvancementTracker().getProgress(advancement);

                if (!progress.isDone()) {
                    // 授予成就（使用我们在JSON中定义的criteria名称"triggered"）
                    player.getAdvancementTracker().grantCriterion(advancement, "triggered");



                }
            } else {
                // 成就未找到，输出错误信息
                System.err.println("成就未找到: " + ADVANCEMENT_ID);

            }
        } catch (Exception e) {
            System.err.println("授予成就时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        // 保持原有的tooltip文本
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.kageroudaze.megasaeru.text").formatted(Formatting.YELLOW));


    }
}