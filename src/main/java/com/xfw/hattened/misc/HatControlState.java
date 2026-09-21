package com.xfw.hattened.misc;

/** 保存玩家当前帽子控制按键的仅服务端运行时状态。 */
public record HatControlState(boolean usingHat, boolean throwingItems, boolean vacuuming) {
    public static final HatControlState IDLE = new HatControlState(false, false, false);

    /** 返回只修改 Alt 按住状态、保留其他输入状态的新值。 */
    public HatControlState withUsingHat(boolean value) {
        return value ? new HatControlState(true, throwingItems, vacuuming) : IDLE;
    }

    /** 返回只修改持续吐出状态、保留其他输入状态的新值。 */
    public HatControlState withThrowingItems(boolean value) {
        return new HatControlState(usingHat, value, vacuuming);
    }

    /** 返回只修改持续吸取状态、保留其他输入状态的新值。 */
    public HatControlState withVacuuming(boolean value) {
        return new HatControlState(usingHat, throwingItems, value);
    }
}
