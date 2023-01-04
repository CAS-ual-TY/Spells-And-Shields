package de.cas_ual_ty.spells.spell.action.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.util.ParamNames;

public class PickTargetAction extends CopyTargetsAction
{
    public static Codec<PickTargetAction> makeCodec2(SpellActionType<PickTargetAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                dstCodec(),
                srcCodec(),
                Codec.BOOL.fieldOf(ParamNames.paramBooleanImm("remove")).forGetter(PickTargetAction::getRemove),
                Codec.BOOL.fieldOf(ParamNames.paramBooleanImm("random")).forGetter(PickTargetAction::getRandom)
        ).apply(instance, (activation, dst, src, remove, random) -> new PickTargetAction(type, activation, dst, src, remove, random)));
    }
    
    protected boolean remove;
    protected boolean random;
    
    public PickTargetAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public PickTargetAction(SpellActionType<?> type, String activation, String dst, String src, boolean remove, boolean random)
    {
        super(type, activation, dst, src);
        this.remove = remove;
        this.random = random;
    }
    
    public boolean getRemove()
    {
        return remove;
    }
    
    public boolean getRandom()
    {
        return random;
    }
    
    @Override
    public void findTargets(SpellContext ctx, TargetGroup source, TargetGroup destination)
    {
        if(source == null || source.isEmpty())
        {
            return;
        }
        
        int pick = random ? ctx.level.getRandom().nextInt(source.size()) : 0;
        Target t = remove ? source.getTargets().remove(pick) : source.getTargets().get(pick);
        
        destination.addTargets(t);
    }
}
