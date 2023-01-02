package de.cas_ual_ty.spells.spell.action.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;

public class BooleanActivationAction extends SpellAction
{
    public static Codec<BooleanActivationAction> makeCodec(SpellActionType<BooleanActivationAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf("operant").forGetter(BooleanActivationAction::getOperant),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf("activateIfTrue").forGetter(BooleanActivationAction::getActivateIfTrue),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf("deactivateIfFalse").forGetter(BooleanActivationAction::getDeactivateIfFalse)
        ).apply(instance, (activation, operant, activateIfTrue, deactivateIfFalse) -> new BooleanActivationAction(type, activation, operant, activateIfTrue, deactivateIfFalse)));
    }
    
    protected DynamicCtxVar<Boolean> operant;
    protected DynamicCtxVar<Boolean> activateIfTrue;
    protected DynamicCtxVar<Boolean> deactivateIfFalse;
    
    public BooleanActivationAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public BooleanActivationAction(SpellActionType<?> type, String activation, DynamicCtxVar<Boolean> operant, DynamicCtxVar<Boolean> activateIfTrue, DynamicCtxVar<Boolean> deactivateIfFalse)
    {
        super(type, activation);
        this.operant = operant;
        this.activateIfTrue = activateIfTrue;
        this.deactivateIfFalse = deactivateIfFalse;
    }
    
    public DynamicCtxVar<Boolean> getOperant()
    {
        return operant;
    }
    
    public DynamicCtxVar<Boolean> getActivateIfTrue()
    {
        return activateIfTrue;
    }
    
    public DynamicCtxVar<Boolean> getDeactivateIfFalse()
    {
        return deactivateIfFalse;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        operant.getValue(ctx).ifPresent(operant ->
        {
            if(operant)
            {
                activateIfTrue.getValue(ctx).ifPresent(activate ->
                {
                    if(activate)
                    {
                        ctx.activate(activation);
                    }
                });
            }
            else
            {
                deactivateIfFalse.getValue(ctx).ifPresent(deactivate ->
                {
                    if(deactivate)
                    {
                        ctx.deactivate(activation);
                    }
                });
            }
        });
    }
}
