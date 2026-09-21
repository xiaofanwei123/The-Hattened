package com.xfw.hattened.client.renderer;

import com.xfw.hattened.item.HatItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

/** 在物品工具提示中把帽子存储内容绘制成横向卡片条。 */
public class HatTooltipComponent implements ClientTooltipComponent {
    private final HatItem.HatTooltipData data;

    /** 为一份不可变工具提示数据快照创建客户端渲染组件。 */
    public HatTooltipComponent(HatItem.HatTooltipData component) {
        this.data = component;
    }

    /** 返回卡片条占用的固定高度，供工具提示布局计算。 */
    @Override
    public int getHeight() {
        return data.getStorage().isEmpty() ? 0 : 52;
    }

    /** 根据存储卡片数量返回完整卡片条所需宽度。 */
    @Override
    public int getWidth(Font font) {
        return data.getStorage().isEmpty() ? 0 : data.getStorage().size() * 35;
    }

    /** 依次绘制所有存储卡片，但不显示菜单悬停时使用的物品名称。 */
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
