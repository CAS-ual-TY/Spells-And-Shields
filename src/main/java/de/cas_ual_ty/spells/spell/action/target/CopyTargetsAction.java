package de.cas_ual_ty.spells.spell.action.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;

public class CopyTargetsAction extends SrcDstTargetAction
{
    public static Codec<CopyTargetsAction> makeCodec(SpellActionType<CopyTargetsAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                dstCodec(),
                srcCodec()
        ).apply(instance, (activation, dst, src) -> new CopyTargetsAction(type, activation, dst, src)));
    }
    
    public static CopyTargetsAction make(String activation, String dest, String src)
    {
        return new CopyTargetsAction(SpellActionTypes.COPY_TARGETS.get(), activation, dest, src);
    }
    
    public CopyTargetsAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public CopyTargetsAction(SpellActionType<?> type, String activation, String dest, String src)
    {
        super(type, activation, dest, src);
    }
    
    @Override
    public void findTargets(SpellContext ctx, TargetGroup source, TargetGroup destination)
    {
        destination.addTargets(source.getTargets());
    }
}
