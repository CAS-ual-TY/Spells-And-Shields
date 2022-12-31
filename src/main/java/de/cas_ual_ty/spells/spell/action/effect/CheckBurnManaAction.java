package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.BuiltinActivations;
import de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;

public class CheckBurnManaAction extends AffectSingleTypeAction<PlayerTarget>
{
    public static Codec<CheckBurnManaAction> makeCodec(SpellActionType<CheckBurnManaAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                SpellAction.activationCodec(),
                AffectTypeAction.targetsCodec(),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf("mana_amount").forGetter(CheckBurnManaAction::getAmount)
        ).apply(instance, (activation, targets, amount) -> new CheckBurnManaAction(type, activation, targets, amount)));
    }
    
    protected DynamicCtxVar<Double> amount;
    
    public CheckBurnManaAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public CheckBurnManaAction(SpellActionType<?> type, String activation, String targets, DynamicCtxVar<Double> amount)
    {
        super(type, activation, targets);
        this.amount = amount;
    }
    
    public CheckBurnManaAction(SpellActionType<?> type, BuiltinActivations activation, BuiltinTargetGroups targets, double amount)
    {
        this(type, activation.activation, targets.targetGroup, CtxVarTypes.DOUBLE.get().refImm(amount));
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
