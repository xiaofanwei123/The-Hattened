package com.xfw.hattened.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.UUID;

/** 保存一个物品栈的不可变卡片，并用稳定标识维持客户端动画连续性。 */
public final class Card {
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

    /** 为待存储物品栈创建具有新随机标识的卡片。 */
    public Card(ItemStack stack) {
        this(stack, UUID.randomUUID());
    }

    /** 使用已有标识重建卡片，使编解码后仍能对应同一动画控件。 */
    public Card(ItemStack stack, UUID uuid) {
        this.stack = stack.copy();
        this.uuid = Objects.requireNonNull(uuid);
    }

    /** 返回所存物品栈的防御性副本，避免调用方修改卡片内部数据。 */
    public ItemStack getStack() {
        return stack.copy();
    }

    /** 返回客户端匹配动画控件时使用的稳定卡片标识。 */
    public UUID getUuid() {
        return uuid;
    }

    /** 用新物品栈创建替代卡片，同时保留原动画标识。 */
    public Card withStack(ItemStack replacement) {
        return new Card(replacement, uuid);
    }

    /** 比较卡片的动画标识以及物品栈类型、数量和组件是否一致。 */
    public boolean sameContent(Card other) {
        return uuid.equals(other.uuid) && ItemStack.matches(stack, other.stack);
    }

    /** 按稳定标识和完整物品栈内容判断两个卡片对象是否相等。 */
    @Override
    public boolean equals(Object object) {
        return object instanceof Card other && sameContent(other);
    }

    /** 根据稳定标识和物品栈内容生成与相等判断一致的哈希值。 */
    @Override
    public int hashCode() {
        return 31 * uuid.hashCode() + ItemStack.hashItemAndComponents(stack) + stack.getCount();
    }
}
