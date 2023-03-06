package de.cas_ual_ty.spells.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.LazyOptional;

public class ExtraTagHolder implements IExtraTagHolder
{
    protected CompoundTag tag;
    
    public ExtraTagHolder()
    {
        this.tag = new CompoundTag();
    }
    
    @Override
    public void applyExtraTag(CompoundTag tag)
    {
        for(String s : tag.getAllKeys())
        {
            this.tag.put(s, tag.get(s));
        }
    }
    
    @Override
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
