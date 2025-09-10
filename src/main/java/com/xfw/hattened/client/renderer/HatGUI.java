package com.xfw.hattened.client.renderer;

import com.xfw.hattened.client.ClientStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2f;

import java.util.Map;

public class HatGUI {
    public static void render(GuiGraphics context, float progress) {
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
}
