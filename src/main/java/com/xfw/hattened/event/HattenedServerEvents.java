package com.xfw.hattened.event;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.misc.HatData;
import com.xfw.hattened.misc.HattenedHelper;
import com.xfw.hattened.networking.HatDataSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = HattenedMain.MODID)
public class HattenedServerEvents {

    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            //同步帽子数据到刚登录的玩家
            HatData hatData = HattenedHelper.getHatData(serverPlayer);
            PacketDistributor.sendToPlayer(serverPlayer,
                new HatDataSyncPayload(serverPlayer.getId(), hatData, HattenedHelper.getPose(serverPlayer)));
        }
    }

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(HattenedMain.HAT_ITEM.get());
        }
    }
}