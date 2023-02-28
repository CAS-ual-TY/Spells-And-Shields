package de.cas_ual_ty.spells.spell.action.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.compiler.Compiler;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class PutVarAction<T> extends SpellAction
{
    public static <T> Codec<PutVarAction<T>> makeCodec(SpellActionType<PutVarAction<T>> type, CtxVarType<T> varType)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                varType.refCodec().fieldOf(ParamNames.param("source", varType)).forGetter(PutVarAction::getSrc),
                Codec.STRING.fieldOf(ParamNames.varResult()).forGetter(PutVarAction::getDst)
        ).apply(instance, (activation, src, dst) -> new PutVarAction<>(type, activation, src, dst, varType)));
    }
    
    public static PutVarAction<Integer> makeInt(String activation, DynamicCtxVar<Integer> src, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_INT.get(), activation, src, dst, CtxVarTypes.INT.get());
    }
    
    public static PutVarAction<Integer> makeInt(String activation, String src, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_INT.get(), activation, Compiler.compileString(src, CtxVarTypes.INT.get()), dst, CtxVarTypes.INT.get());
    }
    
    public static PutVarAction<Integer> makeInt(String activation, int value, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_INT.get(), activation, CtxVarTypes.INT.get().immediate(value), dst, CtxVarTypes.INT.get());
    }
    
    public static PutVarAction<Double> makeDouble(String activation, DynamicCtxVar<Double> src, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_DOUBLE.get(), activation, src, dst, CtxVarTypes.DOUBLE.get());
    }
    
    public static PutVarAction<Double> makeDouble(String activation, String src, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_DOUBLE.get(), activation, Compiler.compileString(src, CtxVarTypes.DOUBLE.get()), dst, CtxVarTypes.DOUBLE.get());
    }
    
    public static PutVarAction<Double> makeDouble(String activation, double value, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_DOUBLE.get(), activation, CtxVarTypes.DOUBLE.get().immediate(value), dst, CtxVarTypes.DOUBLE.get());
    }
    
    public static PutVarAction<Vec3> makeVec3(String activation, DynamicCtxVar<Vec3> src, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_VEC3.get(), activation, src, dst, CtxVarTypes.VEC3.get());
    }
    
    public static PutVarAction<Vec3> makeVec3(String activation, String src, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_VEC3.get(), activation, Compiler.compileString(src, CtxVarTypes.VEC3.get()), dst, CtxVarTypes.VEC3.get());
    }
    
    public static PutVarAction<Vec3> makeVec3(String activation, Vec3 value, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_VEC3.get(), activation, CtxVarTypes.VEC3.get().immediate(value), dst, CtxVarTypes.VEC3.get());
    }
    
    public static PutVarAction<BlockPos> makeBlockPos(String activation, DynamicCtxVar<BlockPos> src, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_BLOCK_POS.get(), activation, src, dst, CtxVarTypes.BLOCK_POS.get());
    }
    
    public static PutVarAction<BlockPos> makeBlockPos(String activation, String src, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_BLOCK_POS.get(), activation, Compiler.compileString(src, CtxVarTypes.BLOCK_POS.get()), dst, CtxVarTypes.BLOCK_POS.get());
    }
    
    public static PutVarAction<BlockPos> makeBlockPos(String activation, BlockPos value, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_BLOCK_POS.get(), activation, CtxVarTypes.BLOCK_POS.get().immediate(value), dst, CtxVarTypes.BLOCK_POS.get());
    }
    
    public static PutVarAction<Boolean> makeBoolean(String activation, DynamicCtxVar<Boolean> src, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_BOOLEAN.get(), activation, src, dst, CtxVarTypes.BOOLEAN.get());
    }
    
    public static PutVarAction<Boolean> makeBoolean(String activation, String src, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_BOOLEAN.get(), activation, Compiler.compileString(src, CtxVarTypes.BOOLEAN.get()), dst, CtxVarTypes.BOOLEAN.get());
    }
    
    public static PutVarAction<Boolean> makeBoolean(String activation, boolean value, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_BOOLEAN.get(), activation, CtxVarTypes.BOOLEAN.get().immediate(value), dst, CtxVarTypes.BOOLEAN.get());
    }
    
    public static PutVarAction<CompoundTag> makeCompoundTag(String activation, DynamicCtxVar<CompoundTag> src, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_COMPOUND_TAG.get(), activation, src, dst, CtxVarTypes.COMPOUND_TAG.get());
    }
    
    public static PutVarAction<CompoundTag> makeCompoundTag(String activation, String src, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_COMPOUND_TAG.get(), activation, Compiler.compileString(src, CtxVarTypes.COMPOUND_TAG.get()), dst, CtxVarTypes.COMPOUND_TAG.get());
    }
    
    public static PutVarAction<CompoundTag> makeCompoundTag(String activation, CompoundTag value, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_COMPOUND_TAG.get(), activation, CtxVarTypes.COMPOUND_TAG.get().immediate(value), dst, CtxVarTypes.COMPOUND_TAG.get());
    }
    
    public static PutVarAction<String> makeString(String activation, DynamicCtxVar<String> src, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_STRING.get(), activation, src, dst, CtxVarTypes.STRING.get());
    }
    
    public static PutVarAction<String> makeStringMoveVar(String activation, String src, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_STRING.get(), activation, Compiler.compileString(src, CtxVarTypes.STRING.get()), dst, CtxVarTypes.STRING.get());
    }
    
    public static PutVarAction<String> makeString(String activation, String value, String dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_STRING.get(), activation, CtxVarTypes.STRING.get().immediate(value), dst, CtxVarTypes.STRING.get());
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
            ctx.setCtxVar(varType, dst, varType.copy(value));
        });
    }
    
    public static <T> SpellActionType<PutVarAction<T>> makeType(Supplier<CtxVarType<T>> varType)
    {
        return new SpellActionType<>((type) -> new PutVarAction<>(type, varType.get()), (type) -> makeCodec(type, varType.get()));
    }
}
