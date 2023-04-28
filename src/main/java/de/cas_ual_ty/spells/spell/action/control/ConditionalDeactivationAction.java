package de.cas_ual_ty.spells.spell.action.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;

public class ConditionalDeactivationAction extends SpellAction
{
    public static Codec<ConditionalDeactivationAction> makeCodec(SpellActionType<ConditionalDeactivationAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("condition")).forGetter(ConditionalDeactivationAction::getCondition)
        ).apply(instance, (activation, condition) -> new ConditionalDeactivationAction(type, activation, condition)));
    }
    
    public static ConditionalDeactivationAction make(Object activation, DynamicCtxVar<Boolean> condition)
    {
        return new ConditionalDeactivationAction(SpellActionTypes.CONDITIONAL_DEACTIVATION.get(), activation.toString(), condition);
    }
    
    protected DynamicCtxVar<Boolean> condition;
    
    public ConditionalDeactivationAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ConditionalDeactivationAction(SpellActionType<?> type, String activation, DynamicCtxVar<Boolean> condition)
    {
        super(type, activation);
        this.condition = condition;
    }
    
    public DynamicCtxVar<Boolean> getCondition()
    {
        return condition;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        condition.getValue(ctx).ifPresent(condition ->
        {
            if(!condition)
            {
                ctx.deactivate(activation);
            }
        });
    }
}
