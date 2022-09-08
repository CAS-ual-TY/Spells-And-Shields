package de.cas_ual_ty.spells.spelldata;

import de.cas_ual_ty.spells.SpellsRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public interface ISpellDataType<D extends SpellData>
{
    D create(ISpellDataType<D> type);
    
    default D makeInstance()
    {
        return create(this);
    }
    
    static CompoundTag serialize(SpellData data)
    {
        CompoundTag tag = new CompoundTag();
        
        tag.putString("type", SpellsRegistries.SPELL_DATA_REGISTRY.get().getKey(data.getType()).toString());
        data.write(tag);
        
        return tag;
    }
    
    @Nullable
    static SpellData deserialize(CompoundTag tag)
    {
        String id = tag.getString("type");
        ISpellDataType<?> type = SpellsRegistries.SPELL_DATA_REGISTRY.get().getValue(new ResourceLocation(id));
        
        if(type == null)
        {
            return null;
        }
        
        SpellData data = type.makeInstance();
        data.read(tag);
        
        return data;
    }
}
