package de.cas_ual_ty.spells.spell.action.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.SrcDstTargetAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.Target;

import java.util.LinkedList;
import java.util.List;

public class MovePlayerTargetsAction extends SrcDstTargetAction
{
    public static Codec<MovePlayerTargetsAction> makeCodec(SpellActionType<MovePlayerTargetsAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                dstCodec(),
                srcCodec()
        ).apply(instance, (activation, dst, src) -> new MovePlayerTargetsAction(type, activation, dst, src)));
    }
    
    public static MovePlayerTargetsAction make(Object activation, Object dst, Object src)
    {
        return new MovePlayerTargetsAction(SpellActionTypes.MOVE_PLAYER_TARGETS.get(), activation.toString(), dst.toString(), src.toString());
    }
    
    public MovePlayerTargetsAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public MovePlayerTargetsAction(SpellActionType<?> type, String activation, String dst, String src)
    {
        super(type, activation, dst, src);
    }
    
    @Override
    public void findTargets(SpellContext ctx, TargetGroup source, TargetGroup destination)
    {
        List<Target> list = new LinkedList<>();
        
        source.forEachTarget(target ->
        {
            if(TargetTypes.PLAYER.get().isType(target))
            {
                destination.addTargets(target);
            }
            else
            {
                list.add(target);
            }
        });
        
        destination.clear();
        destination.addTargets(list);
    }
}
