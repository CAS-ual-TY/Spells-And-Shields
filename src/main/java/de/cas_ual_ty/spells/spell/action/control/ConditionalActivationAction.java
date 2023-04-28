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

public class ConditionalActivationAction extends SpellAction
{
    public static Codec<ConditionalActivationAction> makeCodec(SpellActionType<ConditionalActivationAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("to_activate")).forGetter(ConditionalActivationAction::getToActivate),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("condition")).forGetter(ConditionalActivationAction::getCondition)
        ).apply(instance, (activation, toActivate, condition) -> new ConditionalActivationAction(type, activation, toActivate, condition)));
    }
    
    public static ConditionalActivationAction make(Object activation, Object toActivate, DynamicCtxVar<Boolean> condition)
    {
        return new ConditionalActivationAction(SpellActionTypes.CONDITIONAL_ACTIVATION.get(), activation.toString(), toActivate.toString(), condition);
    }
    
    protected String toActivate;
    protected DynamicCtxVar<Boolean> condition;
    
    public ConditionalActivationAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ConditionalActivationAction(SpellActionType<?> type, String activation, String toActivate, DynamicCtxVar<Boolean> condition)
    {
        super(type, activation);
        this.toActivate = toActivate;
        this.condition = condition;
    }
    
    public String getToActivate()
    {
        return toActivate;
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
            if(condition)
            {
                ctx.activate(toActivate);
            }
        });
    }
}
