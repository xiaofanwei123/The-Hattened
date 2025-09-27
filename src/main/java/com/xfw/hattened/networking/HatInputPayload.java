package com.xfw.hattened.networking;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.event.HatConfettiEvent;
import com.xfw.hattened.misc.ServerPlayerEntityMinterface;
import com.xfw.hattened.misc.UserInput;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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

    public static void handle(HatInputPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ((ServerPlayerEntityMinterface) player).queueUserInput(payload.input());
            if (payload.input() == UserInput.MIDDLE_MOUSE_PRESSED) {
                HatConfettiEvent.Pre confettiPreEvent = new HatConfettiEvent.Pre(player);
                //触发烟花前事件
                if (!NeoForge.EVENT_BUS.post(confettiPreEvent).isCanceled()) {
                    player.swing(InteractionHand.MAIN_HAND,true);
                    ConfettiPayload confettiPayload = new ConfettiPayload(
                            player.position().add(0.0, 0.75, 0.0),
                            player.getLookAngle()
                    );
                    PacketDistributor.sendToPlayersNear(
                            player.serverLevel(),
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            32.0,
                            confettiPayload
                    );
                    //触发烟花后事件
                    HatConfettiEvent.Post confettiPostEvent = new HatConfettiEvent.Post(player);
                    NeoForge.EVENT_BUS.post(confettiPostEvent);
                }
            }
        });
    }
}