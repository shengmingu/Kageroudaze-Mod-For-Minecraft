package com.mekakucity.item.custom;

import com.mekakucity.util.CalendarUsageData;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class calendar extends Item {

    // 自定义维度的ID
    private static final Identifier CUSTOM_DIMENSION_ID =
            new Identifier("kageroudaze", "kageroudazeworld_dimension");

    public calendar(Settings settings) {
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

            if (currentDimension.equals(customDimensionKey)) {
                // 在自定义维度中，禁止使用日历
                player.sendMessage(Text.translatable("item.kageroudaze.calendar.notion"), false);
                return TypedActionResult.fail(stack);
            }

            // 正常使用逻辑
            stack.decrement(1);

            // 只记录使用时间，不处理死亡逻辑
            CalendarUsageData.recordUsage(player);

            // 计算下一次日出时间
            long nextSunrise = CalendarUsageData.calculateNextSunrise(player.getWorld().getTime());
            long timeUntilSunrise = nextSunrise - player.getWorld().getTimeOfDay();

            // 发送使用成功的消息
            player.sendMessage(Text.translatable("item.kageroudaze.calendar.start").formatted(Formatting.BLUE));
            player.sendMessage(Text.translatable("item.kageroudaze.calendar.time"), false);
            player.sendMessage(Text.translatable(formatTicksToTime(timeUntilSunrise)), false);

            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }

    // 格式化刻为可读时间
    private String formatTicksToTime(long ticks) {
        long seconds = ticks / 20;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.kageroudaze.calendar.text").formatted(Formatting.WHITE));

    }
}