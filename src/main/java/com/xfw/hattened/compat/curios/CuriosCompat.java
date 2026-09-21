package com.xfw.hattened.compat.curios;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.init.HattenedDataComponents;
import com.xfw.hattened.init.HattenedSounds;
import com.xfw.hattened.misc.HatEquipmentBridge;
import com.xfw.hattened.misc.HattenedHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

/** 仅在 Curios 已加载时，让 hat 槽保存唯一帽子并与原生数据保持同步。 */
public final class CuriosCompat {
    private static final String HAT_SLOT = "hat";
    private static final CuriosHatBridge BRIDGE = new CuriosHatBridge();

    /** 安装 Curios 桥接器，并注册物品能力和手动取下事件。 */
    public static void init(IEventBus modEventBus) {
        HattenedHelper.setEquipmentBridge(BRIDGE);
        modEventBus.addListener(CuriosCompat::onCommonSetup);
        NeoForge.EVENT_BUS.addListener(CuriosCompat::onCurioChange);
    }

    /** 注册右键装备、装备音效和死亡保留规则。 */
    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CuriosApi.registerCurio(HattenedMain.HAT_ITEM.get(), new ICurioItem() {
            /** 允许槽内镜像继续存在；普通帽子只可在尚未佩戴时放入。 */
            @Override
            public boolean canEquip(SlotContext slotContext, ItemStack stack) {
                return HAT_SLOT.equals(slotContext.identifier())
                        && (isMirror(stack) || !hasNativeHat(slotContext));
            }

            /** 右键只允许装备普通帽子，防止内部镜像被当作第二顶帽子使用。 */
            @Override
            public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
                return !isMirror(stack) && canEquip(slotContext, stack);
            }

            /** 使用与原生帽子装备逻辑相同的音效。 */
            @Override
            public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
                return new ICurio.SoundInfo(HattenedSounds.HAT_EQUIP.get(), 1.0F, 1.0F);
            }

