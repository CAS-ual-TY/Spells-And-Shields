package de.cas_ual_ty.spells.spell.action.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.ItemTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;

public class TryConsumeItemAction extends AffectSingleTypeAction<ItemTarget>
{
    public static Codec<TryConsumeItemAction> makeCodec(SpellActionType<TryConsumeItemAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("amount")).forGetter(TryConsumeItemAction::getAmount)
        ).apply(instance, (activation, singleTarget, amount) -> new TryConsumeItemAction(type, activation, singleTarget, amount)));
    }
    
    public static TryConsumeItemAction make(String activation, String singleTarget, DynamicCtxVar<Integer> damage)
    {
        return new TryConsumeItemAction(SpellActionTypes.TRY_CONSUME_ITEM.get(), activation, singleTarget, damage);
    }
    
    protected DynamicCtxVar<Integer> amount;
    
    public TryConsumeItemAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public TryConsumeItemAction(SpellActionType<?> type, String activation, String singleTarget, DynamicCtxVar<Integer> amount)
    {
        super(type, activation, singleTarget);
        this.amount = amount;
    }
    
    public DynamicCtxVar<Integer> getAmount()
    {
        return amount;
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, ItemTarget itemTarget)
    {
        amount.getValue(ctx).ifPresent(amount ->
        {
            if(itemTarget.getItem().getCount() < amount)
            {
                ctx.deactivate(activation);
                return;
            }
            
            if(!itemTarget.isCreative())
            {
                itemTarget.getItem().shrink(amount);
            }
        });
    }
    
    @Override
    public ITargetType<ItemTarget> getAffectedType()
    {
        return TargetTypes.ITEM.get();
    }
}
