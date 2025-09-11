package com.xfw.hattened.init;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.misc.HatData;
import com.xfw.hattened.misc.HatPose;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class HattenedAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = 
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, HattenedMain.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HatData>> HAT_DATA = 
        ATTACHMENT_TYPES.register("hat_data", () -> AttachmentType.builder(() -> HatData.DEFAULT)
            .serialize(HatData.CODEC)
            .copyOnDeath()
            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HatPose>> HAT_POSE = 
        ATTACHMENT_TYPES.register("hat_pose", () -> AttachmentType.builder(() -> HatPose.ON_HEAD)
            .build());

}