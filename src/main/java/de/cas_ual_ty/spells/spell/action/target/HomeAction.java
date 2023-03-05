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
import de.cas_ual_ty.spells.spell.projectile.HomingSpellProjectile;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.entity.Entity;

public class HomeAction extends AffectSingleTypeAction<EntityTarget>
{
    public static Codec<HomeAction> makeCodec(SpellActionType<HomeAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.multiTarget()).forGetter(HomeAction::getTarget),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDoubleImm("velocity")).forGetter(HomeAction::getVelocity),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramIntImm("timeout")).forGetter(HomeAction::getTimeout),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("block_hit_activation")).forGetter(HomeAction::getBlockHitActivation),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("entity_hit_activation")).forGetter(HomeAction::getEntityHitActivation),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("timeout_activation")).forGetter(HomeAction::getTimeoutActivation),
                Codec.STRING.fieldOf(ParamNames.destinationTarget("projectile")).forGetter(HomeAction::getProjectileDestination)
        ).apply(instance, (activation, targets, target, velocity, timeout, blockHitActivation, entityHitActivation, timeoutActivation, projectileDestination) -> new HomeAction(type, activation, targets, target, velocity, timeout, blockHitActivation, entityHitActivation, timeoutActivation, projectileDestination)));
    }
    
    public static HomeAction make(String activation, String source, String target, DynamicCtxVar<Double> velocity, DynamicCtxVar<Integer> timeout, String blockHitActivation, String entityHitActivation, String timeoutActivation, String projectileDestination)
    {
        return new HomeAction(SpellActionTypes.HOME.get(), activation, source, target, velocity, timeout, blockHitActivation, entityHitActivation, timeoutActivation, projectileDestination);
    }
    
    protected String target;
    protected DynamicCtxVar<Double> velocity;
    
    protected DynamicCtxVar<Integer> timeout;
    
    protected String blockHitActivation;
    protected String entityHitActivation;
    protected String timeoutActivation;
    protected String projectileDestination;
    
    public HomeAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public HomeAction(SpellActionType<?> type, String activation, String source, String target, DynamicCtxVar<Double> velocity, DynamicCtxVar<Integer> timeout, String blockHitActivation, String entityHitActivation, String timeoutActivation, String projectileDestination)
    {
        super(type, activation, source);
        this.target = target;
        this.velocity = velocity;
        this.timeout = timeout;
        this.blockHitActivation = blockHitActivation;
        this.entityHitActivation = entityHitActivation;
        this.timeoutActivation = timeoutActivation;
        this.projectileDestination = projectileDestination;
    }
    
    public String getTarget()
    {
        return target;
    }
    
    public DynamicCtxVar<Double> getVelocity()
    {
        return velocity;
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
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, EntityTarget entityTarget)
    {
        velocity.getValue(ctx).ifPresent(velocity ->
        {
            timeout.getValue(ctx).ifPresent(timeout ->
            {
                ctx.getTargetGroup(target).forEachTarget(target1 ->
                {
                    TargetTypes.ENTITY.get().ifType(target1, target ->
                    {
                        Entity e = HomingSpellProjectile.home(entityTarget.getEntity(), target.getEntity(), ctx.spell, velocity.floatValue(), timeout, blockHitActivation, entityHitActivation, timeoutActivation);
                        if(e != null)
                        {
                            ctx.getOrCreateTargetGroup(projectileDestination).addTargets(Target.of(e));
                        }
                    });
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