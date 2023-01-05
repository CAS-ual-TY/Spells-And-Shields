package de.cas_ual_ty.spells.spell.action.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;

public class BooleanActivationAction extends SpellAction
{
    public static Codec<BooleanActivationAction> makeCodec(SpellActionType<BooleanActivationAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("to_activate")).forGetter(BooleanActivationAction::getToActivate),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("input")).forGetter(BooleanActivationAction::getOperant),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("activate_if_true")).forGetter(BooleanActivationAction::getActivateIfTrue),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("deactivate_if_false")).forGetter(BooleanActivationAction::getDeactivateIfFalse)
        ).apply(instance, (activation, toActivate, operant, activateIfTrue, deactivateIfFalse) -> new BooleanActivationAction(type, activation, toActivate, operant, activateIfTrue, deactivateIfFalse)));
    }
    
    protected String toActivate;
    protected DynamicCtxVar<Boolean> operant;
    protected DynamicCtxVar<Boolean> activateIfTrue;
    protected DynamicCtxVar<Boolean> deactivateIfFalse;
    
    public BooleanActivationAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public BooleanActivationAction(SpellActionType<?> type, String activation, String toActivate, DynamicCtxVar<Boolean> operant, DynamicCtxVar<Boolean> activateIfTrue, DynamicCtxVar<Boolean> deactivateIfFalse)
    {
        super(type, activation);
        this.toActivate = toActivate;
        this.operant = operant;
        this.activateIfTrue = activateIfTrue;
        this.deactivateIfFalse = deactivateIfFalse;
    }
    
    public String getToActivate()
    {
        return toActivate;
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
                        ctx.activate(toActivate);
                    }
                });
            }
            else
            {
                deactivateIfFalse.getValue(ctx).ifPresent(deactivate ->
                {
                    if(deactivate)
                    {
                        ctx.deactivate(toActivate);
                    }
                });
            }
        });
    }
}
