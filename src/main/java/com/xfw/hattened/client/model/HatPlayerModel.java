package com.xfw.hattened.client.model;

import com.xfw.hattened.client.ClientStorage;
import com.xfw.hattened.misc.HattenedHelper;
import com.xfw.hattened.misc.HatPose;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class HatPlayerModel implements IAnimation {
    private final Player player;

    public HatPlayerModel(Player player) {
        this.player = player;
    }

    @Override
    public void setupAnim(float tickDelta) {
    }

    @Override
    public boolean isActive() {
        return HattenedHelper.getHatData(player).hasHat();
    }

    @Override
    public Vec3f get3DTransform(String modelKey, TransformType type, float tickDelta, Vec3f original) {
        if (type != TransformType.ROTATION) {
            return original;
        }

        Vec3f rotation = rotateBody(HattenedHelper.getPose(player), modelKey, tickDelta, player);
        return rotation != null ? rotation.scale(Mth.DEG_TO_RAD) : original;
    }

    private Vec3f rotateBody(HatPose pose, String key, float tickDelta, Player player) {
        float time = ClientStorage.ticks + tickDelta;

        switch (pose) {
            case ON_HEAD:
                return null;
            case SEARCHING_HAT:
                if ("leftArm".equals(key)) {
                    return new Vec3f(-50f, 45f, 0f);
                } else if ("rightArm".equals(key)) {
                    if (player.swinging) {
                        float swingProgress = player.getAttackAnim(tickDelta);
                        return new Vec3f(
                                lerp(swingProgress, -50f, -110f),
                                lerp(swingProgress, -25f, -20f),
                                0f
                        );
                    } else {
                        float pitch = (float) Math.sin(time / 3f) * 2f;
                        float yaw = (float) Math.cos(time / 2f) * 2f;
                        return new Vec3f(-50f + pitch, -25f + yaw, 0f);
                    }
                }
                return null;
            case VACUUMING:
                if ("leftArm".equals(key)) {
                    return new Vec3f(
                            -50f + (float) Math.sin(time / 5f) / 2f,
                            10f + (float) Math.cos(time / 6f + 1f),
                            0f
                    );
                } else if ("rightArm".equals(key)) {
                    return new Vec3f(
                            -50f + (float) Math.sin(time / 5f) / 2f,
                            -10f + (float) Math.cos(time / 6f + 1f),
                            0f
                    );
                }
                return null;
            default:
                return null;
        }
    }

    private float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }
}