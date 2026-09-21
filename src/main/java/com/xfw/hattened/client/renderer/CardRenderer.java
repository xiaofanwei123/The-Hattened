package com.xfw.hattened.client.renderer;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.misc.Card;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/** 绘制一张帽子存储卡片及其中保存的物品栈。 */
public final class CardRenderer {

    /** 在当前 GUI 变换位置按给定缩放绘制卡片背景、物品和数量。 */
    public static void render(GuiGraphics context, Font textRenderer, Card card, float scale, boolean drawText) {
        ItemStack stack = card.getStack();
        if (drawText) {
            var text = stack.getHoverName();
            int textWidth = textRenderer.width(text);
            context.drawString(textRenderer, text, -textWidth / 2, (int)(-14 * scale) - 5, 0xFFFFFFFF, true);
        }

        context.pose().pushPose();
        context.pose().scale(scale, scale, scale);
        context.blit(HattenedMain.id("textures/card.png"), -8, -12, 0, 0, 16, 24, 16, 24);
        context.pose().pushPose();
        context.pose().scale(0.6f, 0.6f, 0.6f);
        context.pose().translate(-8f, -8f, -140f);
        context.renderItem(stack, 0, 0);
        context.pose().translate(0f, 0f, -40f);
        context.renderItemDecorations(textRenderer, stack, 0, 0);
        context.pose().popPose();
        context.pose().popPose();
    }

    /** 阻止实例化只提供卡片绘制函数的工具类。 */
    private CardRenderer() {
    }
}
