package com.xfw.hattened.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xfw.hattened.client.ClientStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
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
}