package com.xfw.hattened.misc;

import com.mojang.blaze3d.platform.InputConstants;
import com.xfw.hattened.client.ClientStorage;
import com.xfw.hattened.networking.HatInputPayload;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** 管理客户端帽子按键映射，并在状态变化时向服务端发送控制消息。 */
public final class PeripheralManager {
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
    private static boolean previousDequipState = false;

    /** 将 Alt 菜单键和 H 脱帽键注册到客户端按键设置。 */
    public static void init(RegisterKeyMappingsEvent event) {
        event.register(HAT_KEYBIND);
        event.register(DEQUIP_HAT_KEYBIND);
    }

    /** 检测各控制键的按下和松开变化，并为每次变化发送一次消息。 */
    public static void tick() {
        boolean dequipPressed = DEQUIP_HAT_KEYBIND.isDown();
        if (!previousDequipState && dequipPressed) {
            PacketDistributor.sendToServer(new HatInputPayload(UserInput.DEQUIP_HAT));
        }
        if (!previousState && HAT_KEYBIND.isDown()) {
            PacketDistributor.sendToServer(new HatInputPayload(UserInput.LEFT_ALT_PRESSED));
        } else if (previousState && !HAT_KEYBIND.isDown()) {
            PacketDistributor.sendToServer(new HatInputPayload(UserInput.LEFT_ALT_RELEASED));
        }

        previousState = HAT_KEYBIND.isDown();
        previousDequipState = dequipPressed;
    }

    /** 判断帽子菜单是否已接管鼠标按键和滚轮输入。 */
    public static boolean shouldIntercept() {
        return ClientStorage.hat.hasHat() && HAT_KEYBIND.isDown();
    }

    /** 阻止实例化只保存客户端输入状态的工具类。 */
    private PeripheralManager() {
    }
}
