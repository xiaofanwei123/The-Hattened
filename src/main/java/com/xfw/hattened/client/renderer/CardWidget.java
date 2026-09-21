package com.xfw.hattened.client.renderer;

import com.mojang.math.Axis;
import com.xfw.hattened.misc.Card;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.joml.Vector2f;

/** 保存一张物品卡片仅客户端使用的位置、角度和缩放动画状态。 */
public class CardWidget {
    public int index;
    public Card card;
    public boolean removing = false;

    private float angle = 0f;
    private Vector2f position = new Vector2f(0f, 75f);
    private float scale = 1f;
    private float previousAngle = 0f;
    private final Vector2f previousPosition = new Vector2f(0f, 75f);
    private float previousScale = 1f;

    public float targetAngle = 0f;
    public Vector2f targetPosition = new Vector2f();
    public float targetScale = 1f;

    /** 为具有稳定标识的存储卡片创建动画控件。 */
    public CardWidget(int index, Card card) {
        this.index = index;
        this.card = card;
    }

    /** 每 Tick 推进卡片位置、角度和缩放的平滑插值。 */
    public void tick() {
        this.previousPosition.set(this.position);
        this.previousAngle = this.angle;
        this.previousScale = this.scale;
        this.position = this.position.lerp(this.targetPosition, 0.15f, new Vector2f());
        this.angle = Mth.lerp(0.15f, this.angle, this.targetAngle);

        if (this.removing) {
            this.targetScale = 0f;
        }
        this.scale = Mth.lerp(0.15f, this.scale, this.targetScale);
    }

    /** 在前后两个 Tick 状态之间插值，以当前渲染帧的平滑变换绘制卡片。 */
    public void render(GuiGraphics context, int pos) {
        context.pose().pushPose();
        float cardDepth = -Math.abs(pos) * 50f;
        context.pose().translate(this.position.x, this.position.y, cardDepth);
        context.pose().mulPose(Axis.ZP.rotation(this.angle / 180f * Mth.PI));
        context.pose().scale(this.scale, this.scale, this.scale);
        var textRenderer = Minecraft.getInstance().font;
        CardRenderer.render(context, textRenderer, this.card, 4f, true);
        context.pose().popPose();
    }

    /** 判断已移除卡片的缩小动画是否结束，可以从客户端列表清理。 */
    public boolean canRemove() {
        return this.scale <= 0.05f && this.removing;
    }
}
