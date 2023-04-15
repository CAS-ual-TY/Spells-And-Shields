package de.cas_ual_ty.spells.spell.action.fx;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.ParticleEmitterHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

public class ParticleEmitterAction extends AffectTypeAction<EntityTarget>
{
    public static Codec<ParticleEmitterAction> makeCodec(SpellActionType<ParticleEmitterAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("duration")).forGetter(ParticleEmitterAction::getDuration),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("delay")).forGetter(ParticleEmitterAction::getDelay),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("amount")).forGetter(ParticleEmitterAction::getAmount),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDouble("spread")).forGetter(ParticleEmitterAction::getSpread),
                CtxVarTypes.VEC3.get().refCodec().fieldOf(ParamNames.paramVec3("offset")).forGetter(ParticleEmitterAction::getOffset),
                ParticleTypes.CODEC.fieldOf("particle").forGetter(ParticleEmitterAction::getParticle)
        ).apply(instance, (activation, multiTargets, duration, delay, amount, spread, offset, particle) -> new ParticleEmitterAction(type, activation, multiTargets, duration, delay, amount, spread, offset, particle)));
    }
    
    public static ParticleEmitterAction make(String activation, String multiTargets, DynamicCtxVar<Integer> duration, DynamicCtxVar<Integer> delay, DynamicCtxVar<Integer> amount, DynamicCtxVar<Double> spread, DynamicCtxVar<Vec3> offset, ParticleOptions particle)
    {
        return new ParticleEmitterAction(SpellActionTypes.PARTICLE_EMITTER.get(), activation, multiTargets, duration, delay, amount, spread, offset, particle);
    }
    
    protected DynamicCtxVar<Integer> duration;
    protected DynamicCtxVar<Integer> delay;
    protected DynamicCtxVar<Integer> amount;
    protected DynamicCtxVar<Double> spread;
    protected DynamicCtxVar<Vec3> offset;
    protected ParticleOptions particle;
    
    public ParticleEmitterAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ParticleEmitterAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<Integer> duration, DynamicCtxVar<Integer> delay, DynamicCtxVar<Integer> amount, DynamicCtxVar<Double> spread, DynamicCtxVar<Vec3> offset, ParticleOptions particle)
    {
        super(type, activation, multiTargets);
        this.duration = duration;
        this.delay = delay;
        this.amount = amount;
        this.spread = spread;
        this.offset = offset;
        this.particle = particle;
    }
    
    public DynamicCtxVar<Integer> getDuration()
    {
        return duration;
    }
    
    public DynamicCtxVar<Integer> getDelay()
    {
        return delay;
    }
    
    public DynamicCtxVar<Integer> getAmount()
    {
        return amount;
    }
    
    public DynamicCtxVar<Double> getSpread()
    {
        return spread;
    }
    
    public DynamicCtxVar<Vec3> getOffset()
    {
        return offset;
    }
    
    public ParticleOptions getParticle()
    {
        return particle;
    }
    
    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, EntityTarget entityTarget)
    {
        duration.getValue(ctx).ifPresent(duration ->
        {
            delay.getValue(ctx).ifPresent(delay ->
            {
                amount.getValue(ctx).ifPresent(count ->
                {
                    spread.getValue(ctx).ifPresent(spread ->
                    {
                        offset.getValue(ctx).ifPresent(offset ->
                        {
                            ParticleEmitterHolder.getHolder(entityTarget.getEntity()).ifPresent(holder ->
                            {
                                holder.addParticleEmitter(new ParticleEmitterHolder.ParticleEmitter(duration, delay, count, spread, offset, particle));
                            });
                        });
                    });
                });
            });
        });
    }
}
