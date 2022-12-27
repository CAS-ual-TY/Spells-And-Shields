package de.cas_ual_ty.spells.spell.action.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.effect.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

import java.util.List;
import java.util.function.Function;

public abstract class GetTargetAttributeAction<T extends Target> extends AffectSingleTypeAction<T>
{
    public static <T extends Target, C> Codec<TargetAttribute<T, C>> targetAttributeCodec(Function<T, C> getter, CtxVarType<C> varType)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("result").forGetter(TargetAttribute::varName)
        ).apply(instance, (varName) -> new TargetAttribute<>(getter, varType, varName)));
    }
    
    public GetTargetAttributeAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetTargetAttributeAction(SpellActionType<?> type, String activation, String targets)
    {
        super(type, activation, targets);
    }
    
    public abstract List<TargetAttribute<T, ?>> getAttributes();
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, T t)
    {
        for(TargetAttribute<T, ?> attribute : getAttributes())
        {
            attribute.apply(ctx, t);
        }
    }
    
    public static record TargetAttribute<T extends Target, C>(Function<T, C> getter, CtxVarType<C> varType,
                                                              String varName)
    {
        public void apply(SpellContext ctx, T t)
        {
            ctx.setCtxVar(varType, varName, getter().apply(t));
        }
    }
}
