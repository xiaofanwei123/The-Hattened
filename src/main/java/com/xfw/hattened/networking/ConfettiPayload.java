package com.xfw.hattened.networking;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.client.sound.HattenedSounds;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Random;

public record ConfettiPayload(Vec3 position, Vec3 direction) implements CustomPacketPayload {
    public static final Type<ConfettiPayload> TYPE = new Type<>(HattenedMain.id("confetti"));

    public static final StreamCodec<FriendlyByteBuf, Vec3> VEC3_STREAM_CODEC = StreamCodec.of(
            FriendlyByteBuf::writeVec3,
            FriendlyByteBuf::readVec3
    );

    public static final StreamCodec<FriendlyByteBuf, ConfettiPayload> STREAM_CODEC = StreamCodec.composite(
            VEC3_STREAM_CODEC, ConfettiPayload::position,
            VEC3_STREAM_CODEC, ConfettiPayload::direction,
            ConfettiPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handle(ConfettiPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Vec3 pos = payload.position();
            Vec3 dir = payload.direction();
            player.level().playSound(
                    player,
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
                player.level().addParticle(
                        confettiParticle,
                        pos.x, pos.y, pos.z,
                        velocity.x, velocity.y, velocity.z
                );
            }
        }
    );}
}