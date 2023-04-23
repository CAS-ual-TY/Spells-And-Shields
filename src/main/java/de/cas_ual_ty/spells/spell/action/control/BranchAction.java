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

public class BranchAction extends SpellAction
{
    public static Codec<BranchAction> makeCodec(SpellActionType<BranchAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                Codec.STRING.fieldOf("label").forGetter(BranchAction::getLabel),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("conditional")).forGetter(BranchAction::getConditional)
        ).apply(instance, (activation, label, conditional) -> new BranchAction(type, activation, label, conditional)));
    }
    
    public static BranchAction make(Object activation, String label, DynamicCtxVar<Boolean> conditional)
    {
        return new BranchAction(SpellActionTypes.BRANCH.get(), activation.toString(), label, conditional);
    }
    
    protected String label;
    protected DynamicCtxVar<Boolean> conditional;
    
    public BranchAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public BranchAction(SpellActionType<?> type, String activation, String label, DynamicCtxVar<Boolean> conditional)
    {
        super(type, activation);
        this.label = label;
        this.conditional = conditional;
    }
    
    public String getLabel()
    {
        return label;
    }
    
    public DynamicCtxVar<Boolean> getConditional()
    {
        return conditional;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        if(conditional.getValue(ctx).orElse(false))
        {
            ctx.jumpToLabel(label);
        }
    }
}
