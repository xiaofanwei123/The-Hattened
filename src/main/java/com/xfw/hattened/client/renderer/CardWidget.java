package com.xfw.hattened.client.renderer;

import com.mojang.math.Axis;
import com.xfw.hattened.misc.Card;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.joml.Vector2f;

public class CardWidget {
    public int index;
    public Card card;
    public boolean removing = false;

    private float angle = 0f;
    private Vector2f position = new Vector2f(0f, 75f);
    private float scale = 1f;

    public float targetAngle = 0f;
    public Vector2f targetPosition = new Vector2f();
    public float targetScale = 1f;

    public CardWidget(int index, Card card) {
        this.index = index;
        this.card = card;
    }

    public void tick() {
        this.position = this.position.lerp(this.targetPosition, 0.15f, new Vector2f());
        this.angle = Mth.lerp(0.15f, this.angle, this.targetAngle);

        if (this.removing) {
            this.targetScale = 0f;
        }
        this.scale = Mth.lerp(0.15f, this.scale, this.targetScale);
    }

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

    public boolean canRemove() {
        return this.scale <= 0.05f && this.removing;
    }
}
