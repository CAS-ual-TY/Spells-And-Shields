package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.capability.SpellHolder;

public interface IEquippedTickSpell extends ISpell
{
    void tick(SpellHolder spellHolder, int amount);
    
    default void tickSingleton()
    {
    }
}
