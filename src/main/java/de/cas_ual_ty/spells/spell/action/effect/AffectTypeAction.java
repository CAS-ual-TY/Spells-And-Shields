package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.util.SpellsCodecs;

public abstract class AffectTypeAction<T extends Target> extends SpellAction
{
    public static <T extends AffectTypeAction<?>> RecordCodecBuilder<T, String> targetsCodec()
    {
        return Codec.STRING.fieldOf("targets").forGetter(AffectTypeAction::getTargets);
    }
    
    public static <T extends AffectTypeAction<?>> RecordCodecBuilder<T, ITargetType<?>> targetTypeCodec()
    {
        return SpellsCodecs.TARGET_TYPE.fieldOf("targetType").forGetter(AffectTypeAction::getTargetType);
    }
    
    protected String targets;
    protected ITargetType<T> targetType;
    
    public AffectTypeAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public AffectTypeAction(SpellActionType<?> type, String activation, String targets, ITargetType<T> targetType)
    {
        super(type, activation);
        this.targets = targets;
        this.targetType = targetType;
    }
    
    public String getTargets()
    {
        return targets;
    }
    
    public ITargetType<?> getTargetType()
    {
        return targetType;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        ctx.forTargetGroup(targets, targetGroup -> targetGroup.forEachType(targetType, t -> affectTarget(ctx, targetType.asType(t))));
    }
    
    public abstract void affectTarget(SpellContext ctx, T t);
}
