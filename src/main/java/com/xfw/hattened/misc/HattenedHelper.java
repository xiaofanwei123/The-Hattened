package com.xfw.hattened.misc;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.init.HattenedAttachments;
import com.xfw.hattened.init.HattenedDataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** 集中管理原生帽子数据、存储操作以及服务端到客户端的可见数据同步。 */
public final class HattenedHelper {
    private static HatEquipmentBridge equipmentBridge = HatEquipmentBridge.NONE;

    /** 安装已加载装备模组提供的界面桥接器。 */
    public static void setEquipmentBridge(HatEquipmentBridge bridge) {
        equipmentBridge = bridge;
    }

    /** 返回渲染与界面代码使用的已同步帽子数据视图。 */
    public static HatData getHatData(Player player) {
        return player.getData(HattenedAttachments.HAT_VIEW.get());
    }

    /** 判断当前帽子模型是否应显示；没有外部界面时始终显示。 */
    public static boolean shouldRenderHat(Player player) {
        return equipmentBridge.shouldRenderHat(player);
    }

    /** 返回服务端原生附件保存的真实帽子数据。 */
    public static HatData getWornHatData(ServerPlayer player) {
        return player.getData(HattenedAttachments.HAT_DATA.get());
    }

    /** 从物品栈读取存储内容并装备到模组自带帽子槽位。 */
    public static void equipNativeHat(ServerPlayer player, ItemStack stack) {
        List<Card> storage = stack.getOrDefault(HattenedDataComponents.HAT_STORAGE_COMPONENT.get(), List.of());
        player.setData(HattenedAttachments.HAT_DATA.get(), new HatData(true, storage));
        equipmentBridge.setHat(player, player.getData(HattenedAttachments.HAT_DATA.get()).toItemStack());
        refreshHatView(player);
    }

    /** 脱下当前帽子，并优先返回 Curios 槽中保存的完整物品栈。 */
    public static ItemStack removeWornHat(ServerPlayer player) {
        HatData nativeHat = player.getData(HattenedAttachments.HAT_DATA.get());
        if (!nativeHat.hasHat()) {
            return ItemStack.EMPTY;
        }
        ItemStack external = equipmentBridge.removeHat(player);
        ItemStack removed = external.is(HattenedMain.HAT_ITEM.get()) ? external : nativeHat.toItemStack();
        player.setData(HattenedAttachments.HAT_DATA.get(), HatData.DEFAULT);
        resetControl(player);
        refreshHatView(player);
        return removed;
    }

    /** 将新存储写回原生数据和 Curios 槽，并返回本次写入是否成功。 */
    public static boolean setWornStorage(ServerPlayer player, List<Card> storage) {
        HatData nativeHat = player.getData(HattenedAttachments.HAT_DATA.get());
        if (!nativeHat.hasHat()) {
            return false;
        }
        player.setData(HattenedAttachments.HAT_DATA.get(), new HatData(true, storage));
        equipmentBridge.setHat(player, player.getData(HattenedAttachments.HAT_DATA.get()).toItemStack());
        refreshHatView(player);
        return true;
    }

    /** Curios 槽中的帽子被手动取下时清空原生数据，并立即结束所有帽子操作。 */
    public static void removeNativeHatFromExternalSlot(ServerPlayer player) {
        if (!player.getData(HattenedAttachments.HAT_DATA.get()).hasHat()) {
            return;
        }
        player.setData(HattenedAttachments.HAT_DATA.get(), HatData.DEFAULT);
        resetControl(player);
        refreshHatView(player);
    }

    /** 同步外部槽位与原生帽子数据，再更新客户端视图。 */
    public static HatData refreshHatView(ServerPlayer player) {
        if (equipmentBridge.consumeRemovedHat(player)) {
            player.setData(HattenedAttachments.HAT_DATA.get(), HatData.DEFAULT);
            resetControl(player);
        }
        syncExternalHat(player);
        HatData effective = player.getData(HattenedAttachments.HAT_DATA.get());
        HatData previous = player.getData(HattenedAttachments.HAT_VIEW.get());
        if (!previous.sameContent(effective)) {
            player.setData(HattenedAttachments.HAT_VIEW.get(), effective);
        }
        return effective;
    }

