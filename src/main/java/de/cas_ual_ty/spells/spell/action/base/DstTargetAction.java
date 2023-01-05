package de.cas_ual_ty.spells.spell.action.base;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.util.ParamNames;

public abstract class DstTargetAction extends SpellAction
{
    public static <T extends DstTargetAction> RecordCodecBuilder<T, String> dstCodec()
    {
        return Codec.STRING.fieldOf(ParamNames.destinationTarget("destination")).forGetter(DstTargetAction::getDst);
    }
    
    protected String dst;
    
    public DstTargetAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public DstTargetAction(SpellActionType<?> type, String activation, String dst)
    {
        super(type, activation);
        this.dst = dst;
    }
    
    public String getDst()
    {
        return dst;
    }
    
    @Override
    public void wasActivated(SpellContext ctx)
    {
        findTargets(ctx, ctx.getOrCreateTargetGroup(dst));
    }
    
    public abstract void findTargets(SpellContext ctx, TargetGroup destination);
}
