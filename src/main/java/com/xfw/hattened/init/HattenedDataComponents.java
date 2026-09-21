package com.xfw.hattened.init;

import com.mojang.serialization.Codec;
import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.component.TooltipDisplayComponent;
import com.xfw.hattened.misc.Card;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class HattenedDataComponents {
    // 注册帽子存储、Curios 镜像标记和工具提示使用的数据组件。
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, HattenedMain.MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<Card>>> HAT_STORAGE_COMPONENT =
            DATA_COMPONENTS.register("hat_storage", () -> DataComponentType.<List<Card>>builder()
                    .persistent(Card.CODEC.listOf())
                    .networkSynchronized(Card.STREAM_CODEC.apply(ByteBufCodecs.list()))
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> CURIO_MIRROR_COMPONENT =
            DATA_COMPONENTS.register("curio_proxy", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TooltipDisplayComponent>> TOOLTIP_DISPLAY =
            DATA_COMPONENTS.register("tooltip_display", () ->
                    DataComponentType.<TooltipDisplayComponent>builder()
                            .persistent(TooltipDisplayComponent.CODEC)
                            .build()
            );

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
