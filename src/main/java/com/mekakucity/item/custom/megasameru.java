package com.mekakucity.item.custom;

import com.mekakucity.entity.player.PlayerAbilityManager;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class megasameru extends Item {
    public megasameru(Settings settings) {
        super(settings);
    }
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.kageroudaze.megasameru.text").formatted(Formatting.BLUE));

    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            // 切换玩家能力状态
            boolean isNowActive = PlayerAbilityManager.toggleAbility(player);

            // 根据状态发送消息
            if (isNowActive) {
                player.sendMessage(Text.translatable("item.kageroudaze.megasameru.use").formatted(Formatting.BLUE), false);
            } else {
                player.sendMessage(Text.translatable("item.kageroudaze.megasameru.cancel").formatted(Formatting.YELLOW), false);
            }

            // 添加冷却时间防止连续触发
            player.getItemCooldownManager().set(this, 20);
        }

        return TypedActionResult.success(player.getStackInHand(hand));
    }
}