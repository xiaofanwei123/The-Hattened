package com.xfw.hattened.client;

import com.xfw.hattened.client.renderer.CardWidget;
import com.xfw.hattened.misc.Card;
import com.xfw.hattened.misc.HatData;
import com.xfw.hattened.misc.HatPose;
import com.xfw.hattened.misc.PeripheralManager;
import net.minecraft.client.DeltaTracker;

import java.util.*;
import java.util.stream.Collectors;

public class ClientStorage {
    public static int ticks = 0;
    private static int usingTime = 0;
    public static HatData hat = HatData.DEFAULT;
    public static final HashMap<UUID, CardWidget> cards = new HashMap<>();
    public static List<Map.Entry<CardWidget, Integer>> orderedCards = List.of();

    public static void tick(HatData hat) {
        ClientStorage.hat = hat;
        if (hat.hasHat()) {
            usingTime = Math.max(0, Math.min(10, usingTime + (PeripheralManager.HAT_KEYBIND.isDown() ? 1 : -1)));
        } else {
            usingTime = 0;
            return;
        }
        for (int index = 0; index < hat.getStorage().size(); index++) {
            Card card = hat.getStorage().get(index);
            if (!cards.containsKey(card.getUuid())) {
                cards.put(card.getUuid(), new CardWidget(index, card));
            }
            CardWidget widget = cards.get(card.getUuid());
            widget.card = card;
            widget.index = index;
        }

        Set<UUID> hatUUIDs = hat.getStorage().stream().map(Card::getUuid).collect(Collectors.toSet());
        List<UUID> keysToRemove = new ArrayList<>();
        for (Map.Entry<UUID, CardWidget> entry : cards.entrySet()) {
            UUID uuid = entry.getKey();
            CardWidget card = entry.getValue();
            if (!hatUUIDs.contains(uuid)) {
                card.removing = true;
            }
            if (card.canRemove()) {
                keysToRemove.add(uuid);
            }
        }
        keysToRemove.forEach(cards::remove);

        int cardCount = cards.size();
        orderedCards = cards.values().stream()
                .map(c -> {
                    int relativeIndex = c.index;
                    if (relativeIndex > cardCount / 2) {
                        relativeIndex -= cardCount;
                    }
                    return Map.entry(c, relativeIndex);
                })
                .sorted((a, b) -> Integer.compare(Math.abs(b.getValue()), Math.abs(a.getValue())))
                .collect(Collectors.toList());
    }

    public static float getProgress(DeltaTracker tickCounter) {
        if (!hat.hasHat()) {
            return 0f;
        }

        if (!PeripheralManager.shouldIntercept() && usingTime == 0) {
            return 0f;
        }

        float raw = PeripheralManager.shouldIntercept()
                ? usingTime + tickCounter.getGameTimeDeltaPartialTick(false)
                : usingTime - tickCounter.getGameTimeDeltaPartialTick(false);

        return Math.max(0f, Math.min(1f, raw / 10f));
    }

    private static final Map<Integer, HatData> playerHatData = new HashMap<>();
    private static final Map<Integer, HatPose> playerHatPose = new HashMap<>();

    public static void setHatData(int playerId, HatData hatData) {
        playerHatData.put(playerId, hatData);

    }

    public static HatData getHatData(int playerId) {
        return playerHatData.getOrDefault(playerId, HatData.DEFAULT);
    }

    public static void setHatPose(int playerId, HatPose hatPose) {
        playerHatPose.put(playerId, hatPose);
    }

    public static HatPose getHatPose(int playerId) {
        return playerHatPose.getOrDefault(playerId, HatPose.ON_HEAD);
    }


    public static void clearPlayer(int playerId) {
        playerHatData.remove(playerId);
        playerHatPose.remove(playerId);
    }
}