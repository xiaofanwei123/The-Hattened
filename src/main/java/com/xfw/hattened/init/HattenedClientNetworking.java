package com.xfw.hattened.init;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.client.ClientStorage;
import com.xfw.hattened.client.sound.HattenedSounds;
import com.xfw.hattened.networking.ConfettiPayload;
import com.xfw.hattened.networking.HatDataSyncPayload;
import com.xfw.hattened.networking.SuckItemPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class HattenedClientNetworking {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        //吸物品
        registrar.playToClient(
                SuckItemPayload.TYPE,
                SuckItemPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.level != null) {
                            Entity itemEntity = mc.level.getEntity(payload.itemEntityId());
                            Entity collector = mc.level.getEntity(payload.playerId());
                            if (itemEntity instanceof ItemEntity item && collector != null) {
                                mc.particleEngine.add(new ItemPickupParticle(mc.getEntityRenderDispatcher(),mc.renderBuffers(),mc.level, itemEntity,collector));
                                mc.level.playLocalSound(
                                        collector.getX(), collector.getY(), collector.getZ(),
                                        SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                                        0.2F,
                                        (HattenedMain.RANDOM.nextFloat() - HattenedMain.RANDOM.nextFloat()) * 1.4F + 2.0F,
                                        false
                                );

                                if (!item.getItem().isEmpty()) {
                                    item.getItem().shrink(1);
                                }
                                if (item.getItem().isEmpty()) {
                                    item.discard();
                                }
                            }
                        }
                    });
                }
        );

        //播放撒烟花
        registrar.playToClient(
                ConfettiPayload.TYPE,
                ConfettiPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.level != null && mc.player != null) {
                            Vec3 pos = payload.position();
                            Vec3 dir = payload.direction();
                            mc.level.playSound(
                                    mc.player,
                                    pos.x, pos.y, pos.z,
                                    HattenedSounds.HAT_CONFETTI.get(),
                                    SoundSource.MASTER,
                                    1.0f, 1.0f
                            );
                            Random random = HattenedMain.RANDOM;
                            SimpleParticleType confettiParticle = HattenedMain.CONFETTI_PARTICLE.get();
                            for (int i = 0; i < 100; i++) {
                                double offsetX = (random.nextDouble() * 2 - 1) / 5.0;
                                double offsetY = (random.nextDouble() * 2 - 1) / 5.0;
                                double offsetZ = (random.nextDouble() * 2 - 1) / 5.0;
                                Vec3 velocity = dir.add(offsetX, offsetY, offsetZ).scale(2.0);
                                mc.level.addParticle(
                                        confettiParticle,
                                        pos.x, pos.y, pos.z,
                                        velocity.x, velocity.y, velocity.z
                                );
                            }
                        }
                    });
                }
        );

        //渲染帽子
        registrar.playToClient(
                HatDataSyncPayload.TYPE,
                HatDataSyncPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        ClientStorage.setHatData(payload.playerId(), payload.hatData());
                        ClientStorage.setHatPose(payload.playerId(), payload.hatPose());
                    });
                }
        );
    }

}