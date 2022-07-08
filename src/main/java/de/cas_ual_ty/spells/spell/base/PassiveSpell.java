package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.ManaHolder;

public class PassiveSpell extends BaseSpell implements IPassiveSpell
{
    @Override
    public boolean activate(ManaHolder manaHolder)
    {
        return false;
    }
}
