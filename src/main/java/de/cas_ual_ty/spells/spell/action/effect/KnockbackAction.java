package de.cas_ual_ty.spells.spell.action.effect;

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
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;

public class KnockbackAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<KnockbackAction> makeCodec(SpellActionType<KnockbackAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDouble("strength")).forGetter(KnockbackAction::getStrength),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDouble("dx")).forGetter(KnockbackAction::getDx),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDouble("dz")).forGetter(KnockbackAction::getDz)
        ).apply(instance, (activation, multiTargets, strength, dx, dz) -> new KnockbackAction(type, activation, multiTargets, strength, dx, dz)));
    }
    
    public static KnockbackAction make(Object activation, Object multiTargets, DynamicCtxVar<Double> strength, DynamicCtxVar<Double> dx, DynamicCtxVar<Double> dz)
    {
        return new KnockbackAction(SpellActionTypes.KNOCKBACK.get(), activation.toString(), multiTargets.toString(), strength, dx, dz);
    }
    
    protected DynamicCtxVar<Double> strength;
    protected DynamicCtxVar<Double> dx;
    protected DynamicCtxVar<Double> dz;
    
    public KnockbackAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public KnockbackAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<Double> strength, DynamicCtxVar<Double> dx, DynamicCtxVar<Double> dz)
    {
        super(type, activation, multiTargets);
        this.strength = strength;
        this.dx = dx;
        this.dz = dz;
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
    
    public DynamicCtxVar<Double> getStrength()
    {
        return strength;
    }
    
    public DynamicCtxVar<Double> getDx()
    {
        return dx;
    }
    
    public DynamicCtxVar<Double> getDz()
    {
        return dz;
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget target)
    {
        strength.getValue(ctx).ifPresent(strength ->
        {
            dx.getValue(ctx).ifPresent(dx ->
            {
                dz.getValue(ctx).ifPresent(dz ->
                {
                    target.getLivingEntity().knockback(strength, dx, dz);
                });
            });
        });
    }
}
