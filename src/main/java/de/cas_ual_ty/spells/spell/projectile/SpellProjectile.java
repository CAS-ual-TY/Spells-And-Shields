package de.cas_ual_ty.spells.spell.projectile;

import de.cas_ual_ty.spells.progression.FullSpellNodeId;
import de.cas_ual_ty.spells.registers.BuiltInRegisters;
import de.cas_ual_ty.spells.registers.SpellTrees;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups;
import de.cas_ual_ty.spells.spell.target.Target;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SpellProjectile extends AbstractHurtingProjectile
{
    protected SpellInstance spell;

    protected int timeout;

    protected String blockHitActivation;
    protected String entityHitActivation;
    protected String timeoutActivation;

    protected ParticleOptions particle;

    public SpellProjectile(EntityType<? extends SpellProjectile> entityType, Level level)
    {
        this(entityType, level, null, -1, null, null, null, null);
    }

    public SpellProjectile(EntityType<? extends AbstractHurtingProjectile> entityType, Level pLevel, SpellInstance spell, int timeout, String blockHitActivation, String entityHitActivation, String timeoutActivation, ParticleOptions particle)
    {
        super(entityType, pLevel);
        this.spell = spell;
        this.timeout = timeout;
        this.blockHitActivation = blockHitActivation;
        this.entityHitActivation = entityHitActivation;
        this.timeoutActivation = timeoutActivation;
        this.particle = particle;
        this.accelerationPower = 0;
    }

    @Override
    protected float getInertia()
    {
        return 1F;
    }

    @Override
    protected ParticleOptions getTrailParticle()
    {
        return this.particle;
    }

    @Override
    public void tick()
    {
        super.tick();

        lockRotationToVelocity();

        if(spell != null && !level().isClientSide())
        {
            if(tickCount >= timeout)
            {
                spell.forceRun(level(), getPlayerOwner(), timeoutActivation, (ctx) ->
                {
                    ctx.getOrCreateTargetGroup(BuiltinTargetGroups.PROJECTILE.targetGroup).addTargets(Target.of(this));
                });
                discard();
            }
        }
    }

    /**
     * AbstractHurtingProjectile#tick() unconditionally calls ProjectileUtil.rotateTowardsMovement(this, 0.2F),
     * which drags xRot/yRot 20%/tick towards ITS OWN atan2 convention - one that matches neither
     * Projectile#shoot()'s convention nor the "true" look-direction convention used everywhere else
     * (Entity#calculateViewVector/getViewYRot/getViewXRot, Entity#lookAt) - visibly rotating away from the
     * correct heading over several ticks. Since this projectile's velocity direction never actually changes
     * (getInertia() == 1F, no acceleration), just re-lock rotation to the true instantaneous heading, using the
     * SAME formula as Entity#lookAt (confirmed by inverting calculateViewVector too): yaw/pitch here are the
     * NEGATION of what Projectile#shoot() itself sets - shoot()'s own atan2(dx,dz)/atan2(dy,horiz) do not match
     * the look-direction convention either, they just happen to render correctly for arrows because
     * ArrowRenderer applies its own compensating offset.
     * <p>
     * Called both every tick AND synchronously right after {@link #shoot(double, double, double, float, float)}
     * in the static factories below - the latter closes the window where anything reading this entity's rotation
     * (eg. a particle emitter's one-time {@code initial_position} evaluation) before its first tick() would
     * otherwise still see vanilla shoot()'s un-corrected convention.
     */
    protected void lockRotationToVelocity()
    {
        Vec3 motion = getDeltaMovement();
        if(motion.lengthSqr() > 0.0D)
        {
            double horizontalDistance = motion.horizontalDistance();
            setYRot((float) (Mth.atan2(-motion.x, motion.z) * 180.0F / (float) Math.PI));
            setXRot((float) (Mth.atan2(-motion.y, horizontalDistance) * 180.0F / (float) Math.PI));
            yRotO = getYRot();
            xRotO = getXRot();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult)
    {
        if(spell != null && !level().isClientSide() && !(entityHitResult.getEntity() instanceof SpellProjectile))
        {
            spell.forceRun(level(), getPlayerOwner(), entityHitActivation, (ctx) ->
            {
                ctx.getOrCreateTargetGroup(BuiltinTargetGroups.PROJECTILE.targetGroup).addTargets(Target.of(this));
                ctx.getOrCreateTargetGroup(BuiltinTargetGroups.ENTITY_HIT.targetGroup).addTargets(Target.of(entityHitResult.getEntity()));

                Vec3 clip = entityHitResult.getEntity().getBoundingBox().clip(position().subtract(getDeltaMovement()), position().add(getDeltaMovement())).orElse(entityHitResult.getEntity().getEyePosition());
                ctx.getOrCreateTargetGroup(BuiltinTargetGroups.HIT_POSITION.targetGroup).addTargets(Target.of(level(), clip));
            });

            discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult)
    {
        if(spell != null && !level().isClientSide())
        {
            spell.forceRun(level(), getPlayerOwner(), blockHitActivation, (ctx) ->
            {
                ctx.getOrCreateTargetGroup(BuiltinTargetGroups.PROJECTILE.targetGroup).addTargets(Target.of(this));
                ctx.getOrCreateTargetGroup(BuiltinTargetGroups.BLOCK_HIT.targetGroup).addTargets(Target.of(level(), blockHitResult.getBlockPos()));
                ctx.getOrCreateTargetGroup(BuiltinTargetGroups.HIT_POSITION.targetGroup).addTargets(Target.of(level(), blockHitResult.getLocation()));
            });

            discard();
        }
    }

    @Override
    public boolean shouldBurn()
    {
        return false;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet)
    {
        super.recreateFromPacket(packet);

        // Entity#recreateFromPacket sets xRot/yRot from the packet but leaves xRotO/yRotO at their
        // constructor-default 0 for one tick, so partial-tick-interpolated rotation reads (eg. the particle
        // emitter's RELATIVE rotation attach) blend from 0 towards the real heading for that first tick instead
        // of already being correct.
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }
    
    @Nullable
    public SpellInstance getSpell()
    {
        return spell;
    }

    public void setSpell(SpellInstance spell)
    {
        this.spell = spell;
    }

    @Nullable
    public Player getPlayerOwner()
    {
        if(getOwner() instanceof ServerPlayer player && !player.hasDisconnected())
        {
            return player;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);

        if(spell != null)
        {
            if(spell.getNodeId() != null)
            {
                CompoundTag tag = new CompoundTag();
                spell.getNodeId().toNbt(tag);
                nbt.put("Spell", tag);
            }
            else
            {
                Registry<Spell> spellRegistry = Spells.getRegistry(level());
                nbt.putString("spellId", spell.getSpell().unwrap().map(ResourceKey::location, spellRegistry::getKey).toString());
            }
        }
        else
        {
            discard();
        }

        nbt.putInt("Timeout", timeout);
        nbt.putString("BlockHitActivation", blockHitActivation);
        nbt.putString("EntityHitActivation", entityHitActivation);
        nbt.putString("TimeoutActivation", timeoutActivation);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);

        if(nbt.contains("Spell", CompoundTag.TAG_COMPOUND))
        {
            FullSpellNodeId fullSpellNodeId = FullSpellNodeId.fromNbt(nbt.getCompound("Spell"));

            if(fullSpellNodeId != null)
            {
                spell = fullSpellNodeId.getSpellInstance(SpellTrees.getRegistry(level()));
            }
            else if(nbt.contains("spellId", Tag.TAG_STRING))
            {
                Registry<Spell> spellRegistry = Spells.getRegistry(level());
                Holder<Spell> holder = spellRegistry.getHolder(ResourceKey.create(Spells.REGISTRY_KEY, ResourceLocation.parse(nbt.getString("spellId")))).orElse(null);

                if(holder != null)
                {
                    spell = SpellInstance.direct(holder);
                }
            }
        }

        if(spell == null)
        {
            discard();
            return;
        }

        timeout = nbt.getInt("Timeout");
        blockHitActivation = nbt.getString("BlockHitActivation");
        entityHitActivation = nbt.getString("EntityHitActivation");
        timeoutActivation = nbt.getString("TimeoutActivation");
    }

    public static SpellProjectile shoot(Level level0, Vec3 position, Vec3 direction, @Nullable Entity source, SpellInstance spell, float velocity, float inaccuracy, int timeout, String blockHitActivation, String entityHitActivation, String timeoutActivation, ParticleOptions particle)
    {
        if(level0 instanceof ServerLevel level)
        {
            SpellProjectile projectile = new SpellProjectile(BuiltInRegisters.SPELL_PROJECTILE.get(), level, spell, timeout, blockHitActivation, entityHitActivation, timeoutActivation, particle);
            projectile.setOwner(source);

            projectile.moveTo(position.x, position.y, position.z, 0F, 0F);
            projectile.shoot(direction.x, direction.y, direction.z, velocity, inaccuracy);

            projectile.lockRotationToVelocity();

            level.addFreshEntity(projectile);

            return projectile;
        }

        return null;
    }

    public static SpellProjectile shoot(Entity source, SpellInstance spell, float velocity, float inaccuracy, int timeout, String blockHitActivation, String entityHitActivation, String timeoutActivation, ParticleOptions particle)
    {
        return shoot(source.level(), source.getEyePosition(), source.getLookAngle().normalize(), source, spell, velocity, inaccuracy, timeout, blockHitActivation, entityHitActivation, timeoutActivation, particle);
    }
}
