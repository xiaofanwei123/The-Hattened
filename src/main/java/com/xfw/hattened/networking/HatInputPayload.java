package com.xfw.hattened.networking;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.misc.UserInput;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** 将一次帽子控制按键的状态变化发送给服务端。 */
public record HatInputPayload(UserInput input) implements CustomPacketPayload {
    public static final Type<HatInputPayload> TYPE = new Type<>(HattenedMain.id("hat_input"));

    public static final StreamCodec<FriendlyByteBuf, HatInputPayload> STREAM_CODEC = StreamCodec.composite(
        StreamCodec.of(
                FriendlyByteBuf::writeEnum,
            buf -> buf.readEnum(UserInput.class)
        ), HatInputPayload::input,
        HatInputPayload::new
    );

    /** 返回该载荷注册时使用的类型标识，供网络系统识别消息。 */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
