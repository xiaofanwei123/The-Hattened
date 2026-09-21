package com.xfw.hattened.client;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.client.particle.HatItemPickupParticle;
import com.xfw.hattened.init.HattenedSounds;
import com.xfw.hattened.networking.ConfettiPayload;
import com.xfw.hattened.networking.SuckItemPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Random;

/** 只在物理客户端处理帽子动作对应的视觉和声音网络消息。 */
public final class HattenedClientPayloadHandler {
    /** 播放物品飞向玩家的吸取动画，并在客户端移除已被服务端收走的实体。 */
    public static void handleSuckItem(SuckItemPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity itemEntity = minecraft.level.getEntity(payload.itemEntityId());
        Entity collector = minecraft.level.getEntity(payload.playerId());
        if (!(itemEntity instanceof ItemEntity item) || collector == null) {
            return;
        }

        minecraft.particleEngine.add(new HatItemPickupParticle(
                minecraft.getEntityRenderDispatcher(), minecraft.renderBuffers(), minecraft.level, itemEntity, collector));
        minecraft.level.playLocalSound(
                collector.getX(), collector.getY(), collector.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                (HattenedMain.RANDOM.nextFloat() - HattenedMain.RANDOM.nextFloat()) * 1.4F + 2.0F,
                false
        );
        item.getItem().shrink(1);
        if (item.getItem().isEmpty()) {
            item.discard();
        }
    }

    /** 按服务端给出的方向生成五彩纸屑，并播放对应发射声音。 */
    public static void handleConfetti(ConfettiPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        Vec3 position = payload.position();
        Vec3 direction = payload.direction();
        minecraft.level.playSound(
                minecraft.player, position.x, position.y, position.z,
                HattenedSounds.HAT_CONFETTI.get(), SoundSource.MASTER, 1.0F, 1.0F
        );

        Random random = HattenedMain.RANDOM;
        SimpleParticleType particle = HattenedMain.CONFETTI_PARTICLE.get();
        for (int index = 0; index < 100; index++) {
            double offsetX = (random.nextDouble() * 2.0 - 1.0) / 5.0;
            double offsetY = (random.nextDouble() * 2.0 - 1.0) / 5.0;
            double offsetZ = (random.nextDouble() * 2.0 - 1.0) / 5.0;
            Vec3 velocity = direction.add(offsetX, offsetY, offsetZ).scale(2.0);
            minecraft.level.addParticle(particle, position.x, position.y, position.z,
                    velocity.x, velocity.y, velocity.z);
        }
    }

    /** 阻止实例化只提供客户端消息处理入口的工具类。 */
    private HattenedClientPayloadHandler() {
    }
}
