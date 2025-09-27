package com.xfw.hattened.networking;

import com.xfw.hattened.HattenedMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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


    @OnlyIn(Dist.CLIENT)
    public static void handle(SuckItemPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.level.isClientSide) {
                Entity itemEntity = mc.level.getEntity(payload.itemEntityId());
                Entity collector = mc.level.getEntity(payload.playerId());
                if (itemEntity instanceof ItemEntity item && collector != null) {
                    mc.particleEngine.add(new ItemPickupParticle(mc.getEntityRenderDispatcher(), mc.renderBuffers(), mc.level, itemEntity, collector));
                    mc.level.playLocalSound(
                            collector.getX(), collector.getY(), collector.getZ(),
                            SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                            0.2F,
                            (HattenedMain.RANDOM.nextFloat() - HattenedMain.RANDOM.nextFloat()) * 1.4F + 2.0F,
                            false
                    );

                    if (!item.getItem().isEmpty()) {
                        item.getItem().shrink(1);
                    }
                    if (item.getItem().isEmpty()) {
                        item.discard();
                    }
                }
            }
        });
    }

}