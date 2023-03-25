package de.cas_ual_ty.spells.spell.action.mana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.ManaHolder;
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

public class ReplenishManaAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<ReplenishManaAction> makeCodec(SpellActionType<ReplenishManaAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDouble("mana_amount")).forGetter(ReplenishManaAction::getAmount)
        ).apply(instance, (activation, multiTargets, amount) -> new ReplenishManaAction(type, activation, multiTargets, amount)));
    }
    
    public static ReplenishManaAction make(String activation, String multiTargets, DynamicCtxVar<Double> amount)
    {
        return new ReplenishManaAction(SpellActionTypes.REPLENISH_MANA.get(), activation, multiTargets, amount);
    }
    
    protected DynamicCtxVar<Double> amount;
    
    public ReplenishManaAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ReplenishManaAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<Double> amount)
    {
        super(type, activation, multiTargets);
        this.amount = amount;
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
    
    public DynamicCtxVar<Double> getAmount()
    {
        return amount;
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget target)
    {
        amount.getValue(ctx).ifPresent(amount ->
        {
            ManaHolder.getManaHolder(target.getLivingEntity()).ifPresent(manaHolder ->
            {
                manaHolder.replenish(amount.floatValue());
            });
        });
    }
}
