package de.cas_ual_ty.spells.spell.action.target.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.Target;

import java.util.function.Supplier;

public class TypeFilterAction extends FilterTargetsAction
{
    public static Codec<TypeFilterAction> makeCodec(SpellActionType<TypeFilterAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                dstCodec(),
                srcCodec(),
                TargetTypes.REGISTRY.get().getCodec().fieldOf("target_type").forGetter(TypeFilterAction::getTargetType)
        ).apply(instance, (activation, dst, src, targetType) -> new TypeFilterAction(type, activation, dst, src, () -> targetType)));
    }
    
    protected Supplier<ITargetType<?>> targetType;
    
    public TypeFilterAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public TypeFilterAction(SpellActionType<?> type, String activation, String dst, String src, Supplier<ITargetType<?>> targetType)
    {
        super(type, activation, dst, src);
        this.targetType = targetType;
    }
    
    public ITargetType<?> getTargetType()
    {
        return targetType.get();
    }
    
    @Override
    protected boolean acceptTarget(SpellContext ctx, Target target)
    {
        return this.targetType.get().isType(target);
    }
}
