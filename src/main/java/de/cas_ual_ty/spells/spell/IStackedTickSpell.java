package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.capability.SpellHolder;

public interface IStackedTickSpell extends ISpell
{
    void tick(SpellHolder spellHolder, int slot);
}
