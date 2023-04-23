package de.cas_ual_ty.spells.spell.action.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;

public class TerminateAction extends SpellAction
{
    public static Codec<TerminateAction> makeCodec(SpellActionType<TerminateAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec()
        ).apply(instance, (activation) -> new TerminateAction(type, activation)));
    }
    
    public static TerminateAction make(Object activation)
    {
        return new TerminateAction(SpellActionTypes.TERMINATE.get(), activation.toString());
    }
    
    public TerminateAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public TerminateAction(SpellActionType<?> type, String activation)
    {
        super(type, activation);
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        ctx.terminate();
    }
}
