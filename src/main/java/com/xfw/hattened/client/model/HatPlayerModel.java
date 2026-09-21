package com.xfw.hattened.client.model;

import com.xfw.hattened.client.ClientStorage;
import com.xfw.hattened.misc.HattenedHelper;
import com.xfw.hattened.misc.HatPose;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/** 在玩家控制帽子时向 Player Animator 提供双臂动作变换。 */
public class HatPlayerModel implements IAnimation {
    private final Player player;
    private final FirstPersonConfiguration firstPersonConfiguration = new FirstPersonConfiguration(true, true, false, false);

    /** 将该动画层绑定到当前被渲染的玩家，以读取其同步姿态。 */
    public HatPlayerModel(Player player) {
        this.player = player;
    }

    /** 保持无状态动画持续可用，使渲染器随时可以查询部位变换。 */
    @Override
    public void setupAnim(float tickDelta) {
    }

    /** 仅在帽子执行动作时启用 Player Animator，静止佩戴时不遮蔽披风。 */
    @Override
    public boolean isActive() {
        return HattenedHelper.getHatData(player).hasHat()
                && HattenedHelper.getPose(player) != HatPose.ON_HEAD;
    }

    /** 根据当前帽子动作选择第一人称手臂的渲染模式。 */
    @Override
    public FirstPersonMode getFirstPersonMode(float tickDelta) {
        // 检查当前姿态，只在动作时启用第一人称
        HatPose pose = HattenedHelper.getPose(player);
        if (pose == HatPose.SEARCHING_HAT || pose == HatPose.VACUUMING) {
            return FirstPersonMode.THIRD_PERSON_MODEL;
        }
        return FirstPersonMode.NONE;
    }

    /** 返回第一人称帽子动作中左右手臂的可见性配置。 */
    @Override
    public FirstPersonConfiguration getFirstPersonConfiguration(float tickDelta) {
        return firstPersonConfiguration;
    }

    /** 为参与帽子动作的身体部位提供旋转覆盖，其余部位保持原动画。 */
    @Override
    public Vec3f get3DTransform(String modelKey, TransformType type, float tickDelta, Vec3f original) {
        if (type != TransformType.ROTATION) {
            return original;
        }

        HatPose pose = HattenedHelper.getPose(player);
        

        Vec3f rotation = rotateBody(pose, modelKey, tickDelta, player);
        return rotation != null ? rotation.scale(Mth.DEG_TO_RAD) : original;
    }

    /** 根据同步姿态和玩家视角计算当前帧的双臂旋转。 */
    private Vec3f rotateBody(HatPose pose, String key, float tickDelta, Player player) {
        float time = ClientStorage.ticks + tickDelta;

        // 获取头部旋转信息用于第一人称跟随
        Vec3f headRotation = getHeadRotationOffset(player);

        switch (pose) {
            case ON_HEAD:
                return null;
            case SEARCHING_HAT:
                if ("leftArm".equals(key)) {
                    Vec3f baseRotation = new Vec3f(-50f, 45f, 0f);
                    return addHeadFollowing(baseRotation, headRotation, 0.3f);
                } else if ("rightArm".equals(key)) {
                    if (player.swinging) {
                        float swingProgress = player.getAttackAnim(tickDelta);
                        Vec3f baseRotation = new Vec3f(
                                lerp(swingProgress, -50f, -110f),
                                lerp(swingProgress, -25f, -20f),
                                0f
                        );
                        return addHeadFollowing(baseRotation, headRotation, 0.2f);
                    } else {
                        float pitch = (float) Math.sin(time / 3f) * 2f;
                        float yaw = (float) Math.cos(time / 2f) * 2f;
                        Vec3f baseRotation = new Vec3f(-50f + pitch, -25f + yaw, 0f);
                        
                        // 分别调整左右强度：右看用0.8，左看用0.5
                        float intensity = headRotation.getY() > 0 ? 0.8f : 0.5f;
                        return addHeadFollowing(baseRotation, headRotation, intensity);
                    }
                }
                return null;
            case VACUUMING:
                if ("leftArm".equals(key)) {
                    Vec3f baseRotation = new Vec3f(
                            -50f + (float) Math.sin(time / 5f) / 2f,
                            10f + (float) Math.cos(time / 6f + 1f),
                            0f
                    );
                    return addHeadFollowing(baseRotation, headRotation, 0.25f);
                } else if ("rightArm".equals(key)) {
                    Vec3f baseRotation = new Vec3f(
                            -50f + (float) Math.sin(time / 5f) / 2f,
                            -10f + (float) Math.cos(time / 6f + 1f),
                            0f
                    );
                    return addHeadFollowing(baseRotation, headRotation, 0.25f);
                }
                return null;
            default:
                return null;
        }
    }

    /**
     * 获取头部旋转偏移量，用于第一人称视角的手臂跟随
     */
    private Vec3f getHeadRotationOffset(Player player) {
        float yawDiff = Mth.wrapDegrees(player.getYHeadRot() - player.yBodyRot);
        yawDiff = Mth.clamp(yawDiff, -90f, 90f);
        float headPitch = Mth.clamp(player.getXRot(), -60f, 60f);
        return new Vec3f(headPitch * 0.01f, yawDiff * 0.01f, 0f);
    }

    /**
     * 将头部跟随效果添加到基础旋转上
     */
    private Vec3f addHeadFollowing(Vec3f baseRotation, Vec3f headRotation, float intensity) {
        return new Vec3f(
                baseRotation.getX() + headRotation.getX() * intensity,
                baseRotation.getY() + headRotation.getY() * intensity,
                baseRotation.getZ() + headRotation.getZ() * intensity
        );
    }

    /** 在线性插值起止角度后生成当前动画角度。 */
    private float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }
}
