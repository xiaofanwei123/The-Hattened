package com.xfw.hattened.event;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.misc.HatController;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** 服务端事件*/
@EventBusSubscriber(modid = HattenedMain.MODID)
public final class HattenedServerEvents {
    /** 将帽子物品加入原版“工具与实用物品”创造模式标签页。 */
    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(HattenedMain.HAT_ITEM.get());
        }
    }

    /** 在服务端玩家常规 Tick 后推进长按吸取、吐出和姿态等帽子行为。 */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            HatController.tick(serverPlayer);
        }
    }

    /** 当玩家按住 Alt 控制帽子时，将脚下物品的普通拾取改为存入帽子。 */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (event.getPlayer() instanceof ServerPlayer serverPlayer
                && HatController.storeTouchedItem(serverPlayer, event.getItemEntity())) {
            event.setCanPickup(TriState.FALSE);
        }
    }

    /** 阻止实例化只用于承载服务端事件处理器的工具类。 */
    private HattenedServerEvents() {
    }
}
