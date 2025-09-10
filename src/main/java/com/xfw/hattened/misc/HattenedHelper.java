package com.xfw.hattened.misc;

import com.xfw.hattened.client.ClientStorage;
import com.xfw.hattened.init.HattenedAttachments;
import com.xfw.hattened.networking.HatDataSyncPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

public class HattenedHelper {

    public static HatData getHatData(Player player) {
        if (player.level().isClientSide) {
            return ClientStorage.getHatData(player.getId());
        }
        return player.getData(HattenedAttachments.HAT_DATA.get());
    }

    public static void setHatData(ServerPlayer player, HatData hat) {
        player.setData(HattenedAttachments.HAT_DATA.get(), hat);
        //同步到客户端
        PacketDistributor.sendToPlayersNear((ServerLevel) player.level(), null,
            player.getX(), player.getY(), player.getZ(), 64.0,
            new HatDataSyncPayload(player.getId(), hat, getPose(player)));
    }

    public static HatPose getPose(Player player) {
        if (player.level().isClientSide) {
            return ClientStorage.getHatPose(player.getId());
        }
        return player.getData(HattenedAttachments.HAT_POSE.get());
    }

    public static void setPose(ServerPlayer player, HatPose pose) {
        player.setData(HattenedAttachments.HAT_POSE.get(), pose);
        //同步到客户端
        PacketDistributor.sendToPlayersNear((ServerLevel) player.level(), null,
            player.getX(), player.getY(), player.getZ(), 64.0,
            new HatDataSyncPayload(player.getId(), getHatData(player), pose));
    }

    public static List<Card> insertStack(List<Card> storage, ItemStack stack) {
        for (Card card : storage) {
            if (ItemStack.isSameItemSameComponents(card.getStack(), stack)) {
                int transferAmount = Math.min(card.getStack().getMaxStackSize() - card.getStack().getCount(), stack.getCount());
                stack.split(transferAmount);
                card.getStack().grow(transferAmount);
                card.markDirty();
                if (stack.isEmpty()) {
                    return List.copyOf(storage);
                }
            }
        }
        return new ArrayList<>(storage) {{
            add(new Card(stack));
        }};
    }

    public static void updateHat(ServerPlayer player, Queue<UserInput> inputQueue,Queue<ItemStack> proposedAdditions) {
        HatData hat = getHatData(player);
        while (!inputQueue.isEmpty()) {
            hat = hat.updateInternalState(inputQueue.poll());
        }

        hat.tick(player);
        Card firstCard = hat.getStorage().isEmpty() ? null : hat.getStorage().get(0);
        if (firstCard != null && firstCard.isMutated() && firstCard.getReplacement() == null) {
            hat = new HatData(hat.hasHat(), hat.getStorage(), hat.isUsingHat(), false, hat.isVacuuming());
        }

        List<Card> newStorage = hat.getStorage().stream()
                .map(c -> c.isMutated() ? c.getReplacement() : c)
                .filter(Objects::nonNull)
                .toList();

        while (!proposedAdditions.isEmpty()) {
            newStorage = insertStack(newStorage, proposedAdditions.poll());
        }

        setHatData(player, new HatData(hat.hasHat(), newStorage, hat.isUsingHat(), hat.isThrowingItems(), hat.isVacuuming()));
    }
}