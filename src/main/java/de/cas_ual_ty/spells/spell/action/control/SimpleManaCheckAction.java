package de.cas_ual_ty.spells.spell.action.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups;
import de.cas_ual_ty.spells.spell.context.BuiltinVariables;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;

public class SimpleManaCheckAction extends AffectSingleTypeAction<PlayerTarget>
{
    public static Codec<SimpleManaCheckAction> makeCodec(SpellActionType<SimpleManaCheckAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                SpellAction.activationCodec()
        ).apply(instance, (activation) -> new SimpleManaCheckAction(type, activation)));
    }
    
    public static SimpleManaCheckAction make(String activation)
    {
        return new SimpleManaCheckAction(SpellActionTypes.SIMPLE_MANA_CHECK.get(), activation);
    }
    
    protected DynamicCtxVar<Double> amount;
    
    public SimpleManaCheckAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public SimpleManaCheckAction(SpellActionType<?> type, String activation)
    {
        super(type, activation, BuiltinTargetGroups.OWNER.targetGroup);
        this.amount = CtxVarTypes.DOUBLE.get().reference(BuiltinVariables.MANA_COST.name);
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
                    if(manaHolder.getMana() >= amount)
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
