package com.xfw.hattened.event;

import com.xfw.hattened.misc.Card;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * 帽子吐物品事件基类
 */
public abstract class HatThrowItemEvent extends Event {
    private final ServerPlayer player;
    private final Card card;
    private final ItemStack thrownStack;
    private final Vec3 position;
    private final Vec3 velocity;

    public HatThrowItemEvent(ServerPlayer player, Card card, ItemStack thrownStack, Vec3 position, Vec3 velocity) {
        this.player = player;
        this.card = card;
        this.thrownStack = thrownStack;
        this.position = position;
        this.velocity = velocity;
    }

    /**
     * 获取执行吐物品动作的玩家
     */
    public ServerPlayer getPlayer() {
        return player;
    }

    /**
     * 获取被吐出的卡片
     */
    public Card getCard() {
        return card;
    }

    /**
     * 获取被吐出的物品堆叠
     */
    public ItemStack getThrownStack() {
        return thrownStack;
    }

    /**
     * 获取物品被吐出的位置
     */
    public Vec3 getPosition() {
        return position;
    }

    /**
     * 获取物品被吐出的速度向量
     */
    public Vec3 getVelocity() {
        return velocity;
    }

    /**
     * 帽子吐物品前事件
     * 在物品被吐出之前触发，可以被取消
     */
    public static class Pre extends HatThrowItemEvent implements ICancellableEvent {
        private ItemStack modifiedThrownStack;

        public Pre(ServerPlayer player, Card card, ItemStack thrownStack, Vec3 position, Vec3 velocity) {
            super(player, card, thrownStack, position, velocity);
            this.modifiedThrownStack = thrownStack.copy(); //创建副本以供修改
        }

        /**
         * 获取修改后的被吐出物品堆叠
         * 事件监听器可以修改这个ItemStack来改变实际吐出的物品
         */
        public ItemStack getModifiedThrownStack() {
            return modifiedThrownStack;
        }

        /**
         * 设置修改后的被吐出物品堆叠
         * @param modifiedStack 要吐出的新物品堆叠
         */
        public void setModifiedThrownStack(ItemStack modifiedStack) {
            this.modifiedThrownStack = modifiedStack.copy();
        }
    }

    /**
     * 帽子吐物品后事件
     * 在物品被成功吐出之后触发，不可取消
     */
    public static class Post extends HatThrowItemEvent {
        public Post(ServerPlayer player, Card card, ItemStack thrownStack, Vec3 position, Vec3 velocity) {
            super(player, card, thrownStack, position, velocity);
        }
    }
}