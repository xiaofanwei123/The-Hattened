package com.xfw.hattened.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.xfw.hattened.HattenedMain;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class ConfettiParticle extends TextureSheetParticle {
    private final double particleId;
    private float pitch = 0f;
    private float yaw = 0f;
    private float lastPitch = 0f;
    private float lastYaw = 0f;
    private float lastAngle = 0f;
    private float deltaPitch = 0f;
    private float deltaYaw = 0f;
    private float deltaRoll = 0f;

    private static final ImprovedNoise X_NOISE = createNoise(58637214);
    private static final ImprovedNoise Z_NOISE = createNoise(823917);
    private static final ImprovedNoise YAW_NOISE = createNoise(28943157);
    private static final ImprovedNoise ROLL_NOISE = createNoise(80085);
    private static final ImprovedNoise PITCH_NOISE = createNoise(49715286);

    private static ImprovedNoise createNoise(int seed) {
        return new ImprovedNoise(RandomSource.create(seed));
    }

    protected ConfettiParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.setSprite(sprites.get(this.random));
        this.particleId = this.random.nextDouble();
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.setSize(0.001f, 0.001f);
        this.gravity = 0.2f;
        this.friction = 0.9f;
        this.lifetime = this.random.nextInt(400) + 300;
        this.quadSize *= 1.25f;
        this.yaw = HattenedMain.RANDOM.nextFloat() * Mth.TWO_PI;
        this.pitch = HattenedMain.RANDOM.nextFloat() * Mth.TWO_PI;
        this.roll = HattenedMain.RANDOM.nextFloat() * Mth.TWO_PI;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.lastPitch = this.pitch;
        this.lastYaw = this.yaw;
        this.lastAngle = this.roll;

        this.xd += X_NOISE.noise(this.particleId, this.age, 0) / 100.0;
        this.zd += Z_NOISE.noise(this.particleId, this.age, 0) / 100.0;

        this.yd -= 0.04D * (double)this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;

        if (this.onGround || (this.x == this.xo && this.y == this.yo && this.z == this.zo && this.age != 0)) {
            if (this.age < this.lifetime - 5) {
                this.age = this.lifetime - 5;
            }
            this.alpha = (float)(this.lifetime - this.age) / 5.0F;
        } else {
            this.deltaYaw += (float)(YAW_NOISE.noise(this.particleId, this.age, 0)) / 10.0f;
            this.deltaRoll += (float)(ROLL_NOISE.noise(this.particleId, this.age, 0)) / 10.0f;
            this.deltaPitch += (float)(PITCH_NOISE.noise(this.particleId, this.age, 0)) / 10.0f;

            this.yaw += this.deltaYaw;
            this.pitch += this.deltaPitch;
            this.roll += this.deltaRoll;
        }
        this.deltaYaw *= 0.98f;
        this.deltaRoll *= 0.98f;
        this.deltaPitch *= 0.98f;
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        float interpolatedX = (float)(Mth.lerp(partialTicks, this.xo, this.x) - renderInfo.getPosition().x());
        float interpolatedY = (float)(Mth.lerp(partialTicks, this.yo, this.y) - renderInfo.getPosition().y());
        float interpolatedZ = (float)(Mth.lerp(partialTicks, this.zo, this.z) - renderInfo.getPosition().z());
        float interpolatedPitch = Mth.lerp(partialTicks, this.lastPitch, this.pitch);
        float interpolatedYaw = Mth.lerp(partialTicks, this.lastYaw, this.yaw);
        float interpolatedRoll = Mth.lerp(partialTicks, this.lastAngle, this.roll);
        Quaternionf rotation = new Quaternionf()
                .rotateZ(interpolatedRoll)
                .rotateY(interpolatedYaw)
                .rotateX(interpolatedPitch);
        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-this.quadSize, -this.quadSize, 0.0f),
                new Vector3f(-this.quadSize, this.quadSize, 0.0f),
                new Vector3f(this.quadSize, this.quadSize, 0.0f),
                new Vector3f(this.quadSize, -this.quadSize, 0.0f)
        };
        for (Vector3f vertex : vertices) {
            vertex.rotate(rotation);
            vertex.add(interpolatedX, interpolatedY, interpolatedZ);
        }
        float minU = this.getU0();
        float maxU = this.getU1();
        float minV = this.getV0();
        float maxV = this.getV1();
        int light = this.getLightColor(partialTicks);

        buffer.addVertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).setUv(maxU, maxV).setColor(1.0F, 1.0F, 1.0F, this.alpha).setLight(light);
        buffer.addVertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).setUv(maxU, minV).setColor(1.0F, 1.0F, 1.0F, this.alpha).setLight(light);
        buffer.addVertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).setUv(minU, minV).setColor(1.0F, 1.0F, 1.0F, this.alpha).setLight(light);
        buffer.addVertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).setUv(minU, maxV).setColor(1.0F, 1.0F, 1.0F, this.alpha).setLight(light);

        buffer.addVertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).setUv(minU, maxV).setColor(1.0F, 1.0F, 1.0F, this.alpha).setLight(light);
        buffer.addVertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).setUv(minU, minV).setColor(1.0F, 1.0F, 1.0F, this.alpha).setLight(light);
        buffer.addVertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).setUv(maxU, minV).setColor(1.0F, 1.0F, 1.0F, this.alpha).setLight(light);
        buffer.addVertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).setUv(maxU, maxV).setColor(1.0F, 1.0F, 1.0F, this.alpha).setLight(light);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new ConfettiParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}