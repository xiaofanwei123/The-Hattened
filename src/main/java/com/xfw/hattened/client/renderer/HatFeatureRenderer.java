package com.xfw.hattened.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xfw.hattened.client.model.HatModel;
import com.xfw.hattened.misc.HatData;
import com.xfw.hattened.misc.HatPose;
import com.xfw.hattened.misc.HattenedHelper;
import com.xfw.hattened.misc.Poser;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** 为原生槽中佩戴帽子的玩家绘制同步模型，并读取 Curios 的可见开关。 */
public class HatFeatureRenderer extends PlayerItemInHandLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final HatModel hatModel;

    /** 使用共享的烘焙帽子模型创建玩家特征渲染层。 */
    public HatFeatureRenderer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer, HatModel hatModel) {
        super(renderer, Minecraft.getInstance().gameRenderer.itemInHandRenderer);
        this.hatModel = hatModel;
    }

    /** 玩家装备帽子时，根据其同步姿态变换并绘制帽子。 */
    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, 
                      float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        
        HatData hatData = HattenedHelper.getHatData(player);
        if (!hatData.hasHat()) {
            return;
        }

        HatPose pose = HattenedHelper.getPose(player);
        if (pose == HatPose.ON_HEAD && !HattenedHelper.shouldRenderHat(player)) {
            return;
        }
        poseStack.pushPose();
        
        Poser.transformHat(pose, poseStack, this.getParentModel(), partialTick);
        
        var vertexConsumer = buffer.getBuffer(hatModel.renderType(HatModel.getTextureLocation()));
        hatModel.renderHat(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        
        poseStack.popPose();
    }
}
