package com.xfw.hattened.init;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.misc.HatControlState;
import com.xfw.hattened.misc.HatData;
import com.xfw.hattened.misc.HatPose;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayDeque;

/** 注册帽子的持久数据、同步视图、动画姿态和仅服务端使用的输入状态。 */
public final class HattenedAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, HattenedMain.MODID);

    //不自动同步:客户端看不到这个数据
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HatData>> HAT_DATA =
            ATTACHMENT_TYPES.register("hat_data", () -> AttachmentType.builder(() -> HatData.DEFAULT)
                    .serialize(HatData.CODEC)//持久化:让数据写进 NBT 存档
                    .copyOnDeath()//玩家死了帽子还在
                    .build());
    
    //不持久化:不写存档（每次登录重新从服务端同步）
    //不 copyOnDeath:反正会重新同步
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HatData>> HAT_VIEW =
            ATTACHMENT_TYPES.register("hat_view", () -> AttachmentType.builder(() -> HatData.DEFAULT)
                    .sync(HatData.PACKET_CODEC)//自动同步:服务端改它,客户端自动收到
                    .build());

    //同步,不持久化:姿态是"瞬时状态",不需要存,使用HatPose枚举
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HatPose>> HAT_POSE =
            ATTACHMENT_TYPES.register("hat_pose", () -> AttachmentType.builder(() -> HatPose.ON_HEAD)
                    .sync(StreamCodec.of(
                            FriendlyByteBuf::writeEnum,
                            buffer -> buffer.readEnum(HatPose.class)
                    ))
                    .build());

    //不持久化:按键状态不存
    //不同步:客户端不需要知道服务端按键状态
    //不 copyOnDeath:死后重置
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HatControlState>> HAT_CONTROL =
            ATTACHMENT_TYPES.register("hat_control", () -> AttachmentType.builder(() -> HatControlState.IDLE).build());

    //不持久化:临时队列,会话结束就丢
    //不同步:仅服务端
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ArrayDeque<ItemStack>>> HAT_PENDING_ITEMS =
            ATTACHMENT_TYPES.register("hat_pending_items",
                    () -> AttachmentType.builder(
                            () -> new ArrayDeque<ItemStack>()).build());

    /** 阻止实例化仅用于保存附件类型注册项的工具类。 */
    private HattenedAttachments() {
    }
}
