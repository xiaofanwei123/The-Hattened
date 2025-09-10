package com.xfw.hattened.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class Card {
    public static final Codec<Card> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    ItemStack.CODEC.fieldOf("stack").forGetter(Card::getStack),
                    UUIDUtil.CODEC.fieldOf("uuid").forGetter(Card::getUuid)
            ).apply(builder, Card::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, Card> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, Card::getStack,
            ByteBufCodecs.fromCodec(UUIDUtil.CODEC), Card::getUuid,
            Card::new
    );

    private final ItemStack stack;
    private final UUID uuid;
    private boolean mutated = false;
    private Card replacement = null;

    public Card(ItemStack stack) {
        this(stack, UUID.randomUUID());
    }

    public Card(ItemStack stack, UUID uuid) {
        this.stack = stack;
        this.uuid = uuid;
    }

    public ItemStack getStack() {
        return stack;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isMutated() {
        return mutated;
    }

    public Card getReplacement() {
        return replacement;
    }

    public void markDirty() {
        this.mutated = true;
        this.replacement = this.stack.isEmpty() ? null : new Card(this.stack, this.uuid);
    }
}