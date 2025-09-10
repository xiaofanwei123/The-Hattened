package com.xfw.hattened.client.renderer;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.misc.Card;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class CardRenderer {

    public static void render(GuiGraphics context, Font textRenderer, Card card, float scale, boolean drawText) {
        if (drawText) {
            var text = card.getStack().getHoverName();
            int textWidth = textRenderer.width(text);
            context.drawString(textRenderer, text, -textWidth / 2, (int)(-14 * scale) - 5, 0xFFFFFFFF, true);
        }

        context.pose().pushPose();
        context.pose().scale(scale, scale, scale);
        context.blit(HattenedMain.id("textures/card.png"), -8, -12, 0, 0, 16, 24, 16, 24);
        context.pose().pushPose();
        context.pose().pushPose();
        context.pose().scale(0.6f, 0.6f, 0.6f);
        context.pose().translate(-8f, -8f, -140f);
        context.renderItem(card.getStack(), 0, 0);
        context.pose().translate(0f, 0f, -40f);
        context.renderItemDecorations(textRenderer, card.getStack(), 0, 0);
        context.pose().popPose();
        context.pose().popPose();
        context.pose().popPose();
    }

}
