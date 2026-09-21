package com.xfw.hattened.networking;

import com.xfw.hattened.HattenedMain;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

/** 携带五彩纸屑特效的生成位置和喷射方向，供客户端还原效果。 */
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

    /** 返回该载荷注册时使用的类型标识，供网络系统识别消息。 */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
