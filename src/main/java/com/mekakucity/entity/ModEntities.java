package com.mekakucity.entity;


import com.mekakucity.entity.custom.kuroha;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<kuroha> KUROHA = FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, kuroha::new)
            .dimensions(EntityDimensions.fixed(1.5f, 3.5f))
            .build();






    public static void registerEntities() {
        Registry.register(Registries.ENTITY_TYPE,
                new Identifier("kageroudaze", "kuroha"), KUROHA);




    }


}