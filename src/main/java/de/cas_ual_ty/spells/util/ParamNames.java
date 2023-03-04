package de.cas_ual_ty.spells.util;

import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

public class ParamNames
{
    public static String actionType()
    {
        return "type";
    }
    
    public static String activation()
    {
        return "activation";
    }
    
    public static String varResult()
    {
        return var("result");
    }
    
    public static String var(String name)
    {
        return "var_" + name;
    }
    
    public static String interactedActivation(String activation)
    {
        return "a_" + activation;
    }
    
    public static String param(String name, CtxVarType<?> type)
    {
        return "d_" + paramImm(name, type);
    }
    
    public static String paramImm(String name, CtxVarType<?> type)
    {
        return CtxVarTypes.REGISTRY.get().getKey(type).getPath() + "_" + name;
    }
    
    public static String paramInt(String name)
    {
        return "d_" + paramIntImm(name);
    }
    
    public static String paramIntImm(String name)
    {
        return "int_" + name;
    }
    
    public static String paramDouble(String name)
    {
        return "d_" + paramDoubleImm(name);
    }
    
    public static String paramDoubleImm(String name)
    {
        return "double_" + name;
    }
    
    public static String paramVec3(String name)
    {
        return "d_" + paramVec3Imm(name);
    }
    
    public static String paramVec3Imm(String name)
    {
        return "vec3_" + name;
    }
    
    public static String paramBlockPos(String name)
    {
        return "d_" + paramBlockPosImm(name);
    }
    
    public static String paramBlockPosImm(String name)
    {
        return "block_pos_" + name;
    }
    
    public static String paramBoolean(String name)
    {
        return "d_" + paramBooleanImm(name);
    }
    
    public static String paramBooleanImm(String name)
    {
        return "boolean_" + name;
    }
    
    public static String paramCompoundTag(String name)
    {
        return "d_" + paramCompoundTagImm(name);
    }
    
    public static String paramCompoundTagImm(String name)
    {
        return "compound_tag_" + name;
    }
    
    public static String paramString(String name)
    {
        return "d_" + paramStringImm(name);
    }
    
    public static String paramStringImm(String name)
    {
        return "string_" + name;
    }
    
    public static String multiTarget()
    {
        return multiTarget("targets");
    }
    
    public static String singleTarget()
    {
        return singleTarget("target");
    }
    
    public static String multiTarget(String name)
    {
        return "ts_" + name;
    }
    
    public static String singleTarget(String name)
    {
        return "t_" + name;
    }
    
    public static String destinationTarget(String name)
    {
        return "td_" + name;
    }
}
