package de.cas_ual_ty.spells.spell.context;

public enum BuiltinVariables
{
    MANA_COST("mana_cost"),
    DELAY_TIME("delay_time");
    
    public final String name;
    
    BuiltinVariables(String name)
    {
        this.name = name;
    }
}
