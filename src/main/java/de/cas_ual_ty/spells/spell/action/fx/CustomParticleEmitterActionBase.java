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
 * Shared fields/codec helpers/cast-time resolution for both {@link CustomParticleEmitterMotionAction} (initial
 * position + per-tick motion) and {@link CustomParticleEmitterPositionAction} (position recomputed fresh every
 * tick, no motion) - see {@code CustomParticleEmitterInstance} for what these become client-side. One emitter is
 * spawned per matched {@link EntityTarget}, attached to that entity per {@link #positionAttachMode}/
 * {@link #rotationAttachMode}.
 * <p>
 * {@link #color}/{@link #alpha} are raw DSL formula strings, not {@link DynamicCtxVar} fields like every other
 * dynamic field in this mod - they're never evaluated server-side. They're forwarded to the client verbatim and
 * compiled/evaluated there, once per particle per tick, against {@code index}/{@code age} (see
 * {@code CustomParticleContext}) - that per-tick, per-particle re-evaluation is the whole reason this action
 * exists rather than the older, vanilla-particle-based {@code SpawnParticlesAction}.
 * <p>
 * {@link #capturedVariables}, by contrast, are resolved exactly once, here, server-side, at cast time (see
 * {@link #captureVariables(SpellContext)}) - named existing {@link SpellContext} variables that get baked to
 * plain values and sent alongside the formulas, per the mod's rule that particles never get live access to
 * server-only spell state. Names not currently set on the caster's context are silently skipped.
 * <p>
 * {@link #initialize} entries are, like {@link #color}/{@link #alpha}, raw DSL formula strings compiled
 * client-side - but evaluated only ONCE per particle, at spawn, and the result kept on that specific particle
 * (see {@code CustomParticleInitEntry}/{@code CustomParticleInitVar}) for every later per-tick formula to
 * reference by name - permanent per-particle state, distinct from the always-changing {@code index}/{@code age}
 * and from the emitter-wide {@link #capturedVariables}.
 */
public abstract class CustomParticleEmitterActionBase extends AffectTypeAction<EntityTarget>
{
    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, DynamicCtxVar<Integer>> countCodec()
    {
        return CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("count")).forGetter(CustomParticleEmitterActionBase::getCount);
    }

    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, DynamicCtxVar<Integer>> durationCodec()
    {
        return CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("duration")).forGetter(CustomParticleEmitterActionBase::getDuration);
    }

    /**
     * {@code <= 0} means one-shot (spawn once, no repeats) - see {@code CustomParticleEmitterInstance#period}.
     */
    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, DynamicCtxVar<Integer>> periodCodec()
    {
        return CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("period")).forGetter(CustomParticleEmitterActionBase::getPeriod);
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

    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, String> colorCodec()
    {
        return Codec.STRING.fieldOf("color").forGetter(CustomParticleEmitterActionBase::getColor);
    }

    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, String> alphaCodec()
    {
        return Codec.STRING.fieldOf("alpha").forGetter(CustomParticleEmitterActionBase::getAlpha);
    }

    /**
     * Each entry is a named ctx var evaluated once per particle at spawn and then kept on that particle - see
     * {@link CustomParticleInitEntry}. Optional, defaults to an empty list.
     */
    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, List<CustomParticleInitEntry>> initializeCodec()
    {
        return CustomParticleInitEntry.CODEC.listOf().optionalFieldOf("initialize", new LinkedList<>()).forGetter(CustomParticleEmitterActionBase::getInitialize);
    }

    protected DynamicCtxVar<Integer> count;
    protected DynamicCtxVar<Integer> duration;
    protected DynamicCtxVar<Integer> period;
    protected CustomParticleAttachMode positionAttachMode;
    protected CustomParticleAttachMode rotationAttachMode;
    protected List<String> capturedVariables;
    protected String color;
    protected String alpha;
    protected List<CustomParticleInitEntry> initialize;

    public CustomParticleEmitterActionBase(SpellActionType<?> type)
    {
        super(type);
    }

    public CustomParticleEmitterActionBase(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<Integer> count, DynamicCtxVar<Integer> duration, DynamicCtxVar<Integer> period, CustomParticleAttachMode positionAttachMode, CustomParticleAttachMode rotationAttachMode, List<String> capturedVariables, String color, String alpha, List<CustomParticleInitEntry> initialize)
    {
        super(type, activation, multiTargets);
        this.count = count;
        this.duration = duration;
        this.period = period;
        this.positionAttachMode = positionAttachMode;
        this.rotationAttachMode = rotationAttachMode;
        this.capturedVariables = capturedVariables;
        this.color = color;
        this.alpha = alpha;
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

    public DynamicCtxVar<Integer> getDuration()
    {
        return duration;
    }

    public DynamicCtxVar<Integer> getPeriod()
    {
        return period;
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
