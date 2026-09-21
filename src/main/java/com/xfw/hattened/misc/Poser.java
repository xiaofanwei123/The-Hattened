package com.xfw.hattened.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xfw.hattened.client.ClientStorage;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;

/** 根据同步动作姿态对渲染中的帽子应用位置和旋转变换。 */
public final class Poser {

    /** 按指定玩家的同步姿态，将帽子放到头顶或对应手臂动作位置。 */
    public static void transformHat(HatPose pose, PoseStack poseStack, PlayerModel<AbstractClientPlayer> contextModel, float partialTick) {
        float time = ClientStorage.ticks + partialTick;

        switch (pose) {
            case ON_HEAD:
                contextModel.head.translateAndRotate(poseStack);
                poseStack.translate(0.5f, -0.5f, -0.5f);
                break;

            case SEARCHING_HAT:
                contextModel.leftArm.translateAndRotate(poseStack);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
                poseStack.mulPose(Axis.XP.rotationDegrees(30.0f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-30.0f));
                poseStack.scale(0.8f, 0.8f, 0.8f);
                poseStack.translate(0.65f, -0.15f, -1.4f);
                break;

            case VACUUMING:
                //帽子保持在双手之间
                poseStack.translate(0.5f + Mth.sin(time / 5f) / 200f,
                        -Mth.cos(time / 6f + 1f) / 200f,
                        -0.8f);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
                break;
        }
    }

    /** 阻止实例化只提供帽子姿态变换函数的工具类。 */
    private Poser() {
    }
}
