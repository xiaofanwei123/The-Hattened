package com.xfw.hattened.networking;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.client.ClientStorage;
import com.xfw.hattened.misc.HatData;
import com.xfw.hattened.misc.HatPose;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record HatDataSyncPayload(int playerId, HatData hatData, HatPose hatPose) implements CustomPacketPayload {
    public static final Type<HatDataSyncPayload> TYPE = new Type<>(HattenedMain.id("hat_data_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HatDataSyncPayload> STREAM_CODEC = StreamCodec.composite(
        StreamCodec.of(RegistryFriendlyByteBuf::writeInt, RegistryFriendlyByteBuf::readInt), HatDataSyncPayload::playerId,
        HatData.PACKET_CODEC, HatDataSyncPayload::hatData,
        StreamCodec.of(
            (buf, pose) -> buf.writeEnum(pose),
            buf -> buf.readEnum(HatPose.class)
        ), HatDataSyncPayload::hatPose,
        HatDataSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HatDataSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientStorage.setHatData(payload.playerId(), payload.hatData());
            ClientStorage.setHatPose(payload.playerId(), payload.hatPose());
        });
    }
}