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

public class ManaCheckAction extends AffectSingleTypeAction<PlayerTarget>
{
    public static Codec<ManaCheckAction> makeCodec(SpellActionType<ManaCheckAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDouble("amount")).forGetter(ManaCheckAction::getAmount)
        ).apply(instance, (activation, singleTarget, amount) -> new ManaCheckAction(type, activation, singleTarget, amount)));
    }
    
    public static ManaCheckAction make(Object activation, Object singleTarget, DynamicCtxVar<Double> amount)
    {
        return new ManaCheckAction(SpellActionTypes.MANA_CHECK.get(), activation.toString(), singleTarget.toString(), amount);
    }
    
    protected DynamicCtxVar<Double> amount;
    
    public ManaCheckAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ManaCheckAction(SpellActionType<?> type, String activation, String singleTarget, DynamicCtxVar<Double> amount)
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
                    if(manaHolder.getUsableMana() >= amount)
                    {
                        manaHolder.burn(amount.floatValue());
                    }
                    else
                    {
                        ctx.deactivate(activation);
                    }
                });
            });
        }
    }
}
