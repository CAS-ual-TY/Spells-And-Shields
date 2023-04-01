package de.cas_ual_ty.spells.util;

import de.cas_ual_ty.spells.datagen.DocsGen;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

public class ParamNames
{
    static
    {
        DocsGen.PREFIX_MAP.put("a/", "String");
        DocsGen.PREFIX_MAP.put("ad/", "String");
        DocsGen.PREFIX_MAP.put("var/", "String");
        DocsGen.PREFIX_MAP.put("int/", "Integer");
        DocsGen.PREFIX_MAP.put("double/", "Double");
        DocsGen.PREFIX_MAP.put("vec3/", "Vector");
        DocsGen.PREFIX_MAP.put("block_pos/", "Block Position");
        DocsGen.PREFIX_MAP.put("boolean/", "Boolean");
        DocsGen.PREFIX_MAP.put("tag/", "NBT Compound Tag");
        DocsGen.PREFIX_MAP.put("string/", "String");
        DocsGen.PREFIX_MAP.put("ts/", "String");
        DocsGen.PREFIX_MAP.put("t/", "String");
        DocsGen.PREFIX_MAP.put("td/", "String");
    }
    
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
        return "var/" + name;
    }
    
    public static String interactedActivation(String activation)
    {
        return "a/" + activation;
    }
    
    public static String asynchronousActivation(String activation)
    {
        return "ad/" + activation;
    }
    
    public static String param(String name, CtxVarType<?> type)
    {
        return "d/" + paramImm(name, type);
    }
    
    public static String paramImm(String name, CtxVarType<?> type)
    {
        return CtxVarTypes.REGISTRY.get().getKey(type).getPath() + "/" + name;
    }
    
    public static String paramInt(String name)
    {
        return "d/" + paramIntImm(name);
    }
    
    public static String paramIntImm(String name)
    {
        return "int/" + name;
    }
    
    public static String paramDouble(String name)
    {
        return "d/" + paramDoubleImm(name);
    }
    
    public static String paramDoubleImm(String name)
    {
        return "double/" + name;
    }
    
    public static String paramVec3(String name)
    {
        return "d/" + paramVec3Imm(name);
    }
    
    public static String paramVec3Imm(String name)
    {
        return "vec3/" + name;
    }
    
    public static String paramBlockPos(String name)
    {
        return "d/" + paramBlockPosImm(name);
    }
    
    public static String paramBlockPosImm(String name)
    {
        return "block_pos/" + name;
    }
    
    public static String paramBoolean(String name)
    {
        return "d/" + paramBooleanImm(name);
    }
    
    public static String paramBooleanImm(String name)
    {
        return "boolean/" + name;
    }
    
    public static String paramCompoundTag(String name)
    {
        return "d/" + paramCompoundTagImm(name);
    }
    
    public static String paramCompoundTagImm(String name)
    {
        return "compound_tag/" + name;
    }
    
    public static String paramString(String name)
    {
        return "d/" + paramStringImm(name);
    }
    
    public static String paramStringImm(String name)
    {
        return "string/" + name;
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
        return "ts/" + name;
    }
    
    public static String singleTarget(String name)
    {
        return "t/" + name;
    }
    
    public static String destinationTarget(String name)
    {
        return "td/" + name;
    }
}
