package com.mekakucity;



import com.mekakucity.entity.ModEntities;
import com.mekakucity.entity.custom.kuroha;
import com.mekakucity.event.*;
import com.mekakucity.item.ModItemGroup;
import com.mekakucity.item.ModItems;
import net.fabricmc.api.ModInitializer;


import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Kageroudaze implements ModInitializer {
	public static final String MOD_ID = "kageroudaze";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);



	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
		ModItems.registerModItems();
		ModEntities.registerEntities();

		ModItemGroup.registerModItemGroup();
		ServerPlayerEvents.AFTER_RESPAWN.register(new PlayerDeathHandler());
		ItemUseHandler.register();
		HeadPlacementHandler.register();
		ServerPlayerEvents.AFTER_RESPAWN.register(new SafeDeathTeleporter());



		PlayerEquipmentHandler.register();
		FabricDefaultAttributeRegistry.register(ModEntities.KUROHA, kuroha.createkurohaAttributes());



	}
}