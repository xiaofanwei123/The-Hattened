package com.xfw.hattened.client;

import com.xfw.hattened.client.renderer.CardWidget;
import com.xfw.hattened.misc.Card;
import com.xfw.hattened.misc.HatData;
import com.xfw.hattened.misc.PeripheralManager;
import net.minecraft.client.DeltaTracker;

import java.util.*;
import java.util.stream.Collectors;

/** 根据同步到客户端的帽子数据维护界面开合进度和卡片动画状态。 */
public final class ClientStorage {
    public static int ticks = 0;
    private static int usingTime = 0;
    public static HatData hat = HatData.DEFAULT;
    public static final HashMap<UUID, CardWidget> cards = new HashMap<>();
    public static List<Map.Entry<CardWidget, Integer>> orderedCards = List.of();

    /** 每个客户端 Tick 推进帽子菜单开合动画，并同步、更新和清理卡片控件。 */
    public static void tick(HatData hat) {
        ClientStorage.hat = hat;
        if (hat.hasHat()) {
            usingTime = Math.clamp(usingTime + (PeripheralManager.HAT_KEYBIND.isDown() ? 1 : -1), 0, 10);
        } else {
            usingTime = 0;
        }

        List<Card> storage = hat.hasHat() ? hat.getStorage() : List.of();
        for (int index = 0; index < storage.size(); index++) {
            Card card = storage.get(index);
            if (!cards.containsKey(card.getUuid())) {
                cards.put(card.getUuid(), new CardWidget(index, card));
            }
            CardWidget widget = cards.get(card.getUuid());
            widget.card = card;
            widget.index = index;
            widget.removing = false;
        }

        Set<UUID> hatUUIDs = storage.stream().map(Card::getUuid).collect(Collectors.toSet());
        List<UUID> keysToRemove = new ArrayList<>();
        for (Map.Entry<UUID, CardWidget> entry : cards.entrySet()) {
            UUID uuid = entry.getKey();
            CardWidget card = entry.getValue();
            if (!hatUUIDs.contains(uuid)) {
                card.removing = true;
            }
            card.tick();
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

    /** 根据部分 Tick 返回当前帧使用的平滑菜单展开进度。 */
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

        return Math.clamp(raw / 10f, 0f, 1f);
    }

    /** 阻止实例化只保存客户端界面状态的工具类。 */
    private ClientStorage() {
    }
}
