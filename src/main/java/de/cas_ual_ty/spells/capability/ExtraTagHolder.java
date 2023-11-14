package de.cas_ual_ty.spells.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.common.util.LazyOptional;

public class ExtraTagHolder implements INBTSerializable<CompoundTag>
{
    protected CompoundTag tag;
    
    public ExtraTagHolder()
    {
        tag = new CompoundTag();
    }
    
    public void applyExtraTag(CompoundTag tag)
    {
        for(String s : tag.getAllKeys())
        {
            this.tag.put(s, tag.get(s));
        }
    }
    
    public CompoundTag getExtraTag()
    {
        return tag;
    }
    
    @Override
    public CompoundTag serializeNBT()
    {
        return tag;
    }
    
    @Override
    public void deserializeNBT(CompoundTag tag)
    {
        this.tag = tag;
    }
    
    public static LazyOptional<ExtraTagHolder> getHolder(Entity entity)
    {
        return entity.getCapability(SpellsCapabilities.EXTRA_TAG_CAPABILITY).cast();
    }
}
