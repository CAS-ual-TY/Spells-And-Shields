package de.cas_ual_ty.spells.spell.action.mana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;

public class HasManaAction extends AffectSingleTypeAction<PlayerTarget>
{
    public static Codec<HasManaAction> makeCodec(SpellActionType<HasManaAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDouble("amount")).forGetter(HasManaAction::getAmount)
        ).apply(instance, (activation, singleTarget, amount) -> new HasManaAction(type, activation, singleTarget, amount)));
    }
    
    public static HasManaAction make(Object activation, Object singleTarget, DynamicCtxVar<Double> amount)
    {
        return new HasManaAction(SpellActionTypes.HAS_MANA.get(), activation.toString(), singleTarget.toString(), amount);
    }
    
    protected DynamicCtxVar<Double> amount;
    
    public HasManaAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public HasManaAction(SpellActionType<?> type, String activation, String singleTarget, DynamicCtxVar<Double> amount)
    {
        super(type, activation, singleTarget);
        this.amount = amount;
    }
    
    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }
    
    public DynamicCtxVar<Double> getAmount()
    {
        return amount;
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, PlayerTarget target)
    {
        if(!target.getPlayer().isCreative())
        {
            amount.getValue(ctx).ifPresent(amount ->
            {
                ManaHolder.getManaHolder(target.getPlayer()).ifPresent(manaHolder ->
                {
                    if(manaHolder.getUsableMana() < amount)
                    {
                        ctx.deactivate(activation);
                    }
                });
            });
        }
    }
}
