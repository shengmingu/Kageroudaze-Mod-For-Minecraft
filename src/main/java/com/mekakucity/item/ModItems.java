package com.mekakucity.item;



import com.mekakucity.Kageroudaze;

import com.mekakucity.item.custom.*;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;


public class ModItems {

    public static final Item mekaku=registerItems("mekaku",new mekaku(new FabricItemSettings()));
    public static final Item mewonusumu=registerItems("mewonusumu",new mewonusumu(new FabricItemSettings()));
    public static final Item mewoazamuku=registerItems("mewoazamuku",new mewoazamuku(new FabricItemSettings()));
    public static final Item mewoawaseru=registerItems("mewoawaseru",new mewoawaseru(new FabricItemSettings()));
    public static final Item meoubau=registerItems("meoubau",new meoubau(new FabricItemSettings()));

    public static final Item mewokorasu=registerItems("mewokorasu",new mewokorasu(new FabricItemSettings()));
    public static final Item mewosamasu=registerItems("mewosamasu",new mewosamasu(new FabricItemSettings()));
    public static final Item megasameru=registerItems("megasameru",new megasameru(new FabricItemSettings()));
    public static final Item mewokakeru=registerItems("mewokakeru",new mewokakeru(new FabricItemSettings()));
    public static final Item meniyakitsukeru=registerItems("meniyakitsukeru",new meniyakitsukeru(new FabricItemSettings()));
    public static final Item megasaeru=registerItems("megasaeru",new megasaeru(new FabricItemSettings()));

    public static final Item phone=registerItems("phone",new phone(new FabricItemSettings()));
    public static final Item scarf=registerItems("scarf",new scarf(new FabricItemSettings()));
    public static final Item scissor=registerItems("scissor",new scissor(new FabricItemSettings()));
    public static final Item calendar=registerItems("calendar",new calendar(new FabricItemSettings()));

    public static final Item ouroboros=registerItems("ouroboros",new ouroboros(new FabricItemSettings()));
    public static final Item sketchbook=registerItems("sketchbook",new sketchbook(new FabricItemSettings()));



    private static Item registerItems(String Id,Item item){
        return Registry.register(Registries.ITEM,new Identifier(Kageroudaze.MOD_ID,Id),item);

    }
    public static void registerModItems(){

    }
}
