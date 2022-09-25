package de.cas_ual_ty.spells.spelldata;

import de.cas_ual_ty.spells.SpellsRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.function.Function;

public interface ISpellDataType extends IForgeRegistryEntry<ISpellDataType.SpellDataType>
{
    SpellData create(ISpellDataType type);
    
    default SpellData makeInstance()
    {
        return create(this);
    }
    
    static CompoundTag serialize(SpellData data)
    {
        CompoundTag tag = new CompoundTag();
        
        tag.putString("type", data.getType().getRegistryName().toString());
        data.write(tag);
        
        return tag;
    }
    
    @Nullable
    static SpellData deserialize(CompoundTag tag)
    {
        String id = tag.getString("type");
        ISpellDataType type = SpellsRegistries.SPELL_DATA_REGISTRY.get().getValue(new ResourceLocation(id));
        
        if(type == null)
        {
            return null;
        }
        
        SpellData data = type.makeInstance();
        data.read(tag);
        
        return data;
    }
    
    // this class is needed because in 1.18 any registry object must also extend/implement IForgeRegistryEntry
    // this means that ISpellDataType can not be a functional interface anymore
    // and thus this helper class is needed
    class SpellDataType extends ForgeRegistryEntry<ISpellDataType.SpellDataType> implements ISpellDataType
    {
        public final Function<SpellDataType, SpellData> factory;
        
        public SpellDataType(Function<SpellDataType, SpellData> factory)
        {
            this.factory = factory;
        }
        
        @Override
        public SpellData create(ISpellDataType type)
        {
            return factory.apply((SpellDataType) type);
        }
    }
}