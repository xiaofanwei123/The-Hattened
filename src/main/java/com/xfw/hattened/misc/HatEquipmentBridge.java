package com.xfw.hattened.misc;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** 为 Curios 等可选装备界面定义桥接接口，原生数据与唯一槽位物品通过它同步。 */
public interface HatEquipmentBridge {
    HatEquipmentBridge NONE = new HatEquipmentBridge() {
        /** 未加载外部装备模组时始终找不到外部帽子。 */
        @Override
        public ItemStack findHat(Player player) {
            return ItemStack.EMPTY;
        }
    };

    /** 查找外部界面槽位中唯一的帽子物品。 */
    ItemStack findHat(Player player);

    /** 判断当前是否存在需要同步的外部帽子槽。 */
    default boolean hasExternalSlot() {
        return false;
    }

    /** 检查外部槽位中的帽子是否刚被玩家手动取下。 */
    default boolean consumeRemovedHat(ServerPlayer player) {
        return false;
    }

    /** 创建或更新外部槽位中的唯一帽子物品。 */
    default void setHat(ServerPlayer player, ItemStack hat) {
    }

    /** 从外部槽位移除并返回唯一帽子物品。 */
    default ItemStack removeHat(ServerPlayer player) {
        return ItemStack.EMPTY;
    }

    /** 读取外部界面的可见开关；未加载外部装备模组时默认显示。 */
    default boolean shouldRenderHat(Player player) {
        return true;
    }
}
