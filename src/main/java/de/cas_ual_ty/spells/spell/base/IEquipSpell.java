package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.SpellHolder;

public interface IEquipSpell extends ISpell
{
    void onEquip(SpellHolder spellHolder, int slot);
    
    void onUnequip(SpellHolder spellHolder, int slot);
}
