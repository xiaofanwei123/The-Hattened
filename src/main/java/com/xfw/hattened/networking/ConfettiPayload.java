package com.xfw.hattened.networking;

import com.xfw.hattened.HattenedMain;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

public record ConfettiPayload(Vec3 position, Vec3 direction) implements CustomPacketPayload {
    public static final Type<ConfettiPayload> TYPE = new Type<>(HattenedMain.id("confetti"));

    public static final StreamCodec<FriendlyByteBuf, Vec3> VEC3_STREAM_CODEC = StreamCodec.of(
            FriendlyByteBuf::writeVec3,
            FriendlyByteBuf::readVec3
    );

    public static final StreamCodec<FriendlyByteBuf, Long> LONG_CODEC = StreamCodec.of(
            FriendlyByteBuf::writeLong,
            FriendlyByteBuf::readLong
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
}