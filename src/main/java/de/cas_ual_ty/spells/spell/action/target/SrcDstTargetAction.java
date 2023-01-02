package de.cas_ual_ty.spells.spell.action.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.util.ParamNames;

public abstract class SrcDstTargetAction extends DstTargetAction
{
    public static <T extends SrcDstTargetAction> RecordCodecBuilder<T, String> srcCodec()
    {
        return Codec.STRING.fieldOf(ParamNames.multiTarget("source")).forGetter(SrcDstTargetAction::getSrc);
    }
    
    protected String src;
    
    public SrcDstTargetAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public SrcDstTargetAction(SpellActionType<?> type, String activation, String dst, String src)
    {
        super(type, activation, dst);
        this.src = src;
    }
    
    public String getSrc()
    {
        return src;
    }
    
    @Override
    public void findTargets(SpellContext ctx, TargetGroup destination)
    {
        TargetGroup source = ctx.getTargetGroup(src);
        
        if(source != null)
        {
            findTargets(ctx, source, destination);
        }
    }
    
    public abstract void findTargets(SpellContext ctx, TargetGroup source, TargetGroup destination);
}
