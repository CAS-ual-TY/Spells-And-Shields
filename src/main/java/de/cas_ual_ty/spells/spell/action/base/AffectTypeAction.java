package de.cas_ual_ty.spells.spell.action.base;

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
    public static <T extends AffectTypeAction<?>> RecordCodecBuilder<T, String> multiTargetsCodec()
    {
        return Codec.STRING.fieldOf(ParamNames.multiTarget()).forGetter(AffectTypeAction::getMultiTargets);
    }
    
    public static <T extends AffectTypeAction<?>> RecordCodecBuilder<T, String> singleTargetCodec()
    {
        return Codec.STRING.fieldOf(ParamNames.singleTarget()).forGetter(AffectTypeAction::getMultiTargets);
    }
    
    public static <T extends AffectTypeAction<?>> RecordCodecBuilder<T, String> sourceCodec()
    {
        return Codec.STRING.fieldOf(ParamNames.singleTarget("source")).forGetter(AffectTypeAction::getMultiTargets);
    }
    
    protected String multiTargets;
    
    public AffectTypeAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public AffectTypeAction(SpellActionType<?> type, String activation, String multiTargets)
    {
        super(type, activation);
        this.multiTargets = multiTargets;
    }
    
    public abstract ITargetType<T> getAffectedType();
    
    public String getMultiTargets()
    {
        return multiTargets;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        ctx.forTargetGroup(multiTargets, targetGroup -> targetGroup.forEachTypeSafe(getAffectedType(), t -> affectTarget(ctx, targetGroup, getAffectedType().asType(t))));
    }
    
    public abstract void affectTarget(SpellContext ctx, TargetGroup group, T t);
}
