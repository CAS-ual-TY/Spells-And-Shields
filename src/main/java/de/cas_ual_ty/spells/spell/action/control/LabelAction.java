package de.cas_ual_ty.spells.spell.action.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;

public class LabelAction extends SpellAction
{
    public static Codec<LabelAction> makeCodec(SpellActionType<LabelAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                Codec.STRING.fieldOf("label").forGetter(LabelAction::getLabel)
        ).apply(instance, (activation, label) -> new LabelAction(type, activation, label)));
    }
    
    public static LabelAction make(Object activation, String label)
    {
        return new LabelAction(SpellActionTypes.LABEL.get(), activation.toString(), label);
    }
    
    protected String label;
    
    public LabelAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public LabelAction(SpellActionType<?> type, String activation, String label)
    {
        super(type, activation);
        this.label = label;
    }
    
    public String getLabel()
    {
        return label;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        ctx.addLabel(label, this);
    }
}
