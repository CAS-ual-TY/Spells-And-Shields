package de.cas_ual_ty.spells.spell.action.item;

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
import de.cas_ual_ty.spells.spell.target.ItemTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;

public class ConsumeItemAction extends AffectTypeAction<ItemTarget>
{
    public static Codec<ConsumeItemAction> makeCodec(SpellActionType<ConsumeItemAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("amount")).forGetter(ConsumeItemAction::getAmount)
        ).apply(instance, (activation, multiTargets, amount) -> new ConsumeItemAction(type, activation, multiTargets, amount)));
    }
    
    public static ConsumeItemAction make(Object activation, Object multiTargets, DynamicCtxVar<Integer> amount)
    {
        return new ConsumeItemAction(SpellActionTypes.CONSUME_ITEM.get(), activation.toString(), multiTargets.toString(), amount);
    }
    
    protected DynamicCtxVar<Integer> amount;
    
    public ConsumeItemAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ConsumeItemAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<Integer> amount)
    {
        super(type, activation, multiTargets);
        this.amount = amount;
    }
    
    public DynamicCtxVar<Integer> getAmount()
    {
        return amount;
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, ItemTarget itemTarget)
    {
        amount.getValue(ctx).ifPresent(amount ->
        {
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
