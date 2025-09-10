package com.xfw.hattened.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * 帽子烟花事件基类
 */
public abstract class HatConfettiEvent extends Event {
    private final ServerPlayer player;

    public HatConfettiEvent(ServerPlayer player) {
        this.player = player;

    }

    /**
     * 获取释放烟花的玩家
     */
    public ServerPlayer getPlayer() {
        return player;
    }


    /**
     * 帽子烟花前事件
     * 在烟花效果触发之前触发，可以被取消
     */
    public static class Pre extends HatConfettiEvent implements ICancellableEvent {
        public Pre(ServerPlayer player) {
            super(player);
        }
    }

    /**
     * 帽子烟花后事件
     * 在烟花效果成功触发之后触发，不可取消
     */
    public static class Post extends HatConfettiEvent {
        public Post(ServerPlayer player) {
            super(player);
        }
    }
}