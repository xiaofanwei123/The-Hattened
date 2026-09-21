package com.xfw.hattened;

import com.mojang.serialization.Codec;
import com.xfw.hattened.component.TooltipDisplayComponent;
import com.xfw.hattened.compat.curios.CuriosCompat;
import com.xfw.hattened.init.HattenedAttachments;
import com.xfw.hattened.init.HattenedDataComponents;
import com.xfw.hattened.init.HattenedNetworking;
import com.xfw.hattened.init.HattenedSounds;
import com.xfw.hattened.item.HatItem;
import com.xfw.hattened.misc.Card;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Random;

/** 注册帽子模组的内容，并在对应模组存在时启用兼容功能。 */
@Mod(HattenedMain.MODID)
public class HattenedMain {
    public static final String MODID = "hattened";
    public static final Random RANDOM = new Random();

    // 注册五彩纸屑粒子类型。
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, MODID);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CONFETTI_PARTICLE =
        PARTICLES.register("confetti", () -> new SimpleParticleType(true));

    // 注册玩家可以持有和装备的帽子物品。
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredHolder<Item, HatItem> HAT_ITEM = ITEMS.register("hat", HatItem::new);

    /** 将公共注册表挂到模组总线，并按已加载模组启用可选兼容。 */
    public HattenedMain(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        PARTICLES.register(modEventBus);
        HattenedDataComponents.register(modEventBus);
        HattenedAttachments.ATTACHMENT_TYPES.register(modEventBus);
        HattenedSounds.SOUNDS.register(modEventBus);
        modEventBus.addListener(HattenedNetworking::register);
        if (ModList.get().isLoaded("curios")) {
            CuriosCompat.init(modEventBus);
        }
    }

    /** 使用本模组命名空间创建资源位置，避免各处重复填写模组 ID。 */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
