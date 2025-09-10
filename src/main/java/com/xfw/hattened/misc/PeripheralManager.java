package com.xfw.hattened.misc;

import com.mojang.blaze3d.platform.InputConstants;
import com.xfw.hattened.client.ClientStorage;
import com.xfw.hattened.networking.HatInputPayload;
import com.xfw.hattened.networking.HatKeybindPayload;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class PeripheralManager {
    public static final String CATEGORY = "key.categories.hattened";

    public static final KeyMapping HAT_KEYBIND = new KeyMapping(
            "key.hattened.hat",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LALT,
            CATEGORY
    );

    public static final KeyMapping DEQUIP_HAT_KEYBIND = new KeyMapping(
            "key.hattened.dequip",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_H,
            CATEGORY
    );

    private static boolean previousState = false;

    public static void init(RegisterKeyMappingsEvent event) {
        event.register(HAT_KEYBIND);
        event.register(DEQUIP_HAT_KEYBIND);
    }

    public static void tick() {
        if (DEQUIP_HAT_KEYBIND.isDown()) {
            PacketDistributor.sendToServer(new HatKeybindPayload(true));
        }
        if (!previousState && HAT_KEYBIND.isDown()) {
            PacketDistributor.sendToServer(new HatInputPayload(UserInput.LEFT_ALT_PRESSED));
        } else if (previousState && !HAT_KEYBIND.isDown()) {
            PacketDistributor.sendToServer(new HatInputPayload(UserInput.LEFT_ALT_RELEASED));
        }

        previousState = HAT_KEYBIND.isDown();
    }

    public static boolean shouldIntercept(){
        return ClientStorage.hat.hasHat() && HAT_KEYBIND.isDown();
    }
}
