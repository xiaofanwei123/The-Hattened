package com.xfw.hattened.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/** 帽子吸取物品的公开 NeoForge 事件 */
public abstract class HatVacuumItemEvent extends Event {
    private final ServerPlayer player;
    private final ItemEntity itemEntity;
    private final ItemStack vacuumedStack;

    /** 保存执行玩家、目标实体和本次计划吸取的物品快照。 */
    public HatVacuumItemEvent(ServerPlayer player, ItemEntity itemEntity, ItemStack vacuumedStack) {
        this.player = player;
        this.itemEntity = itemEntity;
        this.vacuumedStack = vacuumedStack;
    }

    /**
     * 获取执行吸物品动作的玩家
     */
    public ServerPlayer getPlayer() {
        return player;
    }

    /**
     * 获取被吸取的物品实体
     */
    public ItemEntity getItemEntity() {
        return itemEntity;
    }

    /**
     * 获取被吸取的物品堆叠
     */
    public ItemStack getVacuumedStack() {
        return vacuumedStack;
    }

    /**
     * 帽子吸物品前事件
     * 在物品被吸取之前触发，可以被取消
     */
    public static class Pre extends HatVacuumItemEvent implements ICancellableEvent {
        private ItemStack modifiedVacuumedStack;

        /** 创建可取消的吸取前事件，并提供一份可由监听器替换的物品副本。 */
        public Pre(ServerPlayer player, ItemEntity itemEntity, ItemStack vacuumedStack) {
            super(player, itemEntity, vacuumedStack);
            this.modifiedVacuumedStack = vacuumedStack.copy(); //创建副本以供修改
        }

        /**
         * 获取修改后的被吸取物品堆叠
         * 事件监听器可以修改这个ItemStack来改变实际吸取的物品
         */
        public ItemStack getModifiedVacuumedStack() {
            return modifiedVacuumedStack;
        }

        /**
         * 设置修改后的被吸取物品堆叠
         * @param modifiedStack 要吸取的新物品堆叠
         */
        public void setModifiedVacuumedStack(ItemStack modifiedStack) {
            this.modifiedVacuumedStack = modifiedStack.copy();
        }
    }

    /**
     * 帽子吸物品后事件
     * 在物品被成功吸取之后触发，不可取消
     */
    public static class Post extends HatVacuumItemEvent {
        /** 创建吸取成功后的通知事件，记录最终被存入帽子的物品。 */
        public Post(ServerPlayer player, ItemEntity itemEntity, ItemStack vacuumedStack) {
            super(player, itemEntity, vacuumedStack);
        }
    }
}
