package com.xfw.hattened.mixin;

import com.xfw.hattened.init.HattenedSounds;
import com.xfw.hattened.misc.HattenedHelper;
import com.xfw.hattened.misc.ServerPlayerEntityMinterface;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void onPlayerTouch(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer && HattenedHelper.getHatData(player).isUsingHat()) {
            ItemEntity itemEntity = (ItemEntity)(Object)this;
            if (!itemEntity.hasPickUpDelay()) {
                ((ServerPlayerEntityMinterface) serverPlayer).proposeItemStack(itemEntity.getItem().copy());
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), HattenedSounds.INSERT_ITEM.get(), player.getSoundSource(), 1.0F, 1.0F);
                itemEntity.discard();
                ci.cancel();
            }
        }
    }
}