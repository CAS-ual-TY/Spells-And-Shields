package de.cas_ual_ty.spells.spell.context;

public enum BuiltinEvents
{
    ACTIVE("active"),
    ON_EQUIP("on_equip"),
    ON_UNEQUIP("on_unequip"),
    LIVING_ATTACK_VICTIM("living_attack_victim"),
    LIVING_HURT_VICTIM("living_hurt_victim");
    
    public final String activation;
    
    BuiltinEvents(String activation)
    {
        this.activation = activation;
    }
    
    @Override
    public String toString()
    {
        return activation;
    }
}
