package com.xfw.hattened.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** 复刻原版物品拾取动画，但让物品飞向玩家视线前方的帽子位置。 */
public final class HatItemPickupParticle extends Particle {
    private static final int LIFE_TIME = 3;
    private static final double FORWARD_OFFSET = 0.9;

    private final RenderBuffers renderBuffers;
    private final Entity itemEntity;
    private final Entity collector;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private int life;
    private Vec3 target;
    private Vec3 previousTarget;

    /** 复制待吸取实体，并记录会随玩家位置和视线更新的动画目标。 */
    public HatItemPickupParticle(EntityRenderDispatcher entityRenderDispatcher, RenderBuffers renderBuffers,
                                 ClientLevel level, Entity itemEntity, Entity collector) {
        super(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                itemEntity.getDeltaMovement().x, itemEntity.getDeltaMovement().y, itemEntity.getDeltaMovement().z);
        this.renderBuffers = renderBuffers;
        this.itemEntity = itemEntity instanceof ItemEntity item ? item.copy() : itemEntity;
        this.collector = collector;
        this.entityRenderDispatcher = entityRenderDispatcher;
        this.target = calculateTarget();
        this.previousTarget = this.target;
    }

    /** 使用自定义实体渲染通道绘制物品模型。 */
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    /** 按原版二次插值轨迹把物品模型绘制到玩家前方的动态目标。 */
    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        float progress = (this.life + partialTick) / LIFE_TIME;
        progress *= progress;
        double targetX = Mth.lerp(partialTick, this.previousTarget.x, this.target.x);
        double targetY = Mth.lerp(partialTick, this.previousTarget.y, this.target.y);
        double targetZ = Mth.lerp(partialTick, this.previousTarget.z, this.target.z);
        double renderX = Mth.lerp(progress, this.itemEntity.getX(), targetX);
        double renderY = Mth.lerp(progress, this.itemEntity.getY(), targetY);
        double renderZ = Mth.lerp(progress, this.itemEntity.getZ(), targetZ);
        Vec3 cameraPosition = camera.getPosition();
        MultiBufferSource.BufferSource buffers = this.renderBuffers.bufferSource();

        this.entityRenderDispatcher.render(this.itemEntity,
                renderX - cameraPosition.x, renderY - cameraPosition.y, renderZ - cameraPosition.z,
                this.itemEntity.getYRot(), partialTick, new PoseStack(), buffers,
                this.entityRenderDispatcher.getPackedLightCoords(this.itemEntity, partialTick));
        buffers.endBatch();
    }

    /** 推进三 Tick 生命周期，并让终点持续跟随玩家当前视线。 */
    @Override
    public void tick() {
        if (++this.life == LIFE_TIME) {
            remove();
        }
        this.previousTarget = this.target;
        this.target = calculateTarget();
    }

    /** 计算玩家身体中部沿视线前移 0.5 格的位置。 */
    private Vec3 calculateTarget() {
        Vec3 forward = this.collector.getLookAngle().scale(FORWARD_OFFSET);
        double centerY = (this.collector.getY() + this.collector.getEyeY()) / 2.0;
        return new Vec3(this.collector.getX(), centerY, this.collector.getZ()).add(forward);
    }

    /** 始终允许短生命周期动画被渲染，避免移动目标造成视锥误判。 */
    @Override
    public AABB getRenderBoundingBox(float partialTick) {
        return AABB.INFINITE;
    }
}
