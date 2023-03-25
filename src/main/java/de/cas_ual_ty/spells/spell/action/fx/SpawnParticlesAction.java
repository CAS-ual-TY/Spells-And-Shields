package de.cas_ual_ty.spells.spell.action.fx;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PositionTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

public class SpawnParticlesAction extends AffectTypeAction<PositionTarget>
{
    public static Codec<SpawnParticlesAction> makeCodec(SpellActionType<SpawnParticlesAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                ParticleTypes.CODEC.fieldOf("particle").forGetter(SpawnParticlesAction::getParticle),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("count")).forGetter(SpawnParticlesAction::getCount),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDouble("spread")).forGetter(SpawnParticlesAction::getSpread)
        ).apply(instance, (activation, multiTargets, particle, count, spread) -> new SpawnParticlesAction(type, activation, multiTargets, particle, count, spread)));
    }
    
    public static SpawnParticlesAction make(String activation, String multiTargets, ParticleOptions particle, DynamicCtxVar<Integer> count, DynamicCtxVar<Double> spread)
    {
        return new SpawnParticlesAction(SpellActionTypes.SPAWN_PARTICLES.get(), activation, multiTargets, particle, count, spread);
    }
    
    protected ParticleOptions particle;
    protected DynamicCtxVar<Integer> count;
    protected DynamicCtxVar<Double> spread;
    
    public SpawnParticlesAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public SpawnParticlesAction(SpellActionType<?> type, String activation, String multiTargets, ParticleOptions particle, DynamicCtxVar<Integer> count, DynamicCtxVar<Double> spread)
    {
        super(type, activation, multiTargets);
        this.particle = particle;
        this.count = count;
        this.spread = spread;
    }
    
    public ParticleOptions getParticle()
    {
        return particle;
    }
    
    public DynamicCtxVar<Integer> getCount()
    {
        return count;
    }
    
    public DynamicCtxVar<Double> getSpread()
    {
        return spread;
    }
    
    @Override
    public ITargetType<PositionTarget> getAffectedType()
    {
        return TargetTypes.POSITION.get();
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, PositionTarget positionTarget)
    {
        count.getValue(ctx).ifPresent(count ->
        {
            spread.getValue(ctx).ifPresent(spread ->
            {
                if(positionTarget.getLevel() instanceof ServerLevel level)
                {
                    level.sendParticles(
                            particle,
                            positionTarget.getPosition().x(),
                            positionTarget.getPosition().y(),
                            positionTarget.getPosition().z(),
                            count,
                            SpellsUtil.RANDOM.nextGaussian() * spread,
                            SpellsUtil.RANDOM.nextGaussian() * spread,
                            SpellsUtil.RANDOM.nextGaussian() * spread,
                            0
                    );
                }
            });
        });
    }
}
