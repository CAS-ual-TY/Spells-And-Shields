package de.cas_ual_ty.spells.spell.action.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.projectile.SpellProjectile;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PositionTarget;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public class ShootAltAction extends AffectSingleTypeAction<PositionTarget>
{
    public static Codec<ShootAltAction> makeCodec(SpellActionType<ShootAltAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                CtxVarTypes.VEC3.get().refCodec().fieldOf(ParamNames.paramDouble("velocity")).forGetter(ShootAltAction::getVelocity),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDouble("inaccuracy")).forGetter(ShootAltAction::getInaccuracy),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("timeout")).forGetter(ShootAltAction::getTimeout),
                Codec.STRING.fieldOf(ParamNames.asynchronousActivation("block_hit_activation")).forGetter(ShootAltAction::getBlockHitActivation),
                Codec.STRING.fieldOf(ParamNames.asynchronousActivation("entity_hit_activation")).forGetter(ShootAltAction::getEntityHitActivation),
                Codec.STRING.fieldOf(ParamNames.asynchronousActivation("timeout_activation")).forGetter(ShootAltAction::getTimeoutActivation),
                Codec.STRING.fieldOf(ParamNames.destinationTarget("projectile")).forGetter(ShootAltAction::getProjectileDestination),
                Codec.optionalField(ParamNames.singleTarget("shooter"), Codec.STRING).xmap(o -> o.orElse(""), s -> s.isEmpty() ? Optional.empty() : Optional.ofNullable(s)).forGetter(ShootAltAction::getShooter)
        ).apply(instance, (activation, source, velocity, inaccuracy, timeout, blockHitActivation, entityHitActivation, timeoutActivation, projectileDestination, shooter) -> new ShootAltAction(type, activation, source, velocity, inaccuracy, timeout, blockHitActivation, entityHitActivation, timeoutActivation, projectileDestination, shooter)));
    }
    
    public static ShootAltAction make(Object activation, Object source, DynamicCtxVar<Vec3> velocity, DynamicCtxVar<Double> inaccuracy, DynamicCtxVar<Integer> timeout, String blockHitActivation, String entityHitActivation, String timeoutActivation, String projectileDestination, @Nullable Object shooter)
    {
        return new ShootAltAction(SpellActionTypes.SHOOT_ALT.get(), activation.toString(), source.toString(), velocity, inaccuracy, timeout, blockHitActivation, entityHitActivation, timeoutActivation, projectileDestination, shooter == null ? "" : shooter.toString());
    }
    
    protected DynamicCtxVar<Vec3> velocity;
    protected DynamicCtxVar<Double> inaccuracy;
    
    protected DynamicCtxVar<Integer> timeout;
    
    protected String blockHitActivation;
    protected String entityHitActivation;
    protected String timeoutActivation;
    protected String projectileDestination;
    
    protected String shooter;
    
    public ShootAltAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ShootAltAction(SpellActionType<?> type, String activation, String source, DynamicCtxVar<Vec3> velocity, DynamicCtxVar<Double> inaccuracy, DynamicCtxVar<Integer> timeout, String blockHitActivation, String entityHitActivation, String timeoutActivation, String projectileDestination, String shooter)
    {
        super(type, activation, source);
        this.velocity = velocity;
        this.inaccuracy = inaccuracy;
        this.timeout = timeout;
        this.blockHitActivation = blockHitActivation;
        this.entityHitActivation = entityHitActivation;
        this.timeoutActivation = timeoutActivation;
        this.projectileDestination = projectileDestination;
        this.shooter = shooter;
    }
    
    public DynamicCtxVar<Vec3> getVelocity()
    {
        return velocity;
    }
    
    public DynamicCtxVar<Double> getInaccuracy()
    {
        return inaccuracy;
    }
    
    public DynamicCtxVar<Integer> getTimeout()
    {
        return timeout;
    }
    
    public String getBlockHitActivation()
    {
        return blockHitActivation;
    }
    
    public String getEntityHitActivation()
    {
        return entityHitActivation;
    }
    
    public String getTimeoutActivation()
    {
        return timeoutActivation;
    }
    
    public String getProjectileDestination()
    {
        return projectileDestination;
    }
    
    public String getShooter()
    {
        return shooter;
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, PositionTarget positionTarget)
    {
        velocity.getValue(ctx).ifPresent(velocity ->
        {
            inaccuracy.getValue(ctx).ifPresent(inaccuracy ->
            {
                timeout.getValue(ctx).ifPresent(timeout ->
                {
                    Entity shooter = ctx.getTargetGroup(this.shooter).getSingleTarget().flatMap(t -> TargetTypes.ENTITY.get().ifType(t).map(EntityTarget::getEntity)).orElse(null);
                    Entity e = SpellProjectile.shoot(ctx.level, positionTarget.getPosition(), velocity, shooter, ctx.spell, inaccuracy.floatValue(), (float) velocity.length(), timeout, blockHitActivation, entityHitActivation, timeoutActivation);
                    if(e != null)
                    {
                        ctx.getOrCreateTargetGroup(projectileDestination).addTargets(Target.of(e));
                    }
                });
            });
        });
    }
    
    @Override
    public ITargetType<PositionTarget> getAffectedType()
    {
        return TargetTypes.POSITION.get();
    }
}
