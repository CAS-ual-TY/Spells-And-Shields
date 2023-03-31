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

public class HealAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<HealAction> makeCodec(SpellActionType<HealAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDouble("healing_amount")).forGetter(HealAction::getHealingAmount)
        ).apply(instance, (activation, multiTargets, healingAmount) -> new HealAction(type, activation, multiTargets, healingAmount)));
    }
    
    public static HealAction make(String activation, String multiTargets, DynamicCtxVar<Double> healingAmount)
    {
        return new HealAction(SpellActionTypes.HEAL.get(), activation, multiTargets, healingAmount);
    }
    
    protected DynamicCtxVar<Double> healingAmount;
    
    public HealAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public HealAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<Double> healingAmount)
    {
        super(type, activation, multiTargets);
        this.healingAmount = healingAmount;
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
    
    public DynamicCtxVar<Double> getHealingAmount()
    {
        return healingAmount;
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget target)
    {
        healingAmount.getValue(ctx).ifPresent(healingAmount ->
        {
            target.getLivingEntity().heal(healingAmount.floatValue());
        });
    }
}
