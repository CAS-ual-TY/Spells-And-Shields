package de.cas_ual_ty.spells.spelldata;

import de.cas_ual_ty.spells.capability.SpellDataHolder;
import de.cas_ual_ty.spells.spell.ISpell;
import de.cas_ual_ty.spells.spell.ITickedDataSpell;

import java.util.function.Supplier;

public class SimpleTickedSpellData extends TickedSpellData
{
    protected final ITickedDataSpell spell;
    
    public SimpleTickedSpellData(ISpellDataType type, Supplier<ISpell> spell)
    {
        super(type);
        
        ISpell s = spell.get();
        
        if(s instanceof ITickedDataSpell s2)
        {
            this.spell = s2;
        }
        else
        {
            throw new IllegalArgumentException("Spell " + s.getNameKey() + " must be an instance of " + ITickedDataSpell.class.getName());
        }
    }
    
    @Override
    public int getMaxTime(SpellDataHolder spellDataHolder)
    {
        return this.spell.getMaxTime(spellDataHolder);
    }
    
    @Override
    protected void tick(SpellDataHolder spellDataHolder, int tickTime)
    {
        this.spell.dataTick(spellDataHolder, tickTime);
    }
}