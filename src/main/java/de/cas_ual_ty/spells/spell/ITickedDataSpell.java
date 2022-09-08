package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.capability.SpellDataHolder;
import de.cas_ual_ty.spells.spelldata.ISpellDataType;
import de.cas_ual_ty.spells.spelldata.SimpleTickedSpellData;

import java.util.function.Supplier;

public interface ITickedDataSpell extends ISpell
{
    int getMaxTime(SpellDataHolder spellDataHolder);
    
    void dataTick(SpellDataHolder spellDataHolder, int tickTime);
    
    static SimpleTickedSpellData makeData(ISpellDataType<SimpleTickedSpellData> type)
    {
        return type.makeInstance();
    }
    
    // for convenient registration ONLY
    static ISpellDataType<SimpleTickedSpellData> makeDataType(Supplier<ISpell> spell)
    {
        return (type) -> new SimpleTickedSpellData(type, spell);
    }
}
