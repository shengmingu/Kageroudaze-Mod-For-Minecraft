package com.mekakucity;

import com.mekakucity.entity.ModEntities;
import com.mekakucity.entity.client.ModModelLayers;
import com.mekakucity.entity.client.kurohaModel;
import com.mekakucity.entity.client.kurohaRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class Kageroudazeclient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.KUROHA, kurohaModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.KUROHA, kurohaRenderer::new);

    }


    // 简单的实体渲染器


}




