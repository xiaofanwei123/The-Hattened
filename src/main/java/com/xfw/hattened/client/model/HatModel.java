package com.xfw.hattened.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.xfw.hattened.HattenedMain;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** 定义礼帽的独立几何体，并提供纹理和模型绘制入口。 */
public class HatModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(HattenedMain.id("hat"), "main");
    private static final ResourceLocation TEXTURE = HattenedMain.id("textures/hat.png");
    
    private final ModelPart hat;

    /** 从已烘焙模型根节点中取得并保存帽子子节点。 */
    public HatModel(ModelPart root) {
        super(RenderType::entityCutout);
        this.hat = root.getChild("hat");
    }

    /** 构建玩家帽子特征渲染器使用的固定礼帽几何体。 */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

//        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create()
//                .texOffs(0, 0).addBox(-13.5F, -2.0F, 2.5F, 11.0F, 2.0F, 11.0F)
//                .texOffs(0, 13).addBox(-12.0F, -10.0F, 4.0F, 8.0F, 8.0F, 8.0F),
//                PartPose.ZERO);
        PartDefinition hat = partdefinition.addOrReplaceChild("hat", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-13.5F, -2.0F, 2.5F, 11.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(-12.0F, -10.0F, 4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 29).addBox(-12.5F, -4.0F, 3.5F, 9.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    /** 通过标准模型接口使用传入颜色和光照绘制完整帽子。 */
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        hat.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    /** 使用调用方指定的叠加纹理状态绘制帽子主体。 */
    public void renderHat(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        hat.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }


    /** 返回帽子模型绘制时使用的纹理资源位置。 */
    public static ResourceLocation getTextureLocation() {
        return TEXTURE;
    }
}
