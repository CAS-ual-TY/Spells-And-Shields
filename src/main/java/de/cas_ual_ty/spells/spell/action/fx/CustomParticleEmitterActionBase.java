package de.cas_ual_ty.spells.spell.action.fx;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.client.particle.CustomParticleAttachMode;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;

import java.util.LinkedList;
import java.util.List;

/**
 * Shared fields/codec helpers for {@link CustomParticleEmitterMotionAction}/{@link CustomParticleEmitterPositionAction}.
 * {@link #color}/{@link #alpha}/{@link #initialize} are raw DSL formula strings compiled and evaluated
 * client-side, never server-side; {@link #capturedVariables} are resolved once at cast time instead.
 */
public abstract class CustomParticleEmitterActionBase extends AffectTypeAction<EntityTarget>
{
    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, DynamicCtxVar<Integer>> countCodec()
    {
        return CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("count")).forGetter(CustomParticleEmitterActionBase::getCount);
    }

    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, DynamicCtxVar<Integer>> totalLifetimeCodec()
    {
        return CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("total_lifetime")).forGetter(CustomParticleEmitterActionBase::getTotalLifetime);
    }

    /** {@code <= 0} means one-shot. */
    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, DynamicCtxVar<Integer>> periodCodec()
    {
        return CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("period")).forGetter(CustomParticleEmitterActionBase::getPeriod);
    }

    /** Ticks before the first batch spawns; {@code <= 0} means immediately. */
    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, DynamicCtxVar<Integer>> delayCodec()
    {
        return CtxVarTypes.INT.get().refCodec().optionalFieldOf(ParamNames.paramInt("delay"), CtxVarTypes.INT.get().immediate(0)).forGetter(CustomParticleEmitterActionBase::getDelay);
    }

    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, CustomParticleAttachMode> positionAttachModeCodec()
    {
        return CustomParticleAttachMode.CODEC.fieldOf("position_attach_mode").forGetter(CustomParticleEmitterActionBase::getPositionAttachMode);
    }

    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, CustomParticleAttachMode> rotationAttachModeCodec()
    {
        return CustomParticleAttachMode.CODEC.fieldOf("rotation_attach_mode").forGetter(CustomParticleEmitterActionBase::getRotationAttachMode);
    }

    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, List<String>> capturedVariablesCodec()
    {
        return Codec.STRING.listOf().optionalFieldOf("captured_variables", new LinkedList<>()).forGetter(CustomParticleEmitterActionBase::getCapturedVariables);
    }

    /** {@code key}: callers pick their own {@code p1}/{@code p2}/... slot, field count differs per subclass. */
    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, String> colorCodec(String key)
    {
        return Codec.STRING.fieldOf(key).forGetter(CustomParticleEmitterActionBase::getColor);
    }

    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, String> alphaCodec(String key)
    {
        return Codec.STRING.fieldOf(key).forGetter(CustomParticleEmitterActionBase::getAlpha);
    }

    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, List<CustomParticleInitEntry>> initializeCodec()
    {
        return CustomParticleInitEntry.CODEC.listOf().optionalFieldOf("initialize", new LinkedList<>()).forGetter(CustomParticleEmitterActionBase::getInitialize);
    }

    /** Empty means no per-particle limit. */
    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, String> particleLifetimeCodec(String key)
    {
        return Codec.STRING.optionalFieldOf(key, "").forGetter(CustomParticleEmitterActionBase::getParticleLifetime);
    }

    protected DynamicCtxVar<Integer> count;
    protected DynamicCtxVar<Integer> totalLifetime;
    protected DynamicCtxVar<Integer> period;
    protected DynamicCtxVar<Integer> delay;
    protected CustomParticleAttachMode positionAttachMode;
    protected CustomParticleAttachMode rotationAttachMode;
    protected List<String> capturedVariables;
    protected String color;
    protected String alpha;
    protected String particleLifetime;
    protected List<CustomParticleInitEntry> initialize;

    public CustomParticleEmitterActionBase(SpellActionType<?> type)
    {
        super(type);
    }

    public CustomParticleEmitterActionBase(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<Integer> count, DynamicCtxVar<Integer> totalLifetime, DynamicCtxVar<Integer> period, DynamicCtxVar<Integer> delay, CustomParticleAttachMode positionAttachMode, CustomParticleAttachMode rotationAttachMode, List<String> capturedVariables, String color, String alpha, String particleLifetime, List<CustomParticleInitEntry> initialize)
    {
        super(type, activation, multiTargets);
        this.count = count;
        this.totalLifetime = totalLifetime;
        this.period = period;
        this.delay = delay;
        this.positionAttachMode = positionAttachMode;
        this.rotationAttachMode = rotationAttachMode;
        this.capturedVariables = capturedVariables;
        this.color = color;
        this.alpha = alpha;
        this.particleLifetime = particleLifetime;
        this.initialize = initialize;
    }

    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }

    public DynamicCtxVar<Integer> getCount()
    {
        return count;
    }

    public DynamicCtxVar<Integer> getTotalLifetime()
    {
        return totalLifetime;
    }

    public DynamicCtxVar<Integer> getPeriod()
    {
        return period;
    }

    public DynamicCtxVar<Integer> getDelay()
    {
        return delay;
    }

    public CustomParticleAttachMode getPositionAttachMode()
    {
        return positionAttachMode;
    }

    public CustomParticleAttachMode getRotationAttachMode()
    {
        return rotationAttachMode;
    }

    public List<String> getCapturedVariables()
    {
        return capturedVariables;
    }

    public String getColor()
    {
        return color;
    }

    public String getAlpha()
    {
        return alpha;
    }

    public String getParticleLifetime()
    {
        return particleLifetime;
    }

    public List<CustomParticleInitEntry> getInitialize()
    {
        return initialize;
    }

    protected List<CtxVar<?>> captureVariables(SpellContext ctx)
    {
        List<CtxVar<?>> captured = new LinkedList<>();

        for(String name : capturedVariables)
        {
            CtxVar<?> var = ctx.getCtxVar(name);

            if(var != null)
            {
                captured.add(var);
            }
        }

        return captured;
    }
}
