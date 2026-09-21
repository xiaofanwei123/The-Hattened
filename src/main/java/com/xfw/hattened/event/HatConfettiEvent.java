package com.xfw.hattened.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/** 帽子烟花效果的公开 NeoForge 事件 */
public abstract class HatConfettiEvent extends Event {
    private final ServerPlayer player;

    /** 保存触发本次帽子礼花动作的服务端玩家。 */
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
        /** 创建可取消的礼花播放前事件。 */
        public Pre(ServerPlayer player) {
            super(player);
        }
    }

    /**
     * 帽子烟花后事件
     * 在烟花效果成功触发之后触发，不可取消
     */
    public static class Post extends HatConfettiEvent {
        /** 创建礼花成功广播后的通知事件。 */
        public Post(ServerPlayer player) {
            super(player);
        }
    }
}
