package com.xfw.hattened.networking;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.misc.UserInput;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record HatInputPayload(UserInput input) implements CustomPacketPayload {
    public static final Type<HatInputPayload> TYPE = new Type<>(HattenedMain.id("hat_input"));

    public static final StreamCodec<FriendlyByteBuf, HatInputPayload> STREAM_CODEC = StreamCodec.composite(
        StreamCodec.of(
                FriendlyByteBuf::writeEnum,
            buf -> buf.readEnum(UserInput.class)
        ), HatInputPayload::input,
        HatInputPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}