package com.xfw.hattened.init;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.client.ClientStorage;
import com.xfw.hattened.event.HatConfettiEvent;
import com.xfw.hattened.misc.HatData;
import com.xfw.hattened.misc.HattenedHelper;
import com.xfw.hattened.misc.ServerPlayerEntityMinterface;
import com.xfw.hattened.misc.UserInput;
import com.xfw.hattened.networking.ConfettiPayload;
import com.xfw.hattened.networking.HatDataSyncPayload;
import com.xfw.hattened.networking.HatInputPayload;
import com.xfw.hattened.networking.HatKeybindPayload;
import com.xfw.hattened.networking.SuckItemPayload;
import com.xfw.hattened.client.sound.HattenedSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;

public class HattenedNetworking {
    
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        
        //中键向附近所有玩家发送彩色纸屑效果
        registrar.playToServer(
                HatInputPayload.TYPE,
                HatInputPayload.STREAM_CODEC,
                (payload, context) -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    ((ServerPlayerEntityMinterface) player).queueUserInput(payload.input());
                    if (payload.input() == UserInput.MIDDLE_MOUSE_PRESSED) {
                        HatConfettiEvent.Pre confettiPreEvent = new HatConfettiEvent.Pre(player);
                        //触发烟花前事件
                        if (!NeoForge.EVENT_BUS.post(confettiPreEvent).isCanceled()) {
                            player.swing(InteractionHand.MAIN_HAND,true);
                            ConfettiPayload confettiPayload = new ConfettiPayload(
                                    player.position().add(0.0, 0.75, 0.0),
                                    player.getLookAngle()
                            );
                            PacketDistributor.sendToPlayersNear(
                                    player.serverLevel(),
                                    null,
                                    player.getX(), player.getY(), player.getZ(),
                                    32.0,
                                    confettiPayload
                            );
                            //触发烟花后事件
                            HatConfettiEvent.Post confettiPostEvent = new HatConfettiEvent.Post(player);
                            NeoForge.EVENT_BUS.post(confettiPostEvent);
                        }
                    }
                }
        );

        //H脱帽子
        //将玩家的Attachments转化为数据组件存到物品里面
        registrar.playToServer(
            HatKeybindPayload.TYPE,
            HatKeybindPayload.STREAM_CODEC,
            (payload, context) -> {
                Player player = context.player();
                HatData hat = HattenedHelper.getHatData(player);
                if (player.getMainHandItem().isEmpty() && hat.hasHat()) {
                    player.swing(InteractionHand.MAIN_HAND);
                    player.setItemInHand(InteractionHand.MAIN_HAND, hat.toItemStack());
                    HattenedHelper.setHatData((ServerPlayer) player, new HatData(false,  List.of()));
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), HattenedSounds.HAT_EQUIP, SoundSource.PLAYERS, 1f, 1f);
                }
            }
        );

    }

}