package de.cas_ual_ty.spells.spell.action.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.util.ParamNames;

public class DeactivateAction extends SpellAction
{
    public static Codec<DeactivateAction> makeCodec(SpellActionType<DeactivateAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("to_deactivate")).forGetter(DeactivateAction::getToDeactivate)
        ).apply(instance, (activation, toActivate) -> new DeactivateAction(type, activation, toActivate)));
    }
    
    protected String toDeactivate;
    
    public DeactivateAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public DeactivateAction(SpellActionType<?> type, String activation, String toDeactivate)
    {
        super(type, activation);
        this.toDeactivate = toDeactivate;
    }
    
    public String getToDeactivate()
    {
        return toDeactivate;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        ctx.deactivate(toDeactivate);
    }
}
