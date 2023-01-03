package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.util.ParamNames;

public abstract class AffectTypeAction<T extends Target> extends SpellAction
{
    public static <T extends AffectTypeAction<?>> RecordCodecBuilder<T, String> targetsCodec()
    {
        return Codec.STRING.fieldOf(ParamNames.multiTarget()).forGetter(AffectTypeAction::getTargets);
    }
    
    public static <T extends AffectTypeAction<?>> RecordCodecBuilder<T, String> targetCodec()
    {
        return Codec.STRING.fieldOf(ParamNames.singleTarget()).forGetter(AffectTypeAction::getTargets);
    }
    
    public static <T extends AffectTypeAction<?>> RecordCodecBuilder<T, String> sourceCodec()
    {
        return Codec.STRING.fieldOf(ParamNames.singleTarget("source")).forGetter(AffectTypeAction::getTargets);
    }
    
    protected String targets;
    
    public AffectTypeAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public AffectTypeAction(SpellActionType<?> type, String activation, String targets)
    {
        super(type, activation);
        this.targets = targets;
    }
    
    public abstract ITargetType<T> getAffectedType();
    
    public String getTargets()
    {
        return targets;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        ctx.forTargetGroup(targets, targetGroup -> targetGroup.forEachType(getAffectedType(), t -> affectTarget(ctx, targetGroup, getAffectedType().asType(t))));
    }
    
    public abstract void affectTarget(SpellContext ctx, TargetGroup group, T t);
}
