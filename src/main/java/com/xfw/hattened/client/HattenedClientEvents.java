package com.xfw.hattened.client;

import com.xfw.hattened.HattenedMain;
import com.xfw.hattened.client.model.HatModel;
import com.xfw.hattened.client.model.HatPlayerModel;
import com.xfw.hattened.client.particle.ConfettiParticle;
import com.xfw.hattened.client.renderer.HatFeatureRenderer;
import com.xfw.hattened.client.renderer.HatGUI;
import com.xfw.hattened.client.renderer.HatTooltipComponent;
import com.xfw.hattened.item.HatItem;
import com.xfw.hattened.misc.*;
import com.xfw.hattened.networking.HatInputPayload;
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

import java.util.Set;

@EventBusSubscriber(modid = HattenedMain.MODID, value = Dist.CLIENT)
/** 连接客户端输入、玩家动画、粒子、帽子渲染层和 HUD 绘制事件。 */
public final class HattenedClientEvents {

    /** 每个客户端 Tick 检查按键变化并推进帽子界面动画。 */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        if (Minecraft.getInstance().player != null) {
            var player = Minecraft.getInstance().player;
            HatData currentHatData = HattenedHelper.getHatData(player);
            ClientStorage.tick(currentHatData);
            if (currentHatData.hasHat()) {
                PeripheralManager.tick();
                ClientStorage.ticks += 1;
            }
        }
    }

    /** 帽子菜单接管输入时，用滚轮切换当前选中的存储卡片。 */
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

    /** 将鼠标按键变化转换为帽子控制消息，并交给服务端校验执行。 */
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

    /** 注册打开帽子菜单的 Alt 键和脱下帽子的 H 键。 */
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        PeripheralManager.init(event);
    }

    /** 注册客户端烘焙帽子几何体所需的模型层定义。 */
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(HatModel.LAYER_LOCATION, HatModel::createBodyLayer);
    }

    /** 为粗手臂和细手臂两种玩家皮肤模型添加帽子渲染层。 */
    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        addHatLayer(event, PlayerSkin.Model.WIDE);
        addHatLayer(event, PlayerSkin.Model.SLIM);
    }

    /** 为已注册的五彩纸屑粒子类型绑定客户端粒子工厂。 */
    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(HattenedMain.CONFETTI_PARTICLE.get(), ConfettiParticle.Provider::new);
    }

    /** 在指定玩家渲染器存在时添加一个帽子特征渲染层。 */
    private static void addHatLayer(EntityRenderersEvent.AddLayers event, PlayerSkin.Model skinModel) {
        PlayerRenderer renderer = event.getSkin(skinModel);
        if (renderer != null) {
            HatModel hatModel = new HatModel(event.getEntityModels().bakeLayer(HatModel.LAYER_LOCATION));
            renderer.addLayer(new HatFeatureRenderer(renderer, hatModel));
        }
    }

    /** 注册帽子存储卡片条对应的客户端工具提示渲染器。 */
    @SubscribeEvent
    public static void onRegisterClientTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(HatItem.HatTooltipData.class, HatTooltipComponent::new);
    }

    /** 为每名客户端玩家安装只在帽子动作期间启用的 Player Animator 动画层。 */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player,stack) -> {
            var hatPlayerModel = new HatPlayerModel(player);
            var animation = new ModifierLayer<>(hatPlayerModel);
            
            PlayerAnimationAccess.getPlayerAssociatedData(player).set(HattenedMain.id("hat"), animation);
            stack.addAnimLayer(10000, animation);
        });
    }

    private static final Set<ResourceLocation> OFFSET_LAYERS = Set.of(
            ResourceLocation.withDefaultNamespace("hotbar"),
            ResourceLocation.withDefaultNamespace("player_health"),
            ResourceLocation.withDefaultNamespace("food_level"),
            ResourceLocation.withDefaultNamespace("air_level"),
            ResourceLocation.withDefaultNamespace("armor_level"),
            ResourceLocation.withDefaultNamespace("experience_bar"),
            ResourceLocation.withDefaultNamespace("jump_meter"),
            ResourceLocation.withDefaultNamespace("vehicle_health")
    );


    /** 在受影响的 HUD 图层绘制前压入位移矩阵，使物品栏随菜单下移。 */
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

    /** 在受影响的 HUD 图层绘制后恢复矩阵，避免位移影响后续界面。 */
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

    /** 在已下移的 HUD 上方绘制展开的帽子卡片菜单。 */
    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Pre event) {
        float progress = ClientStorage.getProgress(event.getPartialTick());
        if (progress > 0.0f) {
            HatGUI.render(event.getGuiGraphics(), progress,
                    event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }
    }

    /** 判断当前 HUD 图层是否应跟随帽子菜单的展开距离下移。 */
    private static boolean shouldOffsetLayer(ResourceLocation layerName) {
        return OFFSET_LAYERS.contains(layerName);
    }

    /** 阻止实例化只用于承载客户端事件处理器的工具类。 */
    private HattenedClientEvents() {
    }
}
