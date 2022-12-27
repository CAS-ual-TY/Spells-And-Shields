package de.cas_ual_ty.spells.spell.context;

import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

import java.util.function.Supplier;

public enum BuiltinVariables
{
    MANA_COST("mana_cost", CtxVarTypes.DOUBLE);
    
    public final String name;
    public final Supplier<CtxVarType<Double>> type;
    
    BuiltinVariables(String name, Supplier<CtxVarType<Double>> type)
    {
        this.name = name;
        this.type = type;
    }
}
