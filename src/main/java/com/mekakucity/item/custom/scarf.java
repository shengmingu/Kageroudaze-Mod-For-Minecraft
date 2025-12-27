package com.mekakucity.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class scarf extends Item {
    public scarf(Settings settings) {
        super(settings);
    }
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.kageroudaze.scarf.text").formatted(Formatting.RED));
        tooltip.add(Text.translatable("item.kageroudaze.scarf.info").formatted(Formatting.WHITE));
        tooltip.add(Text.translatable("item.kageroudaze.scarf.red").formatted(Formatting.RED));

    }
}
