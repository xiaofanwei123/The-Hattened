package com.xfw.hattened.networking;

import com.xfw.hattened.HattenedMain;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SuckItemPayload(int itemEntityId, int playerId) implements CustomPacketPayload {
    public static final Type<SuckItemPayload> TYPE = new Type<>(HattenedMain.id("suck_item"));
    
    public static final StreamCodec<FriendlyByteBuf, SuckItemPayload> STREAM_CODEC = StreamCodec.composite(
        StreamCodec.of(FriendlyByteBuf::writeInt, FriendlyByteBuf::readInt), SuckItemPayload::itemEntityId,
        StreamCodec.of(FriendlyByteBuf::writeInt, FriendlyByteBuf::readInt), SuckItemPayload::playerId,
        SuckItemPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}