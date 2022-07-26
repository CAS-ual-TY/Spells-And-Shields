package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.capability.SpellHolder;

public interface ITickSpell extends ISpell
{
    void tick(SpellHolder spellHolder, int amount);
}
