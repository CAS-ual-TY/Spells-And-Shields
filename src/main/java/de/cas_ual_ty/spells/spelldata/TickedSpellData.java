package de.cas_ual_ty.spells.spelldata;

import de.cas_ual_ty.spells.capability.SpellDataHolder;
import net.minecraft.nbt.CompoundTag;

public abstract class TickedSpellData extends SpellData implements ITickedSpellData
{
    protected int tickTime;
    
    public TickedSpellData(ISpellDataType type)
    {
        super(type);
        tickTime = 0;
    }
    
    @Override
    public void tick(SpellDataHolder spellDataHolder)
    {
        tick(spellDataHolder, tickTime++);
        
        if(getMaxTime(spellDataHolder) != -1 && tickTime > getMaxTime(spellDataHolder))
        {
            remove();
        }
    }
    
    // included
    public abstract int getMaxTime(SpellDataHolder spellDataHolder);
    
    protected abstract void tick(SpellDataHolder spellDataHolder, int tickTime);
    
    @Override
    public void read(CompoundTag tag)
    {
        tag.putInt("time", tickTime);
    }
    
    @Override
    public void write(CompoundTag tag)
    {
        tag.getInt("time");
    }
}