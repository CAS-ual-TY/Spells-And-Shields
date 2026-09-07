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

    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, DynamicCtxVar<Integer>> totalLifetimeCodec()
    {
        return CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("total_lifetime")).forGetter(CustomParticleEmitterActionBase::getTotalLifetime);
    }

    /**
     * {@code <= 0} means one-shot (spawn once, no repeats) - see {@code CustomParticleEmitterInstance#period}.
     */
    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, DynamicCtxVar<Integer>> periodCodec()
    {
        return CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("period")).forGetter(CustomParticleEmitterActionBase::getPeriod);
    }

    /**
     * Ticks to wait before the first batch spawns - subsequent repeat batches (if {@code period > 0}) are offset
     * by this too, so they land on {@code delay, delay + period, delay + 2*period, ...} rather than
     * {@code 0, period, 2*period, ...}. {@code <= 0} means no delay (spawn immediately at cast time), matching
     * every emitter's behavior before this field existed - see {@code CustomParticleEmitterInstance#delay}.
     */
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

    /**
     * {@code key} is the JSON field name to use - callers pick their own {@code p1}/{@code p2}/... slot since
     * {@link CustomParticleEmitterMotionAction} and {@link CustomParticleEmitterPositionAction} have different
     * numbers of per-particle expression fields ahead of this one.
     */
    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, String> colorCodec(String key)
    {
        return Codec.STRING.fieldOf(key).forGetter(CustomParticleEmitterActionBase::getColor);
    }

    /**
     * @see #colorCodec(String)
     */
    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, String> alphaCodec(String key)
    {
        return Codec.STRING.fieldOf(key).forGetter(CustomParticleEmitterActionBase::getAlpha);
    }

    /**
     * Each entry is a named ctx var evaluated once per particle at spawn and then kept on that particle - see
     * {@link CustomParticleInitEntry}. Optional, defaults to an empty list.
     */
    public static <T extends CustomParticleEmitterActionBase> RecordCodecBuilder<T, List<CustomParticleInitEntry>> initializeCodec()
    {
        return CustomParticleInitEntry.CODEC.listOf().optionalFieldOf("initialize", new LinkedList<>()).forGetter(CustomParticleEmitterActionBase::getInitialize);
    }

    /**
     * Raw DSL formula string (INT), compiled/evaluated client-side exactly once per particle at spawn (same
     * timing as {@code initialize} entries) - the result becomes that particle's own {@code maxAge}; once its own
     * {@code age} reaches it, that single particle is removed, independent of the emitter's own
     * {@link #totalLifetime} (which just controls when the emitter stops spawning new batches/expires entirely -
     * see {@code CustomParticleEmitterInstance}/{@code CustomParticleManager}). Optional, empty means no
     * per-particle limit (every particle lives until the emitter itself expires, same as before this field
     * existed).
     *
     * @see #colorCodec(String)
     */
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
