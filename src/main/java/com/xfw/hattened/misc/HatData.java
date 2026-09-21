package com.xfw.hattened.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.init.HattenedDataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** 表示一顶已装备帽子的持久化及网络可见数据，包括佩戴状态和存储卡片。 */
public final class HatData {
    public static final HatData DEFAULT = new HatData(false, List.of());

    public static final Codec<HatData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.BOOL.fieldOf("hasHat").forGetter(HatData::hasHat),
                    Card.CODEC.listOf().fieldOf("storage").forGetter(HatData::getStorage)
            ).apply(builder, HatData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, HatData> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, HatData::hasHat,
            Card.STREAM_CODEC.apply(ByteBufCodecs.list()), HatData::getStorage,
            HatData::new
    );

    private final boolean hasHat;
    private final List<Card> storage;

    /** 创建未佩戴且不包含任何存储卡片的空帽子数据。 */
    public HatData() {
        this(false, List.of());
    }

    /** 复制卡片列表，创建佩戴状态与存储内容的不可变快照。 */
    public HatData(boolean hasHat, List<Card> storage) {
        this.hasHat = hasHat;
        this.storage = List.copyOf(storage);
    }

    /** 判断该快照是否表示玩家当前装备了一顶帽子。 */
    public boolean hasHat() {
        return hasHat;
    }

    /** 返回按照选择顺序保存的不可变卡片列表。 */
    public List<Card> getStorage() {
        return storage;
    }

    /** 创建帽子物品栈，并把当前快照的存储内容写入其数据组件。 */
    public ItemStack toItemStack() {
        ItemStack stack = new ItemStack(HattenedMain.HAT_ITEM.get());
        stack.set(HattenedDataComponents.HAT_STORAGE_COMPONENT.get(), storage);
        return stack;
    }

    /** 比较两个快照的佩戴状态和卡片内容是否完全相同。 */
    public boolean sameContent(HatData other) {
        if (hasHat != other.hasHat || storage.size() != other.storage.size()) {
            return false;
        }
        for (int index = 0; index < storage.size(); index++) {
            if (!storage.get(index).sameContent(other.storage.get(index))) {
                return false;
            }
        }
        return true;
    }
}
