package de.cas_ual_ty.spells.spell.context;

public enum BuiltinVariables
{
    MANA_COST("mana_cost"),
    DELAY_TIME("delay_time"),
    DELAY_UUID("delay_uuid"),
    DELAY_TAG("delay_tag"),
    SPELL_SLOT("spell_slot"),
    MIN_BLOCK_HEIGHT("min_block_height"),
    MAX_BLOCK_HEIGHT("max_block_height"),
    EVENT_IS_CANCELED("event_is_canceled");
    
    public final String name;
    
    BuiltinVariables(String name)
    {
        this.name = name;
    }
    
    @Override
    public String toString()
    {
        return name;
    }
}