    /** 让 Curios 槽中的唯一物品和原生数据保持一致，并接收界面中的直接修改。 */
    private static void syncExternalHat(ServerPlayer player) {
        HatData nativeHat = player.getData(HattenedAttachments.HAT_DATA.get());
        ItemStack external = equipmentBridge.findHat(player);
        if (external.is(HattenedMain.HAT_ITEM.get())) {
            HatData changed = new HatData(true,
                    external.getOrDefault(HattenedDataComponents.HAT_STORAGE_COMPONENT.get(), List.of()));
            if (!nativeHat.sameContent(changed)) {
                player.setData(HattenedAttachments.HAT_DATA.get(), changed);
            }
            return;
        }
        if (nativeHat.hasHat() && equipmentBridge.hasExternalSlot()) {
            equipmentBridge.setHat(player, nativeHat.toItemStack());
        }
    }

    /** 返回玩家当前已同步的帽子动画姿态。 */
    public static HatPose getPose(Player player) {
        return player.getData(HattenedAttachments.HAT_POSE.get());
    }

    /** 仅在目标姿态与当前姿态不同时写入附件并同步。 */
    public static void setPose(ServerPlayer player, HatPose pose) {
        if (player.getData(HattenedAttachments.HAT_POSE.get()) != pose) {
            player.setData(HattenedAttachments.HAT_POSE.get(), pose);
        }
    }

    /** 返回玩家仅服务端保存的帽子按键状态。 */
    public static HatControlState getControl(ServerPlayer player) {
        return player.getData(HattenedAttachments.HAT_CONTROL.get());
    }

    /** 用给定状态替换玩家仅服务端保存的帽子按键状态。 */
    public static void setControl(ServerPlayer player, HatControlState control) {
        player.setData(HattenedAttachments.HAT_CONTROL.get(), control);
    }

    /** 清除所有持续按键状态，并把帽子恢复为头顶静止姿态。 */
    public static void resetControl(ServerPlayer player) {
        setControl(player, HatControlState.IDLE);
        setPose(player, HatPose.ON_HEAD);
    }

    /** 将来源物品栈尽可能合并或追加到不可变卡片列表，并返回新列表和剩余物品。 */
    public static List<Card> insertStack(List<Card> storage, ItemStack source) {
        List<Card> result = new ArrayList<>(storage);
        for (int index = 0; index < result.size() && !source.isEmpty(); index++) {
            Card card = result.get(index);
            ItemStack stored = card.getStack();
            if (!ItemStack.isSameItemSameComponents(stored, source)) {
                continue;
            }
            int transferAmount = Math.min(stored.getMaxStackSize() - stored.getCount(), source.getCount());
            if (transferAmount > 0) {
                source.shrink(transferAmount);
                stored.grow(transferAmount);
                result.set(index, card.withStack(stored));
            }
        }
        if (!source.isEmpty()) {
            result.add(new Card(source.copyAndClear()));
        }
        return List.copyOf(result);
    }

    /** 从首张选中卡片取出一个物品，并在仍有剩余时保留其卡片标识。 */
    public static List<Card> removeOneFromFirst(List<Card> storage) {
        if (storage.isEmpty()) {
            return storage;
        }
        List<Card> result = new ArrayList<>(storage);
        Card first = result.getFirst();
        ItemStack remaining = first.getStack();
        remaining.shrink(1);
        if (remaining.isEmpty()) {
            result.removeFirst();
        } else {
            result.set(0, first.withStack(remaining));
        }
        return List.copyOf(result);
    }

    /** 把当前首张卡片轮换到列表末尾，从而选择下一张卡片。 */
    public static <E> List<E> rotateLeft(List<E> list) {
        if (list.isEmpty()) {
            return list;
        }
        List<E> result = new ArrayList<>(list.subList(1, list.size()));
        result.add(list.getFirst());
        return List.copyOf(result);
    }

    /** 把列表末尾卡片轮换到最前方，从而选择上一张卡片。 */
    public static <E> List<E> rotateRight(List<E> list) {
        if (list.isEmpty()) {
            return list;
        }
        List<E> result = new ArrayList<>(list.size());
        result.add(list.getLast());
        result.addAll(list.subList(0, list.size() - 1));
        return List.copyOf(result);
    }

    /** 阻止实例化只提供帽子公共数据操作的工具类。 */
    private HattenedHelper() {
    }
}
