package com.xfw.hattened.misc;

import com.xfw.hattened.event.HatConfettiEvent;
import com.xfw.hattened.event.HatThrowItemEvent;
import com.xfw.hattened.event.HatVacuumItemEvent;
import com.xfw.hattened.init.HattenedAttachments;
import com.xfw.hattened.init.HattenedSounds;
import com.xfw.hattened.networking.ConfettiPayload;
import com.xfw.hattened.networking.SuckItemPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Queue;

/** 在服务端统一校验并执行帽子的按键控制、持续吸取、吐出和特效动作。 */
public final class HatController {
    /** 校验玩家是否装备帽子，并将一次客户端输入变化写入服务端控制状态。 */
    public static void handleInput(ServerPlayer player, UserInput input) {
        HatData hat = HattenedHelper.getWornHatData(player);
        HatControlState control = HattenedHelper.getControl(player);

        if (!hat.hasHat()) {
            HattenedHelper.resetControl(player);
            HattenedHelper.refreshHatView(player);
            return;
        }

        switch (input) {
            case LEFT_ALT_PRESSED -> HattenedHelper.setControl(player, control.withUsingHat(true));
            case LEFT_ALT_RELEASED -> HattenedHelper.resetControl(player);
            case SCROLL_UP -> rotateStorage(player, hat, control, true);
            case SCROLL_DOWN -> rotateStorage(player, hat, control, false);
            case LEFT_MOUSE_PRESSED -> {
                if (control.usingHat()) {
                    HattenedHelper.setControl(player, control.withThrowingItems(true));
                }
            }
            case LEFT_MOUSE_RELEASED -> HattenedHelper.setControl(player, control.withThrowingItems(false));
            case RIGHT_MOUSE_PRESSED -> {
                if (control.usingHat()) {
                    HattenedHelper.setControl(player, control.withVacuuming(true));
                }
            }
            case RIGHT_MOUSE_RELEASED -> HattenedHelper.setControl(player, control.withVacuuming(false));
            case MIDDLE_MOUSE_PRESSED -> {
                if (control.usingHat()) {
                    fireConfetti(player);
                }
            }
            case DEQUIP_HAT -> dequip(player);
        }
    }

    /** 在主手为空时脱下当前帽子，并把完整帽子物品交到玩家主手。 */
    public static void dequip(ServerPlayer player) {
        if (!player.getMainHandItem().isEmpty()) {
            return;
        }
        ItemStack removed = HattenedHelper.removeWornHat(player);
        if (removed.isEmpty()) {
            return;
        }
        player.swing(InteractionHand.MAIN_HAND);
        player.setItemInHand(InteractionHand.MAIN_HAND, removed);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                HattenedSounds.HAT_EQUIP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** 每个服务端玩家 Tick 执行当前长按操作，并同步装备来源和动画姿态。 */
    public static void tick(ServerPlayer player) {
        HatData hat = HattenedHelper.refreshHatView(player);
        if (!hat.hasHat()) {
            player.getData(HattenedAttachments.HAT_PENDING_ITEMS.get()).clear();
            HattenedHelper.resetControl(player);
            return;
        }

        HatControlState control = HattenedHelper.getControl(player);
        if (!control.usingHat()) {
            HattenedHelper.setPose(player, HatPose.ON_HEAD);
            commitStorage(player, hat.getStorage(), null);
            return;
        }

        List<Card> storage = hat.getStorage();
        PendingThrow pendingThrow = null;
        if (!control.vacuuming() && control.throwingItems() && !hat.getStorage().isEmpty()) {
            pendingThrow = prepareThrow(player, storage.getFirst());
            if (pendingThrow != null) {
                storage = HattenedHelper.removeOneFromFirst(storage);
            }
        }

        if (control.vacuuming()) {
            vacuumItem(player);
            HattenedHelper.setPose(player, HatPose.VACUUMING);
        } else {
            HattenedHelper.setPose(player, HatPose.SEARCHING_HAT);
        }
        commitStorage(player, storage, pendingThrow);
    }

    /** Alt 控制期间将玩家接触到的地面物品尽可能完整地存入帽子。 */
    public static boolean storeTouchedItem(ServerPlayer player, ItemEntity itemEntity) {
        if (!HattenedHelper.getControl(player).usingHat() || itemEntity.hasPickUpDelay()
                || itemEntity.isRemoved() || itemEntity.getItem().isEmpty()) {
            return false;
        }
        HatData hat = HattenedHelper.getWornHatData(player);
        if (!hat.hasHat()) {
            return false;
        }

        ItemStack claimed = itemEntity.getItem().copyAndClear();
        player.getData(HattenedAttachments.HAT_PENDING_ITEMS.get()).add(claimed);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                HattenedSounds.INSERT_ITEM.get(), player.getSoundSource(), 1.0F, 1.0F);
        itemEntity.discard();
        return true;
    }

    /** 仅在服务端确认 Alt 仍按住时，按滚轮方向切换选中的存储卡片。 */
    private static void rotateStorage(ServerPlayer player, HatData hat, HatControlState control, boolean left) {
        if (!control.usingHat()) {
            return;
        }
        List<Card> rotated = left
                ? HattenedHelper.rotateLeft(hat.getStorage())
                : HattenedHelper.rotateRight(hat.getStorage());
        HattenedHelper.setWornStorage(player, rotated);
    }

