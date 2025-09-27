package com.xfw.hattened;

import com.mojang.logging.LogUtils;
import com.xfw.hattened.client.tooltipComponent.TooltipDisplayComponent;
import com.xfw.hattened.init.HattenedAttachments;
import com.xfw.hattened.client.sound.HattenedSounds;
import com.xfw.hattened.item.HatItem;
import com.xfw.hattened.misc.Card;
import com.xfw.hattened.networking.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.List;
import java.util.Random;

@Mod(HattenedMain.MODID)
public class HattenedMain {
    public static final String MODID = "hattened";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Random RANDOM = new Random();

    //Particles
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, MODID);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CONFETTI_PARTICLE =
        PARTICLES.register("confetti", () -> new SimpleParticleType(true));

    //Data Components
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<Card>>> HAT_STORAGE_COMPONENT =
        DATA_COMPONENTS.register("hat_storage", () -> DataComponentType.<List<Card>>builder()
            .persistent(Card.CODEC.listOf())
            .networkSynchronized(Card.STREAM_CODEC.apply(ByteBufCodecs.list()))
            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TooltipDisplayComponent>> TOOLTIP_DISPLAY =
            DATA_COMPONENTS.register("tooltip_display", () ->
                    DataComponentType.<TooltipDisplayComponent>builder()
                            .persistent(TooltipDisplayComponent.CODEC)
                            .build()
            );
    //Items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredHolder<Item, HatItem> HAT_ITEM = ITEMS.register("hat", HatItem::new);

    public HattenedMain(IEventBus modEventBus, Dist dist) {
        ITEMS.register(modEventBus);
        PARTICLES.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        HattenedAttachments.ATTACHMENT_TYPES.register(modEventBus);
        HattenedSounds.SOUNDS.register(modEventBus);

        modEventBus.addListener(this::setupPackets);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public void setupPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        //中键向附近所有玩家发送彩色纸屑效果
        registrar.playToServer(HatInputPayload.TYPE, HatInputPayload.STREAM_CODEC,HatInputPayload::handle);
        //H脱帽子
        //将玩家的Attachments转化为数据组件存到物品里面
        registrar.playToServer(HatKeybindPayload.TYPE, HatKeybindPayload.STREAM_CODEC, HatKeybindPayload::handle);
        //吸物品
        registrar.playToClient(SuckItemPayload.TYPE, SuckItemPayload.STREAM_CODEC, SuckItemPayload::handle);
        //播放撒烟花
        registrar.playToClient(ConfettiPayload.TYPE, ConfettiPayload.STREAM_CODEC, ConfettiPayload::handle);
        //渲染帽子
        registrar.playToClient(HatDataSyncPayload.TYPE, HatDataSyncPayload.STREAM_CODEC, HatDataSyncPayload::handle);
    }
}

