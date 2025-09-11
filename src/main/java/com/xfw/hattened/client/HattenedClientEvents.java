package com.xfw.hattened.client;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.client.model.HatModel;
import com.xfw.hattened.client.model.HatPlayerModel;
import com.xfw.hattened.client.particle.ConfettiParticle;
import com.xfw.hattened.client.renderer.HatFeatureRenderer;
import com.xfw.hattened.client.renderer.HatGUI;
import com.xfw.hattened.client.renderer.HatTooltipComponent;
import com.xfw.hattened.item.HatItem;
import com.xfw.hattened.misc.HattenedHelper;
import com.xfw.hattened.misc.PeripheralManager;
import com.xfw.hattened.networking.HatInputPayload;
import com.xfw.hattened.misc.UserInput;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = HattenedMain.MODID, value = Dist.CLIENT)
public class HattenedClientEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        if(Minecraft.getInstance().player != null && HattenedHelper.getHatData(Minecraft.getInstance().player).hasHat()) {
            PeripheralManager.tick();
            ClientStorage.ticks += 1;
            ClientStorage.tick(HattenedHelper.getHatData(Minecraft.getInstance().player));
        }
    }

    //切换卡片
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.screen == null && PeripheralManager.shouldIntercept()) {
            if (event.getScrollDeltaY() < 0) {
                PacketDistributor.sendToServer(new HatInputPayload(UserInput.SCROLL_UP));
                event.setCanceled(true);
            } else if (event.getScrollDeltaY() > 0) {
                PacketDistributor.sendToServer(new HatInputPayload(UserInput.SCROLL_DOWN));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.screen == null && PeripheralManager.shouldIntercept()) {
            if (event.getButton() == 0) { //左键
                if (event.getAction() == 1) { //PRESS
                    PacketDistributor.sendToServer(new HatInputPayload(UserInput.LEFT_MOUSE_PRESSED));
                    event.setCanceled(true);
                } else if (event.getAction() == 0) { //RELEASE
                    PacketDistributor.sendToServer(new HatInputPayload(UserInput.LEFT_MOUSE_RELEASED));
                    event.setCanceled(true);
                }
            } else if (event.getButton() == 1) { //右键
                if (event.getAction() == 1) { //PRESS
                    PacketDistributor.sendToServer(new HatInputPayload(UserInput.RIGHT_MOUSE_PRESSED));
                    event.setCanceled(true);
                } else if (event.getAction() == 0) { //RELEASE
                    PacketDistributor.sendToServer(new HatInputPayload(UserInput.RIGHT_MOUSE_RELEASED));
                    event.setCanceled(true);
                }
            } else if (event.getButton() == 2 && event.getAction() == 1) { //中键按下
                PacketDistributor.sendToServer(new HatInputPayload(UserInput.MIDDLE_MOUSE_PRESSED));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        PeripheralManager.init(event);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(HatModel.LAYER_LOCATION, HatModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        //为所有玩家渲染器添加帽子特征渲染器
        addHatLayer(event, PlayerSkin.Model.WIDE);
        addHatLayer(event, PlayerSkin.Model.SLIM);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(HattenedMain.CONFETTI_PARTICLE.get(), ConfettiParticle.Provider::new);
    }

    @SuppressWarnings("unchecked")
    private static void addHatLayer(EntityRenderersEvent.AddLayers event, PlayerSkin.Model skinModel) {
        PlayerRenderer renderer = event.getSkin(skinModel);
        if (renderer != null) {
            HatModel hatModel = new HatModel(event.getEntityModels().bakeLayer(HatModel.LAYER_LOCATION));
            renderer.addLayer(new HatFeatureRenderer(renderer, hatModel));
        }
    }

    @SubscribeEvent
    public static void onRegisterClientTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(HatItem.HatTooltipData.class, HatTooltipComponent::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player,stack) -> {
            var animation = new ModifierLayer(new HatPlayerModel(player));
            PlayerAnimationAccess.getPlayerAssociatedData(player).set(HattenedMain.id("hat"), animation);
            stack.addAnimLayer(10000, animation);
        });
    }

    //需要下移的HUD层名称
    private static final ResourceLocation HOTBAR = ResourceLocation.withDefaultNamespace("hotbar");
    private static final ResourceLocation PLAYER_HEALTH = ResourceLocation.withDefaultNamespace("player_health");
    private static final ResourceLocation FOOD_LEVEL = ResourceLocation.withDefaultNamespace("food_level");
    private static final ResourceLocation AIR_LEVEL = ResourceLocation.withDefaultNamespace("air_level");
    private static final ResourceLocation ARMOR_LEVEL = ResourceLocation.withDefaultNamespace("armor_level");
    private static final ResourceLocation EXPERIENCE_BAR = ResourceLocation.withDefaultNamespace("experience_bar");
    private static final ResourceLocation JUMP_METER = ResourceLocation.withDefaultNamespace("jump_meter");
    private static final ResourceLocation VEHICLE_HEALTH = ResourceLocation.withDefaultNamespace("vehicle_health");


    @SubscribeEvent
    public static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
        ResourceLocation layerName = event.getName();
        if (shouldOffsetLayer(layerName)) {
            float progress = ClientStorage.getProgress(event.getPartialTick());
            if (progress > 0.0f) {
                event.getGuiGraphics().pose().pushPose();
                event.getGuiGraphics().pose().translate(0.0f, progress * 60.0f, 0.0f);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayerPost(RenderGuiLayerEvent.Post event) {
        ResourceLocation layerName = event.getName();
        if (shouldOffsetLayer(layerName)) {
            float progress = ClientStorage.getProgress(event.getPartialTick());
            if (progress > 0.0f) {
                event.getGuiGraphics().pose().popPose();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        float progress = ClientStorage.getProgress(event.getPartialTick());
        if (progress > 0.0f) {
            HatGUI.render(event.getGuiGraphics(), progress);
        }
    }

    //检查是否是需要下移的HUD元素
    private static boolean shouldOffsetLayer(ResourceLocation layerName) {
        return layerName.equals(HOTBAR) ||
                layerName.equals(PLAYER_HEALTH) ||
                layerName.equals(FOOD_LEVEL) ||
                layerName.equals(AIR_LEVEL) ||
                layerName.equals(ARMOR_LEVEL) ||
                layerName.equals(EXPERIENCE_BAR) ||
                layerName.equals(JUMP_METER) ||
                layerName.equals(VEHICLE_HEALTH);
    }
}