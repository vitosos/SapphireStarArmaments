package vitosos.sapphireweapons.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import vitosos.sapphireweapons.util.IInsectGlaiveUser;
import net.minecraft.entity.mob.HostileEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class KinsectEntity extends ThrownItemEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean isReturning = false;
    private Vec3d startPos;
    private float damage = 0f;

    private boolean isNuke = false;

    public void setNuke(boolean isNuke) {
        this.isNuke = isNuke;
    }

    // Required default constructor
    public KinsectEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    // Custom constructor for launching
    public KinsectEntity(World world, LivingEntity owner, float damage) {
        super(vitosos.sapphireweapons.registry.ModEntities.KINSECT_ENTITY_TYPE, owner, world);
        this.damage = damage;
        this.startPos = owner.getPos();
        this.setNoGravity(true); // Ensures it flies in a straight line
    }

    @Override
    protected Item getDefaultItem() {
        return vitosos.sapphireweapons.registry.ModItems.KINSECT_ITEM;
    }

    @Override
    public void tick() {
        super.tick();

        // --- 1. VISUAL ROTATION (Runs on both Client & Server) ---
        Vec3d vel = this.getVelocity();
        if (vel.lengthSquared() > 0.0001) {
            double horizontalLength = vel.horizontalLength();
            // Standard Minecraft math to turn velocity into Yaw and Pitch
            this.setYaw((float)(net.minecraft.util.math.MathHelper.atan2(vel.x, vel.z) * 57.2957763671875));
            this.setPitch((float)(net.minecraft.util.math.MathHelper.atan2(vel.y, horizontalLength) * 57.2957763671875));

            // Sync previous rotations to prevent visual stuttering
            this.prevYaw = this.getYaw();
            this.prevPitch = this.getPitch();
        }

        // --- 2. SERVER LOGIC ---
        if (this.getWorld().isClient()) return;

        Entity owner = this.getOwner();
        if (owner == null || !owner.isAlive()) {
            this.discard(); // Destroy if owner disconnects or dies [cite: 86]
            return;
        }

        if (!isReturning) {
            // Check if it reached the 15-block maximum range (Changed from 20.0)
            if (this.startPos != null && this.startPos.distanceTo(this.getPos()) >= 15.0) {
                this.isReturning = true;
            }
        } else {
            // Homing logic: Calculate direction back to the owner's chest/eyes
            Vec3d targetDir = owner.getEyePos().subtract(this.getPos()).normalize();

            // Reduced return speed multiplier from 1.5 to 1.0
            this.setVelocity(targetDir.multiply(1.0));

            // Check if the Kinsect physically touched the owner to "dock"
            if (this.getBoundingBox().intersects(owner.getBoundingBox())) {
                if (owner instanceof IInsectGlaiveUser glaiveUser) {
                    glaiveUser.setKinsectDeployed(false); // Unlock the launch ability! [cite: 90]
                }
                this.discard();
            }
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (!this.getWorld().isClient() && !this.isReturning) {
            Entity target = entityHitResult.getEntity();

            if (target != this.getOwner()) {
                // Check if owner is a Glaive User to apply standard boosts
                float finalDamage = this.damage;
                if (this.getOwner() instanceof IInsectGlaiveUser glaiveUser) {
                    if (glaiveUser.getTripleBuffTicks() > 0) finalDamage *= 1.2f;
                    else if (glaiveUser.getRedEssenceTicks() > 0) finalDamage *= 1.1f;

                    // Harvest Red Essence! (Only if it is NOT a Nuke)
                    if (!this.isNuke && (target instanceof HostileEntity || target instanceof net.minecraft.entity.player.PlayerEntity)) {
                        glaiveUser.grantEssence(0);
                    }
                }

                // --- NEW: NUKE MAGIC & AOE LOGIC ---
                if (this.isNuke) {
                    // 1. Direct Hit: Apply MAGIC damage instead of thrown
                    target.damage(this.getDamageSources().indirectMagic(this, this.getOwner()), finalDamage);

                    if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                        // 2. The Visuals
                        serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
                        serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(), net.minecraft.sound.SoundEvents.ENTITY_GENERIC_EXPLODE, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.0f);

                        // 3. The AoE Splash Damage (4-Block Radius)
                        net.minecraft.util.math.Box explosionBox = this.getBoundingBox().expand(4.0D);
                        java.util.List<Entity> caughtEntities = serverWorld.getOtherEntities(this, explosionBox);

                        for (Entity caught : caughtEntities) {
                            // Don't damage the owner, and don't double-damage the direct target
                            if (caught instanceof LivingEntity livingCaught && caught != this.getOwner() && caught != target) {
                                // Deals 50% of the Nuke's damage as splash magic damage
                                livingCaught.damage(this.getDamageSources().indirectMagic(this, this.getOwner()), finalDamage * 0.5f);
                            }
                        }
                    }
                } else {
                    // Standard Kinsect Hit (Physical Thrown Damage)
                    target.damage(this.getDamageSources().thrown(this, this.getOwner()), finalDamage);
                }

                this.isReturning = true;
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (!this.getWorld().isClient()) {
            this.isReturning = true; // Bounce back if it hits a wall
        }
    }

    // --- NEW: GECKOLIB ANIMATION CONTROLLER ---
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> {

            // Changed "animation.kinsect.fly" to just "fly"
            event.getController().setAnimation(RawAnimation.begin().thenLoop("fly"));

            return PlayState.CONTINUE;
        }));
    }

    // --- COMPLETELY IGNORE WATER DRAG ---
    @Override
    public boolean isTouchingWater() {
        return false;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}