            /** 始终保留槽内唯一帽子，原生数据本身不会再生成另一件物品。 */
            @Override
            public ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source,
                                               boolean recentlyHit, ItemStack stack) {
                return ICurio.DropRule.ALWAYS_KEEP;
            }
        }));
    }

    /** 帽子离开 Curios 槽时清除原生佩戴状态，并移除物品上的内部镜像标记。 */
    private static void onCurioChange(CurioChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.isDeadOrDying()
                || !HAT_SLOT.equals(event.getIdentifier())
                || !event.getFrom().is(HattenedMain.HAT_ITEM.get())
                || event.getTo().is(HattenedMain.HAT_ITEM.get())) {
            return;
        }
        clearMirrorMarks(player);
        HattenedHelper.removeNativeHatFromExternalSlot(player);
    }

    /** 根据所在逻辑端读取帽子状态，供 Curios 的放入校验使用。 */
    private static boolean hasNativeHat(SlotContext slotContext) {
        if (slotContext.entity() instanceof ServerPlayer serverPlayer) {
            return HattenedHelper.getWornHatData(serverPlayer).hasHat();
        }
        return slotContext.entity() instanceof Player player
                && HattenedHelper.getHatData(player).hasHat();
    }

    /** 判断帽子是否属于原生数据在 Curios 槽中的唯一实体。 */
    private static boolean isMirror(ItemStack stack) {
        return stack.is(HattenedMain.HAT_ITEM.get())
                && stack.getOrDefault(HattenedDataComponents.CURIO_MIRROR_COMPONENT.get(), false);
    }

    /** 清除玩家当前容器、背包、光标和刚丢出帽子上的内部镜像标记。 */
    private static void clearMirrorMarks(ServerPlayer player) {
        player.containerMenu.slots.forEach(slot -> clearMirrorMark(slot.getItem()));
        for (int index = 0; index < player.getInventory().getContainerSize(); index++) {
            clearMirrorMark(player.getInventory().getItem(index));
        }
        clearMirrorMark(player.containerMenu.getCarried());
        player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(2.0),
                        entity -> isMirror(entity.getItem()))
                .forEach(entity -> clearMirrorMark(entity.getItem()));
    }

    /** 把已经离开 Curios 槽的帽子恢复为普通物品。 */
    private static void clearMirrorMark(ItemStack stack) {
        if (isMirror(stack)) {
            stack.remove(HattenedDataComponents.CURIO_MIRROR_COMPONENT.get());
        }
    }

    /** 阻止实例化只提供 Curios 初始化入口的工具类。 */
    private CuriosCompat() {
    }

    /** 管理 Curios 槽中的唯一帽子实体，并同步其可见状态。 */
    private static final class CuriosHatBridge implements HatEquipmentBridge {
        private final Set<ServerPlayer> playersWithHat =
                Collections.newSetFromMap(new WeakHashMap<>());

        /** 返回槽内唯一帽子；普通帽子会被标记，槽内帽子会反向恢复原生佩戴数据。 */
        @Override
        public ItemStack findHat(Player player) {
            Optional<HatSlot> optional = findHatSlot(player);
            if (optional.isEmpty()) {
                return ItemStack.EMPTY;
            }
            HatSlot slot = optional.get();
            if (player instanceof ServerPlayer serverPlayer) {
                if (!isMirror(slot.stack())) {
                    ItemStack marked = slot.stack().copy();
                    marked.set(HattenedDataComponents.CURIO_MIRROR_COMPONENT.get(), true);
                    slot.inventory().setEquippedCurio(HAT_SLOT, slot.index(), marked);
                    playersWithHat.add(serverPlayer);
                    removeMirrorCopiesFromInventory(serverPlayer);
                    return marked;
                }
                playersWithHat.add(serverPlayer);
                removeMirrorCopiesFromInventory(serverPlayer);
            }
            return slot.stack();
        }

        /** Curios 加载时始终提供需要与原生数据同步的 hat 槽。 */
        @Override
        public boolean hasExternalSlot() {
            return true;
        }

        /** 已记录的帽子从槽内消失时，将其识别为玩家手动取下。 */
        @Override
        public boolean consumeRemovedHat(ServerPlayer player) {
            if (findHatSlot(player).isPresent()) {
                playersWithHat.add(player);
                return false;
            }
            if (!playersWithHat.remove(player)) {
                return false;
            }
            clearMirrorMarks(player);
            return true;
        }

        /** 用原生数据更新槽内帽子；没有帽子时只向空 hat 槽放入一件。 */
        @Override
        public void setHat(ServerPlayer player, ItemStack hat) {
            if (!hat.is(HattenedMain.HAT_ITEM.get())) {
                return;
            }
            Optional<HatHandler> optional = findHandler(player);
            if (optional.isEmpty()) {
                return;
            }
            HatHandler handler = optional.get();
            Optional<HatSlot> existing = findHatSlot(handler);
            ItemStack synced = existing.map(HatSlot::stack).orElse(hat).copyWithCount(1);
            synced.set(HattenedDataComponents.HAT_STORAGE_COMPONENT.get(),
                    hat.getOrDefault(HattenedDataComponents.HAT_STORAGE_COMPONENT.get(), List.of()));
            synced.set(HattenedDataComponents.CURIO_MIRROR_COMPONENT.get(), true);

            if (existing.isPresent()) {
                existing.get().inventory().setEquippedCurio(HAT_SLOT, existing.get().index(), synced);
                playersWithHat.add(player);
                return;
            }
            var stacks = handler.stacksHandler().getStacks();
            for (int index = 0; index < stacks.getSlots(); index++) {
                if (stacks.getStackInSlot(index).isEmpty()) {
                    handler.inventory().setEquippedCurio(HAT_SLOT, index, synced);
                    playersWithHat.add(player);
                    return;
                }
            }
        }

        /** 从 Curios 槽取出唯一帽子，并把它恢复为普通物品。 */
        @Override
        public ItemStack removeHat(ServerPlayer player) {
            Optional<HatSlot> optional = findHatSlot(player);
            if (optional.isEmpty()) {
                return ItemStack.EMPTY;
            }
            HatSlot slot = optional.get();
            ItemStack removed = slot.stack().copy();
            clearMirrorMark(removed);
            playersWithHat.remove(player);
            slot.inventory().setEquippedCurio(HAT_SLOT, slot.index(), ItemStack.EMPTY);
            return removed;
        }

        /** 读取唯一帽子所在槽的可见开关。 */
        @Override
        public boolean shouldRenderHat(Player player) {
            return findHatSlot(player).map(HatSlot::visible).orElse(true);
        }

        /** 槽内唯一帽子存在时，删除旧实现遗留在普通背包中的带标记副本。 */
        private static void removeMirrorCopiesFromInventory(ServerPlayer player) {
            for (int index = 0; index < player.getInventory().getContainerSize(); index++) {
                if (isMirror(player.getInventory().getItem(index))) {
                    player.getInventory().setItem(index, ItemStack.EMPTY);
                }
            }
            if (isMirror(player.containerMenu.getCarried())) {
                player.containerMenu.setCarried(ItemStack.EMPTY);
            }
        }

        /** 获取玩家的 Curios hat 槽处理器。 */
        private static Optional<HatHandler> findHandler(Player player) {
            return CuriosApi.getCuriosInventory(player).flatMap(inventory ->
                    inventory.getStacksHandler(HAT_SLOT).map(handler ->
                            new HatHandler(inventory, handler)));
        }

        /** 查找玩家 hat 槽中的帽子物品。 */
        private static Optional<HatSlot> findHatSlot(Player player) {
            return findHandler(player).flatMap(CuriosHatBridge::findHatSlot);
        }

        /** 直接扫描真实 hat 槽，避免使用 Curios 的同 Tick 查询缓存。 */
        private static Optional<HatSlot> findHatSlot(HatHandler handler) {
            var stacks = handler.stacksHandler().getStacks();
            for (int index = 0; index < stacks.getSlots(); index++) {
                ItemStack stack = stacks.getStackInSlot(index);
                if (stack.is(HattenedMain.HAT_ITEM.get())) {
                    boolean visible = handler.stacksHandler().getRenders().size() <= index
                            || handler.stacksHandler().getRenders().get(index);
                    return Optional.of(new HatSlot(handler.inventory(), index, stack, visible));
                }
            }
            return Optional.empty();
        }

        /** 保存一次 Curios hat 槽处理器定位结果。 */
        private record HatHandler(ICuriosItemHandler inventory, ICurioStacksHandler stacksHandler) {
        }

        /** 保存一次 Curios 槽物品定位结果及其可见状态。 */
        private record HatSlot(ICuriosItemHandler inventory, int index, ItemStack stack, boolean visible) {
        }
    }
}
