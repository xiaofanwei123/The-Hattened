package com.xfw.hattened.item;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.client.tooltipComponent.TooltipDisplayComponent;
import com.xfw.hattened.client.sound.HattenedSounds;
import com.xfw.hattened.misc.Card;
import com.xfw.hattened.misc.HatData;
import com.xfw.hattened.misc.HattenedHelper;
import net.minecraft.server.level.ServerPlayer;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HatItem extends Item {

    public HatItem() {
        super(new Properties()
                .stacksTo(1)
                .component(HattenedMain.HAT_STORAGE_COMPONENT.get(), Collections.emptyList())
        );
    }

    //将帽子的物品转为Attachments存到玩家身上(不占用物品栏位)
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        HatData currentHatData = HattenedHelper.getHatData(user);
        ItemStack oldHat = currentHatData.hasHat() ? currentHatData.toItemStack() : ItemStack.EMPTY;
        user.playSound(HattenedSounds.HAT_EQUIP.get(), 1f, 1f);
        if (!world.isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) user;
            ItemStack newHat = user.getItemInHand(hand);
            List<Card> storage = newHat.getOrDefault(HattenedMain.HAT_STORAGE_COMPONENT.get(), Collections.emptyList());
            HattenedHelper.setHatData(serverPlayer, new HatData(true, storage));
            user.setItemInHand(hand, oldHat);
        }

        return InteractionResultHolder.success(oldHat);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack hat, Slot slot, ClickAction clickType, Player player) {
        ItemStack clickedStack = slot.getItem();
        if(clickedStack.is(HattenedMain.HAT_ITEM.get())) return false;
        //拿起帽子左键空地方会吸取槽位的物品
        if (clickType == ClickAction.PRIMARY && !clickedStack.isEmpty()) {
            insertItem(hat, clickedStack, player);
            slot.set(ItemStack.EMPTY);
            return true;
        }

        //拿起帽子右键空地方会放下里面的物品
        if (clickType == ClickAction.SECONDARY && clickedStack.isEmpty() &&
                !hat.getOrDefault(HattenedMain.HAT_STORAGE_COMPONENT.get(), Collections.emptyList()).isEmpty()) {
            ItemStack extracted = removeItem(hat, player);
            if (extracted != null) {
                slot.set(extracted);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack hat, ItemStack clickedStack, Slot slot,
                                            ClickAction clickType, Player player, SlotAccess cursorStackReference) {
        if(clickedStack.is(HattenedMain.HAT_ITEM.get())) return false;

        List<Card> existing = hat.getOrDefault(HattenedMain.HAT_STORAGE_COMPONENT.get(), Collections.emptyList());

        //物品左键帽子放里面物品
        if (clickType == ClickAction.PRIMARY && !clickedStack.isEmpty()) {
            insertItem(hat, clickedStack, player);
            cursorStackReference.set(ItemStack.EMPTY);
            return true;
        }

        //空右键取出帽子的物品
        if (clickType == ClickAction.SECONDARY && clickedStack.isEmpty() && !existing.isEmpty()) {
            ItemStack extracted = removeItem(hat, player);
            if (extracted != null) {
                cursorStackReference.set(extracted);
                return true;
            }
        }

        return false;
    }

    //工具提示展示物品
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        TooltipDisplayComponent display = stack.getOrDefault(HattenedMain.TOOLTIP_DISPLAY.get(), TooltipDisplayComponent.DEFAULT);
        if (!display.shouldDisplay(HattenedMain.HAT_STORAGE_COMPONENT.get())) {
            return Optional.empty();
        }

        List<Card> storage = stack.getOrDefault(HattenedMain.HAT_STORAGE_COMPONENT.get(), Collections.emptyList());
        if (storage != null && !storage.isEmpty()) {
            return Optional.of(new HatTooltipData(storage));
        } else {
            return Optional.empty();
        }
    }

    public void insertItem(ItemStack hat, ItemStack stack, Player player) {
        List<Card> currentStorage = hat.getOrDefault(HattenedMain.HAT_STORAGE_COMPONENT.get(), Collections.emptyList());
        List<Card> newStorage = HattenedHelper.insertStack(currentStorage, stack);
        hat.set(HattenedMain.HAT_STORAGE_COMPONENT.get(), newStorage);
        player.playSound(HattenedSounds.INSERT_ITEM.get(), 0.8f, 0.8f + player.level().random.nextFloat() * 0.4f);
    }

    public ItemStack removeItem(ItemStack hat, Player player) {
        List<Card> storage = hat.getOrDefault(HattenedMain.HAT_STORAGE_COMPONENT.get(), Collections.emptyList());
        if (storage.isEmpty()) {
            return null;
        }

        player.playSound(HattenedSounds.REMOVE_ITEM.get(), 0.8f, 0.8f + player.level().random.nextFloat() * 0.4f);
        List<Card> newStorage = storage.subList(1, storage.size());
        Card extracted = storage.getFirst();
        hat.set(HattenedMain.HAT_STORAGE_COMPONENT.get(), newStorage);
        return extracted.getStack();
    }

    public static class HatTooltipData implements TooltipComponent {
        private final List<Card> storage;

        public HatTooltipData(List<Card> storage) {
            this.storage = storage;
        }

        public List<Card> getStorage() {
            return storage;
        }
    }
}
