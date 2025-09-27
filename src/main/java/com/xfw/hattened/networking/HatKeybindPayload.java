package com.xfw.hattened.networking;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.client.sound.HattenedSounds;
import com.xfw.hattened.misc.HatData;
import com.xfw.hattened.misc.HattenedHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

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

    public static void handle(HatKeybindPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            HatData hat = HattenedHelper.getHatData(player);
            if (player.getMainHandItem().isEmpty() && hat.hasHat()) {
                player.swing(InteractionHand.MAIN_HAND);
                player.setItemInHand(InteractionHand.MAIN_HAND, hat.toItemStack());
                HattenedHelper.setHatData((ServerPlayer) player, new HatData(false,  List.of()));
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), HattenedSounds.HAT_EQUIP, SoundSource.PLAYERS, 1f, 1f);
            }
        });
    }
}