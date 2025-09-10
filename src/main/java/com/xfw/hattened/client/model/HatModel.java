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

public class HatModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(HattenedMain.id("hat"), "main");
    private static final ResourceLocation TEXTURE = HattenedMain.id("textures/hat.png");
    
    private final ModelPart hat;

    public HatModel(ModelPart root) {
        super(RenderType::entityCutout);
        this.hat = root.getChild("hat");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-13.5F, -2.0F, 2.5F, 11.0F, 2.0F, 11.0F)
                .texOffs(0, 13).addBox(-12.0F, -10.0F, 4.0F, 8.0F, 8.0F, 8.0F), 
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        hat.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    public void renderHat(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        hat.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }


    public static ResourceLocation getTextureLocation() {
        return TEXTURE;
    }
}