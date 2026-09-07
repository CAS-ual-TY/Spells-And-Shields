package de.cas_ual_ty.spells.client.particle;

import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Evaluation context for the particle DSL formulas - a bare {@link SpellContext} used only as a vehicle for
 * {@link de.cas_ual_ty.spells.spell.compiler.Compiler}, never wrapped in {@code <<...>>} since these formulas
 * are compiled directly via {@code compileString}, not through a JSON field codec.
 */
public class CustomParticleContext
{
    public static final String INDEX_NAME = "index";
    public static final String TOTAL_INDEX_NAME = "total_index";
    public static final String AGE_NAME = "age";
    public static final String MAX_AGE_NAME = "max_age";
    public static final String PARTICLE_MAX_AGE_NAME = "particle_max_age";
    public static final String SOURCE_MOTION_NAME = "source_motion";
    public static final String SOURCE_YAW_NAME = "source_yaw";
    public static final String SOURCE_PITCH_NAME = "source_pitch";

    private final SpellContext ctx;

    public CustomParticleContext(Level level)
    {
        this.ctx = new SpellContext(level, null, null);
    }

    public <T> void capture(CtxVarType<T> type, String name, T value)
    {
        ctx.setCtxVar(type, name, value);
    }

    public void setFrameVars(Vec3 sourceMotion, float sourceYaw, float sourcePitch)
    {
        ctx.setCtxVar(CtxVarTypes.VEC3.get(), SOURCE_MOTION_NAME, sourceMotion);
        ctx.setCtxVar(CtxVarTypes.DOUBLE.get(), SOURCE_YAW_NAME, (double) sourceYaw);
        ctx.setCtxVar(CtxVarTypes.DOUBLE.get(), SOURCE_PITCH_NAME, (double) sourcePitch);
    }

    public <T> Optional<T> evaluate(DynamicCtxVar<T> expression, int index, int totalIndex, int age)
    {
        ctx.setCtxVar(CtxVarTypes.INT.get(), INDEX_NAME, index);
        ctx.setCtxVar(CtxVarTypes.INT.get(), TOTAL_INDEX_NAME, totalIndex);
        ctx.setCtxVar(CtxVarTypes.INT.get(), AGE_NAME, age);
        return expression.getValue(ctx);
    }

    public <T> Optional<T> evaluate(DynamicCtxVar<T> expression, CustomParticleInstance particle)
    {
        ctx.setCtxVar(CtxVarTypes.INT.get(), INDEX_NAME, particle.index);
        ctx.setCtxVar(CtxVarTypes.INT.get(), TOTAL_INDEX_NAME, particle.totalIndex);
        ctx.setCtxVar(CtxVarTypes.INT.get(), AGE_NAME, particle.age);
        ctx.setCtxVar(CtxVarTypes.INT.get(), PARTICLE_MAX_AGE_NAME, particle.maxAge);

        for(CtxVar<?> var : particle.initVars.values())
        {
            setVar(var);
        }

        return expression.getValue(ctx);
    }

    private <T> void setVar(CtxVar<T> var)
    {
        ctx.setCtxVar(var.getType(), var.getName(), var.getValue());
    }
}
