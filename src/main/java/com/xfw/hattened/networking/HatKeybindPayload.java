package com.xfw.hattened.networking;

import com.xfw.hattened.HattenedMain;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record HatKeybindPayload(boolean pressed) implements CustomPacketPayload {
    public static final Type<HatKeybindPayload> TYPE = new Type<>(HattenedMain.id("hat_keybind"));

    public static final StreamCodec<FriendlyByteBuf, HatKeybindPayload> STREAM_CODEC = StreamCodec.composite(
            StreamCodec.of(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean), HatKeybindPayload::pressed,
            HatKeybindPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}