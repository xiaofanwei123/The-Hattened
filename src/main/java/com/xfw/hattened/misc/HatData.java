package com.xfw.hattened.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.event.HatThrowItemEvent;
import com.xfw.hattened.event.HatVacuumItemEvent;
import com.xfw.hattened.client.sound.HattenedSounds;
import com.xfw.hattened.networking.SuckItemPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HatData {
    public static final HatData DEFAULT = new HatData();

    public static final Codec<HatData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.BOOL.fieldOf("hasHat").forGetter(HatData::hasHat),
                    Card.CODEC.listOf().fieldOf("storage").forGetter(HatData::getStorage)
            ).apply(builder, HatData::new)
    );

    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, HatData> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, HatData::hasHat,
            Card.STREAM_CODEC.apply(ByteBufCodecs.list()), hatData -> hatData.getStorage().stream()
                    .filter(card -> !card.getStack().isEmpty())
                    .toList(),
            HatData::new
    );

    private final boolean hasHat;
    private final List<Card> storage;
    private final boolean usingHat;
    private final boolean isThrowingItems;
    private final boolean isVacuuming;

    public HatData() {
        this(false, Collections.emptyList(), false, false, false);
    }

    public HatData(boolean hasHat, List<Card> storage) {
        this(hasHat, storage, false, false, false);
    }

    public HatData(boolean hasHat, List<Card> storage, boolean usingHat, boolean isThrowingItems, boolean isVacuuming) {
        this.hasHat = hasHat;
        this.storage = storage;
        this.usingHat = usingHat;
        this.isThrowingItems = isThrowingItems;
        this.isVacuuming = isVacuuming;
    }

    public boolean hasHat() {
        return hasHat;
    }

    public List<Card> getStorage() {
        return storage;
    }

    public boolean isUsingHat() {
        return usingHat;
    }

    public boolean isThrowingItems() {
        return isThrowingItems;
    }

    public boolean isVacuuming() {
        return isVacuuming;
    }

    public ItemStack toItemStack() {
        ItemStack stack = new ItemStack(HattenedMain.HAT_ITEM.get());
        stack.set(HattenedMain.HAT_STORAGE_COMPONENT.get(), storage);
        return stack;
    }

    public void tick(ServerPlayer player) {
        if (!this.usingHat) {
            HattenedHelper.setPose(player, HatPose.ON_HEAD);
            return;
        }
        Level world = player.level();
        Card selectedCard = storage.isEmpty() ? null : storage.getFirst();
        HatPose pose = HatPose.SEARCHING_HAT;

        if (!this.isVacuuming && this.isThrowingItems && selectedCard != null) {
            Vec3 pos = player.position().add(0.0, 0.75, 0.0);
            Vec3 vel = player.getLookAngle();
            ItemStack thrownStack = selectedCard.getStack().split(1);

            //触发吐物品前事件
            HatThrowItemEvent.Pre throwPreEvent = new HatThrowItemEvent.Pre(player, selectedCard, thrownStack);
            if (!NeoForge.EVENT_BUS.post(throwPreEvent).isCanceled()) {
                //使用事件中可能被修改的ItemStack
                ItemStack finalThrownStack = throwPreEvent.getModifiedThrownStack();
                ItemEntity itemEntity = new ItemEntity(world, pos.x, pos.y, pos.z, finalThrownStack, vel.x, vel.y, vel.z);
                itemEntity.setPickUpDelay(10);
                world.addFreshEntity(itemEntity);
                world.playSound(null, pos.x, pos.y, pos.z, HattenedSounds.THROW_ITEM.get(), SoundSource.PLAYERS, 0.5f, 1f);
                player.swing(InteractionHand.MAIN_HAND, true);
                selectedCard.markDirty();

                //触发吐物品后事件，使用最终的ItemStack
                HatThrowItemEvent.Post throwPostEvent = new HatThrowItemEvent.Post(player, selectedCard, finalThrownStack);
                NeoForge.EVENT_BUS.post(throwPostEvent);
            } else {
                //事件被取消，恢复物品数量
                selectedCard.getStack().grow(1);
            }
        }

        if (this.isVacuuming) {
            pose = HatPose.VACUUMING;
            world.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(24.0))
                    .stream()
                    .filter(itemEntity -> {
                        Vec3 toItem = itemEntity.position().subtract(player.position()).normalize();
                        return player.hasLineOfSight(itemEntity) && !itemEntity.hasPickUpDelay() &&
                                toItem.dot(player.getLookAngle()) > 0.9f;
                    })
                    .findFirst()
                    .ifPresent(itemEntity -> {
                        ItemStack vacuumedStack = itemEntity.getItem().split(1);

                        //触发吸物品前事件
                        HatVacuumItemEvent.Pre vacuumPreEvent = new HatVacuumItemEvent.Pre(player, itemEntity, vacuumedStack);
                        if (!NeoForge.EVENT_BUS.post(vacuumPreEvent).isCanceled()) {
                            //使用事件中可能被修改的ItemStack
                            ItemStack finalVacuumedStack = vacuumPreEvent.getModifiedVacuumedStack();
                            ((ServerPlayerEntityMinterface) player).proposeItemStack(finalVacuumedStack);
                            PacketDistributor.sendToPlayersNear((ServerLevel) player.level(), null, player.getX(), player.getY(), player.getZ(), 64.0,
                                    new SuckItemPayload(itemEntity.getId(), player.getId()));

                            //触发吸物品后事件
                            HatVacuumItemEvent.Post vacuumPostEvent = new HatVacuumItemEvent.Post(player, itemEntity, finalVacuumedStack);
                            NeoForge.EVENT_BUS.post(vacuumPostEvent);
                        } else {
                            //事件被取消，恢复物品数量
                            itemEntity.getItem().grow(1);
                        }
                    });
        }

        HattenedHelper.setPose(player, pose);
    }

    public HatData updateInternalState(UserInput event) {
        return switch (event) {
            case LEFT_ALT_PRESSED -> new HatData(hasHat, storage, true, false, false);
            case LEFT_ALT_RELEASED -> new HatData(hasHat, storage, false, false, false);
            case SCROLL_UP -> new HatData(hasHat, rotateLeft(storage), usingHat, false, false);
            case SCROLL_DOWN -> new HatData(hasHat, rotateRight(storage), usingHat, false, false);
            case LEFT_MOUSE_PRESSED -> new HatData(hasHat, storage, usingHat, true, isVacuuming);
            case LEFT_MOUSE_RELEASED -> new HatData(hasHat, storage, usingHat, false, isVacuuming);
            case RIGHT_MOUSE_PRESSED -> new HatData(hasHat, storage, usingHat, isThrowingItems, true);
            case RIGHT_MOUSE_RELEASED -> new HatData(hasHat, storage, usingHat, isThrowingItems, false);
            default -> this;
        };
    }

    private static <E> List<E> rotateLeft(List<E> list) {
        if (list.isEmpty()) return list;
        List<E> result = new ArrayList<>(list.subList(1, list.size()));
        result.add(list.getFirst());
        return result;
    }

    private static <E> List<E> rotateRight(List<E> list) {
        if (list.isEmpty()) return list;
        List<E> result = new ArrayList<>();
        result.add(list.getLast());
        result.addAll(list.subList(0, list.size() - 1));
        return result;
    }
}