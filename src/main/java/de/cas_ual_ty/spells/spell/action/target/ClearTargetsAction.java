package de.cas_ual_ty.spells.spell.action.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;

public class ClearTargetsAction extends DstTargetAction
{
    public static Codec<ClearTargetsAction> makeCodec(SpellActionType<ClearTargetsAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                dstCodec()
        ).apply(instance, (activation, dst) -> new ClearTargetsAction(type, activation, dst)));
    }
    
    public ClearTargetsAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ClearTargetsAction(SpellActionType<?> type, String activation, String dst)
    {
        super(type, activation, dst);
    }
    
    @Override
    public void findTargets(SpellContext ctx, TargetGroup destination)
    {
        destination.clear();
    }
}