    /** 触发抛射前事件，并记录写回成功后需要生成的物品实体。 */
    private static PendingThrow prepareThrow(ServerPlayer player, Card selectedCard) {
        ItemStack removedStack = selectedCard.getStack().copyWithCount(1);
        ItemStack proposed = removedStack.copy();
        HatThrowItemEvent.Pre preEvent = new HatThrowItemEvent.Pre(player, selectedCard, proposed);
        if (NeoForge.EVENT_BUS.post(preEvent).isCanceled()) {
            return null;
        }

        ItemStack thrownStack = preEvent.getModifiedThrownStack();
        if (thrownStack.isEmpty()) {
            return null;
        }
        return new PendingThrow(selectedCard, removedStack, thrownStack.copy());
    }

    /** 在帽子扣减已经写回后生成抛射实体，并在生成失败时报告失败。 */
    private static boolean spawnThrow(ServerPlayer player, PendingThrow pendingThrow) {
        Vec3 position = player.position().add(0.0, 0.75, 0.0);
        Vec3 velocity = player.getLookAngle();
        ItemEntity itemEntity = new ItemEntity(player.level(), position.x, position.y, position.z,
                pendingThrow.thrownStack(), velocity.x, velocity.y, velocity.z);
        itemEntity.setPickUpDelay(10);
        if (!player.level().addFreshEntity(itemEntity)) {
            return false;
        }
        player.level().playSound(null, position.x, position.y, position.z,
                HattenedSounds.THROW_ITEM.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
        player.swing(InteractionHand.MAIN_HAND, true);
        NeoForge.EVENT_BUS.post(new HatThrowItemEvent.Post(
                player, pendingThrow.selectedCard(), pendingThrow.thrownStack()));
        return true;
    }

    /** 在玩家前方锥形范围内寻找可见物品，并选择一个尝试吸入帽子。 */
    private static void vacuumItem(ServerPlayer player) {
        player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(24.0))
                .stream()
                .filter(itemEntity -> {
                    Vec3 toItem = itemEntity.position().subtract(player.position()).normalize();
                    return player.hasLineOfSight(itemEntity)
                            && !itemEntity.hasPickUpDelay()
                            && toItem.dot(player.getLookAngle()) > 0.9F;
                })
                .findFirst()
                .ifPresent(itemEntity -> vacuumItem(player, itemEntity));
    }

    /** 触发可取消的吸取事件；未取消时存储所选实体物品并同步客户端动画。 */
    private static void vacuumItem(ServerPlayer player, ItemEntity itemEntity) {
        ItemStack proposed = itemEntity.getItem().copyWithCount(1);
        HatVacuumItemEvent.Pre preEvent = new HatVacuumItemEvent.Pre(player, itemEntity, proposed);
        if (NeoForge.EVENT_BUS.post(preEvent).isCanceled()) {
            return;
        }

        ItemStack vacuumedStack = preEvent.getModifiedVacuumedStack();
        if (vacuumedStack.isEmpty()) {
            return;
        }
        player.getData(HattenedAttachments.HAT_PENDING_ITEMS.get()).add(vacuumedStack.copy());
        itemEntity.getItem().shrink(1);
        PacketDistributor.sendToPlayersNear((ServerLevel) player.level(), null,
                player.getX(), player.getY(), player.getZ(), 64.0,
                new SuckItemPayload(itemEntity.getId(), player.getId()));
        if (itemEntity.getItem().isEmpty()) {
            itemEntity.discard();
        }
        NeoForge.EVENT_BUS.post(new HatVacuumItemEvent.Post(player, itemEntity, preEvent.getModifiedVacuumedStack()));
    }

    /** 合并本 Tick 的扣减与拾取，成功写回一次后再生成已经扣除的抛射物。 */
    private static void commitStorage(ServerPlayer player, List<Card> storage, PendingThrow pendingThrow) {
        Queue<ItemStack> pending = player.getData(HattenedAttachments.HAT_PENDING_ITEMS.get());
        if (pending.isEmpty() && pendingThrow == null) {
            return;
        }

        for (ItemStack stack : pending) {
            storage = HattenedHelper.insertStack(storage, stack.copy());
        }
        if (!HattenedHelper.setWornStorage(player, storage)) {
            return;
        }
        pending.clear();

        if (pendingThrow != null && !spawnThrow(player, pendingThrow)) {
            List<Card> restored = HattenedHelper.insertStack(storage, pendingThrow.removedStack().copy());
            if (!HattenedHelper.setWornStorage(player, restored)) {
                pending.add(pendingThrow.removedStack().copy());
            }
        }
    }

    /** 保存一次已通过前置事件校验、等待完成存储事务的抛射动作。 */
    private record PendingThrow(Card selectedCard, ItemStack removedStack, ItemStack thrownStack) {
    }

    /** 触发可取消的礼花事件；未取消时向附近玩家广播五彩纸屑效果。 */
    private static void fireConfetti(ServerPlayer player) {
        HatConfettiEvent.Pre preEvent = new HatConfettiEvent.Pre(player);
        if (NeoForge.EVENT_BUS.post(preEvent).isCanceled()) {
            return;
        }
        player.swing(InteractionHand.MAIN_HAND, true);
        PacketDistributor.sendToPlayersNear(player.serverLevel(), null,
                player.getX(), player.getY(), player.getZ(), 32.0,
                new ConfettiPayload(player.position().add(0.0, 0.75, 0.0), player.getLookAngle()));
        NeoForge.EVENT_BUS.post(new HatConfettiEvent.Post(player));
    }

    /** 阻止实例化只提供服务端帽子控制逻辑的工具类。 */
    private HatController() {
    }
}
