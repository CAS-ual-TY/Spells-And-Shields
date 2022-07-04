package de.cas_ual_ty.spells.progression;

public enum SpellStatus
{
    LEARNED,
    FORGOTTEN,
    LOCKED;
    
    public boolean isAvailable()
    {
        return this == LEARNED;
    }
    
    public boolean isVisible()
    {
        return isAvailable() || this == FORGOTTEN;
    }
}
