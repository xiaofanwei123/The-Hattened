package com.xfw.hattened.client.tooltipComponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record TooltipDisplayComponent(boolean hideTooltip, List<DataComponentType<?>> hiddenComponents) {
    public static final Codec<TooltipDisplayComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.optionalFieldOf("hide_tooltip", false).forGetter(TooltipDisplayComponent::hideTooltip),
                    DataComponentType.CODEC.listOf().optionalFieldOf("hidden_components", List.of()).forGetter(TooltipDisplayComponent::hiddenComponents)
            ).apply(instance, TooltipDisplayComponent::new)
    );

    public static final TooltipDisplayComponent DEFAULT = new TooltipDisplayComponent(false, List.of());

    public TooltipDisplayComponent(boolean hideTooltip, List<DataComponentType<?>> hiddenComponents) {
        this.hideTooltip = hideTooltip;
        this.hiddenComponents = hiddenComponents == null ? List.of() : List.copyOf(hiddenComponents);
    }

    public boolean shouldDisplay(DataComponentType<?> component) {
        if (this.hideTooltip) return false;
        return !this.hiddenComponents.contains(Objects.requireNonNull(component));
    }
}
