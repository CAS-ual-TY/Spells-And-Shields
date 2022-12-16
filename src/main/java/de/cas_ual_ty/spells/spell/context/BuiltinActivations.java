package de.cas_ual_ty.spells.spell.context;

public enum BuiltinActivations
{
    ACTIVE("active"),
    ON_EQUIP("on_equip"),
    ON_UNEQUIP("on_unequip");
    
    public final String activation;
    
    BuiltinActivations(String activation)
    {
        this.activation = activation;
    }
}
