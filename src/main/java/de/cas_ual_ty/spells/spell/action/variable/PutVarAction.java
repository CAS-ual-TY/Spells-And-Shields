package de.cas_ual_ty.spells.spell.action.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;

import java.util.function.Supplier;

public class PutVarAction<T> extends SpellAction
{
    public static <T> Codec<PutVarAction<T>> makeCodec(SpellActionType<PutVarAction<T>> type, CtxVarType<T> varType)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                varType.refCodec().fieldOf("source").forGetter(PutVarAction::getSrc),
                Codec.STRING.fieldOf("destination").forGetter(PutVarAction::getDst)
        ).apply(instance, (activation, src, dst) -> new PutVarAction<>(type, activation, src, dst, varType)));
    }
    
    protected DynamicCtxVar<T> src;
    protected String dst;
    
    protected CtxVarType<T> varType;
    
    public PutVarAction(SpellActionType<?> type, CtxVarType<T> varType)
    {
        super(type);
        this.varType = varType;
    }
    
    public PutVarAction(SpellActionType<?> type, String activation, DynamicCtxVar<T> src, String dst, CtxVarType<T> varType)
    {
        super(type, activation);
        this.src = src;
        this.dst = dst;
        this.varType = varType;
    }
    
    public DynamicCtxVar<T> getSrc()
    {
        return src;
    }
    
    public String getDst()
    {
        return dst;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        src.getValue(ctx).ifPresent(value ->
        {
            ctx.setCtxVar(varType, dst, value);
        });
    }
    
    public static <T> SpellActionType<PutVarAction<T>> makeType(Supplier<CtxVarType<T>> varType)
    {
        return new SpellActionType<>((type) -> new PutVarAction<>(type, varType.get()), (type) -> makeCodec(type, varType.get()));
    }
}
