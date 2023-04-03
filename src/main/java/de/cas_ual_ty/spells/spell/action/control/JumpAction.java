package de.cas_ual_ty.spells.spell.action.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;

public class JumpAction extends SpellAction
{
    public static Codec<JumpAction> makeCodec(SpellActionType<JumpAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                Codec.STRING.fieldOf("label").forGetter(JumpAction::getLabel)
        ).apply(instance, (activation, label) -> new JumpAction(type, activation, label)));
    }
    
    public static JumpAction make(String activation, String label)
    {
        return new JumpAction(SpellActionTypes.JUMP.get(), activation, label);
    }
    
    protected String label;
    
    public JumpAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public JumpAction(SpellActionType<?> type, String activation, String label)
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
        ctx.jumpToLabel(label);
    }
}
