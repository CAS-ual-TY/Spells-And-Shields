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
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("amount")).forGetter(TryConsumeItemAction::getAmount),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("success")).forGetter(TryConsumeItemAction::getSuccess)
        ).apply(instance, (activation, singleTarget, amount, success) -> new TryConsumeItemAction(type, activation, singleTarget, amount, success)));
    }
    
    public static TryConsumeItemAction make(String activation, String singleTarget, DynamicCtxVar<Integer> damage, String success)
    {
        return new TryConsumeItemAction(SpellActionTypes.TRY_CONSUME_ITEM.get(), activation, singleTarget, damage, success);
    }
    
    protected DynamicCtxVar<Integer> amount;
    protected String success;
    
    public TryConsumeItemAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public TryConsumeItemAction(SpellActionType<?> type, String activation, String singleTarget, DynamicCtxVar<Integer> amount, String success)
    {
        super(type, activation, singleTarget);
        this.amount = amount;
        this.success = success;
    }
    
    public DynamicCtxVar<Integer> getAmount()
    {
        return amount;
    }
    
    public String getSuccess()
    {
        return success;
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, ItemTarget itemTarget)
    {
        amount.getValue(ctx).ifPresent(amount ->
        {
            if(itemTarget.getItem().getCount() < amount)
            {
                return;
            }
            
            ctx.activate(success);
            
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
