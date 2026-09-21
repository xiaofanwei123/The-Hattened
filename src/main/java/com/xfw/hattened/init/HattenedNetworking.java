package com.xfw.hattened.init;

import com.xfw.hattened.client.HattenedClientPayloadHandler;
import com.xfw.hattened.misc.HatController;
import com.xfw.hattened.networking.ConfettiPayload;
import com.xfw.hattened.networking.HatInputPayload;
import com.xfw.hattened.networking.SuckItemPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** 注册帽子输入消息以及只在客户端处理的视觉效果消息。 */
public final class HattenedNetworking {
    /** 注册游戏阶段使用的全部载荷编解码器和收发处理器。 */
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("2");

        registrar.playToServer(
                HatInputPayload.TYPE,
                HatInputPayload.STREAM_CODEC,
                (payload, context) -> HatController.handleInput((ServerPlayer) context.player(), payload.input())
        );

        registrar.playToClient(
                SuckItemPayload.TYPE,
                SuckItemPayload.STREAM_CODEC,
                HattenedNetworking::handleSuckItem
        );

        registrar.playToClient(
                ConfettiPayload.TYPE,
                ConfettiPayload.STREAM_CODEC,
                HattenedNetworking::handleConfetti
        );
    }

    /** 在确认处于客户端后再加载吸取特效处理器，避免专服加载客户端类。 */
    private static void handleSuckItem(SuckItemPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            HattenedClientPayloadHandler.handleSuckItem(payload);
        }
    }

    /** 在确认处于客户端后再加载五彩纸屑处理器，避免专服加载客户端类。 */
    private static void handleConfetti(ConfettiPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            HattenedClientPayloadHandler.handleConfetti(payload);
        }
    }

    /** 阻止实例化只提供网络注册功能的工具类。 */
    private HattenedNetworking() {
    }
}
