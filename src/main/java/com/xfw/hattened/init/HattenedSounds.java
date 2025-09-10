package com.xfw.hattened.init;

import com.xfw.hattened.HattenedMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HattenedSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = 
        DeferredRegister.create(Registries.SOUND_EVENT, HattenedMain.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> HAT_EQUIP = 
        SOUNDS.register("hat_equip", () -> SoundEvent.createVariableRangeEvent(HattenedMain.id("hat_equip")));

    public static final DeferredHolder<SoundEvent, SoundEvent> INSERT_ITEM = 
        SOUNDS.register("insert_item", () -> SoundEvent.createVariableRangeEvent(HattenedMain.id("insert_item")));

    public static final DeferredHolder<SoundEvent, SoundEvent> REMOVE_ITEM = 
        SOUNDS.register("remove_item", () -> SoundEvent.createVariableRangeEvent(HattenedMain.id("remove_item")));

    public static final DeferredHolder<SoundEvent, SoundEvent> THROW_ITEM = 
        SOUNDS.register("throw_item", () -> SoundEvent.createVariableRangeEvent(HattenedMain.id("throw_item")));

    public static final DeferredHolder<SoundEvent, SoundEvent> HAT_CONFETTI =
        SOUNDS.register("confetti", () -> SoundEvent.createVariableRangeEvent(HattenedMain.id("confetti")));


    public static void init() {
    }
}