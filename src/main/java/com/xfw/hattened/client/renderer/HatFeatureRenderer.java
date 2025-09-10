package com.xfw.hattened.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xfw.hattened.client.model.HatModel;
import com.xfw.hattened.misc.HatData;
import com.xfw.hattened.misc.HatPose;
import com.xfw.hattened.misc.HattenedHelper;
import com.xfw.hattened.misc.Poser;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class HatFeatureRenderer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final HatModel hatModel;

    public HatFeatureRenderer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer, HatModel hatModel) {
        super(renderer);
        this.hatModel = hatModel;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, 
                      float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        
        //从客户端存储获取帽子数据
        HatData hatData = HattenedHelper.getHatData(player);
        if (!hatData.hasHat()) {
            return;
        }
        
        HatPose pose = HattenedHelper.getPose(player);
        poseStack.pushPose();
        
        //使用Poser类来正确变换帽子位置
        Poser.transformHat(pose, poseStack, this.getParentModel(), partialTick);
        
        var vertexConsumer = buffer.getBuffer(hatModel.renderType(HatModel.getTextureLocation()));
        hatModel.renderHat(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        
        poseStack.popPose();
    }
}