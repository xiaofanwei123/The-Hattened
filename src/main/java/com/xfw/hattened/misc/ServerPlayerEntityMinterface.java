package com.xfw.hattened.misc;

import net.minecraft.world.item.ItemStack;

public interface ServerPlayerEntityMinterface {
    void proposeItemStack(ItemStack stack);
    void queueUserInput(UserInput input);
}