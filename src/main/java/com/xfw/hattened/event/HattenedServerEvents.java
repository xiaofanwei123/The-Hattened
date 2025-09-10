package com.xfw.hattened.event;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.misc.HatData;
import com.xfw.hattened.misc.HattenedHelper;
import com.xfw.hattened.networking.HatDataSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

//    @SubscribeEvent//测试TODO
//    public static void onHatThrowItemPre(HatThrowItemEvent.Pre event) {
//        if (event.getThrownStack().is(Items.DIRT)) {
//            //将泥土替换为钻石
//            event.setModifiedThrownStack(new ItemStack(Items.DIAMOND, 1));
//        }
//    }

//    @SubscribeEvent
//    public static void onHatVacuumItemPre(HatVacuumItemEvent.Pre event) {
//        //将任何物品都替换为绿宝石
//        event.setModifiedVacuumedStack(new ItemStack(Items.EMERALD, 1));
//    }
    //HatConfettiEvent
    @SubscribeEvent
    public static void onHatVacuumItemPre(HatConfettiEvent.Pre event) {
        //阻止烟花
        event.setCanceled(true);
    }
}