package de.cas_ual_ty.spells.spelldata;

import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.capability.SpellDataHolder;
import net.minecraft.nbt.CompoundTag;

public abstract class SpellData
{
    public final ISpellDataType<?> type;
    
    protected String descriptionId;
    protected boolean remove;
    
    public SpellData(ISpellDataType<?> type)
    {
        this.type = type;
        this.descriptionId = SpellsRegistries.SPELL_DATA_REGISTRY.get().getKey(type).toString();
        this.remove = false;
    }
    
    public ISpellDataType<?> getType()
    {
        return type;
    }
    
    public void remove()
    {
        this.remove = true;
    }
    
    public boolean shouldRemove(SpellDataHolder spellDataHolder)
    {
        return remove;
    }
    
    public boolean keepOnDeath(SpellDataHolder spellDataHolder)
    {
        return false;
    }
    
    public abstract void read(CompoundTag tag);
    
    public abstract void write(CompoundTag tag);
}
