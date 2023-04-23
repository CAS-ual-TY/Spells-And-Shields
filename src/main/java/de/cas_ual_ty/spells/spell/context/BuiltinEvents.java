package de.cas_ual_ty.spells.spell.context;

public enum BuiltinEvents
{
    ACTIVE("active"),
    ON_EQUIP("on_equip"),
    ON_UNEQUIP("on_unequip"),
    PLAYER_BREAK_SPEED("player_break_speed"),
    LIVING_HURT("living_hurt");
    
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
