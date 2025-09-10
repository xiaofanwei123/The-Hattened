package com.xfw.hattened.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xfw.hattened.client.ClientStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;

public class Poser {
    
    public static void transformHat(HatPose pose, PoseStack poseStack, PlayerModel<AbstractClientPlayer> contextModel, float partialTick) {
        float time = ClientStorage.ticks + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        
        switch (pose) {
            case ON_HEAD:
                contextModel.head.translateAndRotate(poseStack);
                poseStack.translate(0.5f, -0.5f, -0.5f);
                break;
                
            case SEARCHING_HAT:
                contextModel.leftArm.translateAndRotate(poseStack);
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90.0f));
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0f));
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(30.0f));
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-30.0f));
                poseStack.scale(0.8f, 0.8f, 0.8f);
                poseStack.translate(0.65f, -0.15f, -1.4f);
                break;

            case VACUUMING:
                poseStack.translate(0.5f + Mth.sin(time / 5f) / 200f, 
                                  -Mth.cos(time / 6f + 1f) / 200f, 
                                  -0.8f);
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90.0f));
                break;
        }
    }


    private static void applyModelPartTransform(ModelPart modelPart, com.mojang.blaze3d.vertex.PoseStack matrices) {
        matrices.translate(modelPart.x / 16.0f, modelPart.y / 16.0f, modelPart.z / 16.0f);

        if (modelPart.zRot != 0.0f) {
            matrices.mulPose(Axis.ZP.rotation(modelPart.zRot));
        }
        if (modelPart.yRot != 0.0f) {
            matrices.mulPose(Axis.YP.rotation(modelPart.yRot));
        }
        if (modelPart.xRot != 0.0f) {
            matrices.mulPose(Axis.XP.rotation(modelPart.xRot));
        }

        if (modelPart.xScale != 1.0f || modelPart.yScale != 1.0f || modelPart.zScale != 1.0f) {
            matrices.scale(modelPart.xScale, modelPart.yScale, modelPart.zScale);
        }
    }

    /**
     * 线性插值函数
     */
    private static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }






}