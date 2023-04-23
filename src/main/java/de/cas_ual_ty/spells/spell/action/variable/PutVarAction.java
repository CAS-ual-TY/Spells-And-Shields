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
    
    public static PutVarAction<Integer> makeInt(Object activation, DynamicCtxVar<Integer> src, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_INT.get(), activation.toString(), src, dst.toString(), CtxVarTypes.INT.get());
    }
    
    public static PutVarAction<Integer> moveInt(Object activation, Object src, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_INT.get(), activation.toString(), Compiler.compileString(src.toString(), CtxVarTypes.INT.get()), dst.toString(), CtxVarTypes.INT.get());
    }
    
    public static PutVarAction<Integer> makeInt(Object activation, int value, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_INT.get(), activation.toString(), CtxVarTypes.INT.get().immediate(value), dst.toString(), CtxVarTypes.INT.get());
    }
    
    public static PutVarAction<Double> makeDouble(Object activation, DynamicCtxVar<Double> src, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_DOUBLE.get(), activation.toString(), src, dst.toString(), CtxVarTypes.DOUBLE.get());
    }
    
    public static PutVarAction<Double> moveDouble(Object activation, Object src, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_DOUBLE.get(), activation.toString(), Compiler.compileString(src.toString(), CtxVarTypes.DOUBLE.get()), dst.toString(), CtxVarTypes.DOUBLE.get());
    }
    
    public static PutVarAction<Double> makeDouble(Object activation, double value, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_DOUBLE.get(), activation.toString(), CtxVarTypes.DOUBLE.get().immediate(value), dst.toString(), CtxVarTypes.DOUBLE.get());
    }
    
    public static PutVarAction<Vec3> makeVec3(Object activation, DynamicCtxVar<Vec3> src, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_VEC3.get(), activation.toString(), src, dst.toString(), CtxVarTypes.VEC3.get());
    }
    
    public static PutVarAction<Vec3> moveVec3(Object activation, Object src, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_VEC3.get(), activation.toString(), Compiler.compileString(src.toString(), CtxVarTypes.VEC3.get()), dst.toString(), CtxVarTypes.VEC3.get());
    }
    
    public static PutVarAction<Vec3> makeVec3(Object activation, Vec3 value, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_VEC3.get(), activation.toString(), CtxVarTypes.VEC3.get().immediate(value), dst.toString(), CtxVarTypes.VEC3.get());
    }
    
    public static PutVarAction<BlockPos> makeBlockPos(Object activation, DynamicCtxVar<BlockPos> src, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_BLOCK_POS.get(), activation.toString(), src, dst.toString(), CtxVarTypes.BLOCK_POS.get());
    }
    
    public static PutVarAction<BlockPos> moveBlockPos(Object activation, Object src, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_BLOCK_POS.get(), activation.toString(), Compiler.compileString(src.toString(), CtxVarTypes.BLOCK_POS.get()), dst.toString(), CtxVarTypes.BLOCK_POS.get());
    }
    
    public static PutVarAction<BlockPos> makeBlockPos(Object activation, BlockPos value, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_BLOCK_POS.get(), activation.toString(), CtxVarTypes.BLOCK_POS.get().immediate(value), dst.toString(), CtxVarTypes.BLOCK_POS.get());
    }
    
    public static PutVarAction<Boolean> makeBoolean(Object activation, DynamicCtxVar<Boolean> src, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_BOOLEAN.get(), activation.toString(), src, dst.toString(), CtxVarTypes.BOOLEAN.get());
    }
    
    public static PutVarAction<Boolean> moveBoolean(Object activation, Object src, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_BOOLEAN.get(), activation.toString(), Compiler.compileString(src.toString(), CtxVarTypes.BOOLEAN.get()), dst.toString(), CtxVarTypes.BOOLEAN.get());
    }
    
    public static PutVarAction<Boolean> makeBoolean(Object activation, boolean value, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_BOOLEAN.get(), activation.toString(), CtxVarTypes.BOOLEAN.get().immediate(value), dst.toString(), CtxVarTypes.BOOLEAN.get());
    }
    
    public static PutVarAction<CompoundTag> makeCompoundTag(Object activation, DynamicCtxVar<CompoundTag> src, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_TAG.get(), activation.toString(), src, dst.toString(), CtxVarTypes.TAG.get());
    }
    
    public static PutVarAction<CompoundTag> moveCompoundTag(Object activation, Object src, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_TAG.get(), activation.toString(), Compiler.compileString(src.toString(), CtxVarTypes.TAG.get()), dst.toString(), CtxVarTypes.TAG.get());
    }
    
    public static PutVarAction<CompoundTag> makeCompoundTag(Object activation, CompoundTag value, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_TAG.get(), activation.toString(), CtxVarTypes.TAG.get().immediate(value), dst.toString(), CtxVarTypes.TAG.get());
    }
    
    public static PutVarAction<String> makeString(Object activation, DynamicCtxVar<String> src, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_STRING.get(), activation.toString(), src, dst.toString(), CtxVarTypes.STRING.get());
    }
    
    public static PutVarAction<String> moveString(Object activation, Object src, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_STRING.get(), activation.toString(), Compiler.compileString(src.toString(), CtxVarTypes.STRING.get()), dst.toString(), CtxVarTypes.STRING.get());
    }
    
    public static PutVarAction<String> makeString(Object activation, String value, Object dst)
    {
        return new PutVarAction<>(SpellActionTypes.PUT_STRING.get(), activation.toString(), CtxVarTypes.STRING.get().immediate(value), dst.toString(), CtxVarTypes.STRING.get());
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
