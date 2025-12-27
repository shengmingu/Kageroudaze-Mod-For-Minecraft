package com.mekakucity.entity.client;


import com.mekakucity.entity.custom.kuroha;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class kurohaRenderer extends MobEntityRenderer<kuroha, PlayerEntityModel< kuroha>> {
    private static final Identifier TEXTURE = new Identifier("kageroudaze", "textures/entity/boss/kuroha.png");

    public  kurohaRenderer (EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.5f);

        // 添加手持物品渲染器
        this.addFeature(new HeldItemFeatureRenderer<>(this, context.getHeldItemRenderer()));


    // Call the superclass constructor with the provided context, a new PlayerEntityModel,
    // and a scale factor of 0.5f
    }

    @Override
    public Identifier getTexture( kuroha entity) {
        return TEXTURE;
    }

    @Override
    public void render( kuroha entity, float f, float g, MatrixStack matrixStack,
                       VertexConsumerProvider vertexConsumerProvider, int i) {
        // 设置手臂姿势
        PlayerEntityModel< kuroha> model = this.getModel();
        ItemStack mainHandStack = entity.getMainHandStack();

        if (!mainHandStack.isEmpty()) {
            model.leftArmPose = BipedEntityModel.ArmPose.ITEM;
            model.rightArmPose = BipedEntityModel.ArmPose.ITEM;
        } else {
            model.leftArmPose = BipedEntityModel.ArmPose.EMPTY;
            model.rightArmPose = BipedEntityModel.ArmPose.EMPTY;
        }

        // 调用父类渲染
        super.render(entity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    protected void setupTransforms(kuroha entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta) {
        super.setupTransforms(entity, matrices, animationProgress, bodyYaw, tickDelta);

        // 添加攻击动画
        if (entity.handSwinging) {
            float swingProgress = entity.getHandSwingProgress(tickDelta);
            float swing = MathHelper.sin(swingProgress * (float)Math.PI);
            float swing2 = MathHelper.sin((1.0F - (1.0F - swingProgress) * (1.0F - swingProgress)) * (float)Math.PI);

            // 根据主要手臂调整动画
            if (entity.getMainArm() == Arm.RIGHT) {
                this.getModel().rightArm.pitch = -0.8F + 0.025F * swing2;
                this.getModel().rightArm.yaw = -0.3F * swing;
                this.getModel().rightArm.roll = 0.0F;
            } else {
                this.getModel().leftArm.pitch = -0.8F + 0.025F * swing2;
                this.getModel().leftArm.yaw = 0.3F * swing;
                this.getModel().leftArm.roll = 0.0F;
            }
        }
    }
}