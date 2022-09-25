package de.cas_ual_ty.spells.spelldata;

import de.cas_ual_ty.spells.capability.SpellDataHolder;

public interface ITickedSpellData
{
    void tick(SpellDataHolder spellDataHolder);
    
    default boolean tickOnClient()
    {
        return false;
    }
}