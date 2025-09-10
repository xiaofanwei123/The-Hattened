package com.xfw.hattened.mixin;

import com.xfw.hattened.misc.HattenedHelper;
import com.xfw.hattened.misc.ServerPlayerEntityMinterface;
import com.xfw.hattened.misc.UserInput;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin implements ServerPlayerEntityMinterface {
    @Unique private Queue<UserInput> inputQueue;
    @Unique private Queue<ItemStack> proposedAdditions;

    @Unique
    private final Queue<ItemStack> hattened$proposedAdditions = new LinkedList<>();

    @Override public void proposeItemStack(@NotNull ItemStack stack) {
        this.proposedAdditions.add(stack);
    }

    @Override
    public void queueUserInput(UserInput input) {
        this.inputQueue.add(input);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        this.inputQueue = new ConcurrentLinkedQueue<>();
        this.proposedAdditions = new ConcurrentLinkedQueue<>();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        HattenedHelper.updateHat((ServerPlayer) (Object) this, this.inputQueue, this.proposedAdditions);
    }
}