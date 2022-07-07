package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.SpellHolder;

public interface IEquippedTickSpell extends ISpell
{
    void tick(SpellHolder spellHolder);
    
    default void tickSingleton()
    {
    }
}
