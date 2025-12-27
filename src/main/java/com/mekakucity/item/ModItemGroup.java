package com.mekakucity.item;


import com.mekakucity.Kageroudaze;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroup {

    public static final ItemGroup Kagerou_Daze= Registry.register(Registries.ITEM_GROUP,new Identifier(Kageroudaze.MOD_ID,"kageroudaze_group"), FabricItemGroup.builder().displayName(Text.translatable("itemGroup.kageroudaze_group")).icon(()->new ItemStack(ModItems.mewokakeru)).entries((displayContext, entries) -> {
        entries.add(ModItems.mekaku);
        entries.add(ModItems.mewonusumu);
        entries.add(ModItems.mewoazamuku);
        entries.add(ModItems.mewoawaseru);
        entries.add(ModItems.meoubau);
        entries.add(ModItems.megasameru);
        entries.add(ModItems.mewokorasu);
        entries.add(ModItems.mewosamasu);
        entries.add(ModItems.megasaeru);
        entries.add(ModItems.mewokakeru);
        entries.add(ModItems.meniyakitsukeru);
        entries.add(ModItems.phone);
        entries.add(ModItems.scarf);
        entries.add(ModItems.scissor);
        entries.add(ModItems.ouroboros);
        entries.add(ModItems.calendar);
        entries.add(ModItems.sketchbook);




    }).build());
    public static void registerModItemGroup()
    {

    }
}
