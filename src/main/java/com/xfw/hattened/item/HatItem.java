package com.xfw.hattened.item;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.component.TooltipDisplayComponent;
import com.xfw.hattened.init.HattenedDataComponents;
import com.xfw.hattened.init.HattenedSounds;
import com.xfw.hattened.misc.Card;
import com.xfw.hattened.misc.HattenedHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/** 可穿戴的帽子物品，使用数据组件按顺序保存代表物品栈的卡片。 */
public class HatItem extends Item {
    /** 创建不可堆叠且默认存储为空的帽子物品。 */
    public HatItem() {
        super(new Properties()
                .stacksTo(1)
                .component(HattenedDataComponents.HAT_STORAGE_COMPONENT.get(), List.of()));
    }

    /** 当 Curios 未接管右键操作时，将手中帽子装备到模组自带槽位。 */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ItemStack newHat = held.copy();
            ItemStack oldHat = HattenedHelper.removeWornHat(serverPlayer);
            HattenedHelper.equipNativeHat(serverPlayer, newHat);
            player.setItemInHand(hand, oldHat);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    HattenedSounds.HAT_EQUIP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    /** 处理玩家拿着帽子右键其他背包槽位，将目标物品存进帽子。 */
    @Override
    public boolean overrideStackedOnOther(ItemStack hat, Slot slot, ClickAction clickType, Player player) {
        ItemStack clickedStack = slot.getItem();
        if (clickedStack.is(HattenedMain.HAT_ITEM.get())) {
            return false;
        }
        if (clickType == ClickAction.PRIMARY && !clickedStack.isEmpty()) {
            insertItem(hat, clickedStack, player);
            slot.set(ItemStack.EMPTY);
            return true;
        }
        if (clickType == ClickAction.SECONDARY && clickedStack.isEmpty() && !getStorage(hat).isEmpty()) {
            slot.set(removeItem(hat, player));
            return true;
        }
        return false;
    }

    /** 处理玩家拿着其他物品右键槽内帽子，将手中物品存进帽子。 */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack hat, ItemStack clickedStack, Slot slot,
                                            ClickAction clickType, Player player, SlotAccess cursorStackReference) {
        if (clickedStack.is(HattenedMain.HAT_ITEM.get())) {
            return false;
        }
        if (clickType == ClickAction.PRIMARY && !clickedStack.isEmpty()) {
            insertItem(hat, clickedStack, player);
            cursorStackReference.set(ItemStack.EMPTY);
            return true;
        }
        if (clickType == ClickAction.SECONDARY && clickedStack.isEmpty() && !getStorage(hat).isEmpty()) {
            cursorStackReference.set(removeItem(hat, player));
            return true;
        }
        return false;
    }

    /** 向物品工具提示提供帽子内容快照，用于绘制存储卡片条。 */
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        TooltipDisplayComponent display = stack.getOrDefault(
                HattenedDataComponents.TOOLTIP_DISPLAY.get(), TooltipDisplayComponent.DEFAULT);
        List<Card> storage = getStorage(stack);
        if (!display.shouldDisplay(HattenedDataComponents.HAT_STORAGE_COMPONENT.get()) || storage.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new HatTooltipData(storage));
    }

    /** 尽可能把给定物品栈移入帽子的不可变存储组件，并返回实际变化量。 */
    public void insertItem(ItemStack hat, ItemStack stack, Player player) {
        hat.set(HattenedDataComponents.HAT_STORAGE_COMPONENT.get(), HattenedHelper.insertStack(getStorage(hat), stack));
        player.playSound(HattenedSounds.INSERT_ITEM.get(), 0.8F,
                0.8F + player.level().random.nextFloat() * 0.4F);
    }

    /** 从帽子中移除第一张卡片，并返回该卡片保存的完整物品栈。 */
    public ItemStack removeItem(ItemStack hat, Player player) {
        List<Card> storage = getStorage(hat);
        if (storage.isEmpty()) {
            return ItemStack.EMPTY;
        }
        player.playSound(HattenedSounds.REMOVE_ITEM.get(), 0.8F,
                0.8F + player.level().random.nextFloat() * 0.4F);
        ItemStack extracted = storage.getFirst().getStack();
        hat.set(HattenedDataComponents.HAT_STORAGE_COMPONENT.get(), List.copyOf(storage.subList(1, storage.size())));
        return extracted;
    }

    /** 读取帽子的存储组件；组件缺失时返回不可变的空列表。 */
    private static List<Card> getStorage(ItemStack hat) {
        return hat.getOrDefault(HattenedDataComponents.HAT_STORAGE_COMPONENT.get(), List.of());
    }

    /** 将公共物品 API 提供的卡片数据传给客户端工具提示渲染器。 */
    public static final class HatTooltipData implements TooltipComponent {
        private final List<Card> storage;

        /** 复制卡片列表，创建不会被后续存储变化影响的工具提示快照。 */
        public HatTooltipData(List<Card> storage) {
            this.storage = List.copyOf(storage);
        }

        /** 返回需要按存储顺序绘制的不可变卡片列表。 */
        public List<Card> getStorage() {
            return storage;
        }
    }
}
