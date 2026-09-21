package com.xfw.hattened.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 记录帽子工具提示中哪些数据组件应被隐藏，从而控制存储图像是否显示。 */
public record TooltipDisplayComponent(boolean hideTooltip, List<DataComponentType<?>> hiddenComponents) {
    public static final Codec<TooltipDisplayComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.optionalFieldOf("hide_tooltip", false).forGetter(TooltipDisplayComponent::hideTooltip),
                    DataComponentType.CODEC.listOf().optionalFieldOf("hidden_components", List.of()).forGetter(TooltipDisplayComponent::hiddenComponents)
            ).apply(instance, TooltipDisplayComponent::new)
    );

    public static final TooltipDisplayComponent DEFAULT = new TooltipDisplayComponent(false, List.of());

    /** 将隐藏组件列表复制为不可变集合，防止组件创建后被外部修改。 */
    public TooltipDisplayComponent(boolean hideTooltip, List<DataComponentType<?>> hiddenComponents) {
        this.hideTooltip = hideTooltip;
        this.hiddenComponents = hiddenComponents == null ? List.of() : List.copyOf(hiddenComponents);
    }

    /** 返回修改了指定组件可见性的副本，不改变当前组件实例。 */
    public TooltipDisplayComponent with(DataComponentType<?> component, boolean hidden) {
        Objects.requireNonNull(component);
        boolean already = this.hiddenComponents.contains(component);
        if (already == hidden) return this;

        List<DataComponentType<?>> copy = new ArrayList<>(this.hiddenComponents);
        if (hidden) copy.add(component);
        else copy.remove(component);
        return new TooltipDisplayComponent(this.hideTooltip, copy);
    }

    /** 判断指定数据组件是否允许向物品工具提示添加内容。 */
    public boolean shouldDisplay(DataComponentType<?> component) {
        if (this.hideTooltip) return false;
        return !this.hiddenComponents.contains(Objects.requireNonNull(component));
    }

    /** 生成包含隐藏组件信息的文本，便于日志和调试查看。 */
    @Override
    public String toString() {
        return "TooltipDisplayComponent{" +
                "hideTooltip=" + hideTooltip +
                ", hiddenComponents=" + hiddenComponents +
                '}';
    }
}
