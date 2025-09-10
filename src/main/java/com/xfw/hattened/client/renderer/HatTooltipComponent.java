package com.xfw.hattened.client.renderer;

import com.xfw.hattened.item.HatItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class HatTooltipComponent implements ClientTooltipComponent {
    private final HatItem.HatTooltipData data;

    public HatTooltipComponent(HatItem.HatTooltipData component) {
        this.data = component;
    }

    @Override
    public int getHeight() {
        return data.getStorage().isEmpty() ? 0 : 52;
    }

    @Override
    public int getWidth(Font font) {
        return data.getStorage().isEmpty() ? 0 : data.getStorage().size() * 35;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        if (data.getStorage().isEmpty()) {
            return;
        }

        for (int i = 0; i < data.getStorage().size(); i++) {
            var card = data.getStorage().get(i);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x + i * 35 + 18f, y + 24f, 0f);
            CardRenderer.render(guiGraphics, font, card, 2f, false);
            guiGraphics.pose().popPose();
        }
    }
}