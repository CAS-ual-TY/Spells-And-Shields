package de.cas_ual_ty.spells.spell.action.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;

public class ActivateAction extends SpellAction
{
    public static Codec<ActivateAction> makeCodec(SpellActionType<ActivateAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                Codec.STRING.fieldOf("toActivate").forGetter(ActivateAction::getToActivate)
        ).apply(instance, (activation, toActivate) -> new ActivateAction(type, activation, toActivate)));
    }
    
    protected String toActivate;
    
    public ActivateAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ActivateAction(SpellActionType<?> type, String activation, String toActivate)
    {
        super(type, activation);
        this.toActivate = toActivate;
    }
    
    public String getToActivate()
    {
        return toActivate;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        ctx.activate(toActivate);
    }
}
