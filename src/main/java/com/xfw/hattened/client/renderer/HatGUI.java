package com.xfw.hattened.client.renderer;

import com.xfw.hattened.client.ClientStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2f;

import java.util.Map;

/** 在快捷栏上方绘制带动画的扇形帽子存储卡片。 */
public final class HatGUI {
    /** 按界面开合进度和帧间插值绘制当前仍在动画列表中的每张存储卡片。 */
    public static void render(GuiGraphics context, float progress, float partialTick) {
        if (ClientStorage.cards.isEmpty()) return;

        float riseProgress = Math.min(progress / 0.5f, 1.0f);
        float spreadProgress = Math.max(Math.min((progress - 0.5f) / 0.5f, 1.0f), 0.0f);

        context.pose().pushPose();
        Minecraft client = Minecraft.getInstance();
        context.pose().translate(client.getWindow().getGuiScaledWidth() / 2f, client.getWindow().getGuiScaledHeight(), 0f);

        for (Map.Entry<CardWidget, Integer> entry : ClientStorage.orderedCards) {
            CardWidget card = entry.getKey();
            int pos = entry.getValue();

            float distance = (float) (75 * Math.pow(0.75f, Math.abs(pos) - 1));
            card.targetPosition = new Vector2f(spreadProgress * pos * distance, -140f * riseProgress + 70);
            card.targetAngle = spreadProgress * pos * 10f;
            card.targetScale = (float) Math.pow(0.75, Math.abs(pos));
            card.tick();
            card.render(context, pos);
        }
        context.pose().popPose();
    }

    /** 阻止实例化只提供帽子菜单绘制函数的工具类。 */
    private HatGUI() {
    }
}
