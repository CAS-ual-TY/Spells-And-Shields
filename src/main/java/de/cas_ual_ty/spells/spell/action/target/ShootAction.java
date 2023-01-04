package de.cas_ual_ty.spells.spell.action.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.effect.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.base.SpellProjectile;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;

public class ShootAction extends AffectSingleTypeAction<EntityTarget>
{
    public static Codec<ShootAction> makeCodec(SpellActionType<ShootAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDoubleImm("velocity")).forGetter(ShootAction::getVelocity),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDoubleImm("inaccuracy")).forGetter(ShootAction::getInaccuracy),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramIntImm("timeout")).forGetter(ShootAction::getTimeout),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("block_hit_activation")).forGetter(ShootAction::getBlockHitActivation),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("entity_hit_activation")).forGetter(ShootAction::getEntityHitActivation),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("timeout_activation")).forGetter(ShootAction::getTimeoutActivation)
        ).apply(instance, (activation, targets, velocity, inaccuracy, timeout, blockHitActivation, entityHitActivation, timeoutActivation) -> new ShootAction(type, activation, targets, velocity, inaccuracy, timeout, blockHitActivation, entityHitActivation, timeoutActivation)));
    }
    
    protected DynamicCtxVar<Double> velocity;
    protected DynamicCtxVar<Double> inaccuracy;
    
    protected DynamicCtxVar<Integer> timeout;
    
    protected String blockHitActivation;
    protected String entityHitActivation;
    protected String timeoutActivation;
    
    public ShootAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ShootAction(SpellActionType<?> type, String activation, String source, DynamicCtxVar<Double> velocity, DynamicCtxVar<Double> inaccuracy, DynamicCtxVar<Integer> timeout, String blockHitActivation, String entityHitActivation, String timeoutActivation)
    {
        super(type, activation, source);
        this.velocity = velocity;
        this.inaccuracy = inaccuracy;
        this.timeout = timeout;
        this.blockHitActivation = blockHitActivation;
        this.entityHitActivation = entityHitActivation;
        this.timeoutActivation = timeoutActivation;
    }
    
    public DynamicCtxVar<Double> getVelocity()
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
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, EntityTarget entityTarget)
    {
        velocity.getValue(ctx).ifPresent(velocity ->
        {
            inaccuracy.getValue(ctx).ifPresent(inaccuracy ->
            {
                timeout.getValue(ctx).ifPresent(timeout ->
                {
                    SpellProjectile.shoot(entityTarget.getEntity(), ctx.spell, velocity.floatValue(), inaccuracy.floatValue(), timeout, blockHitActivation, entityHitActivation, timeoutActivation);
                });
            });
        });
    }
    
    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
}
