package de.cas_ual_ty.spells.spell.base;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class BaseSpell extends ForgeRegistryEntry<ISpell> implements IConfigurableSpell
{
    public ResourceLocation icon;
    
    public BaseSpell()
    {
        this.icon = null;
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        return new JsonObject();
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
    }
    
    @Override
    public void applyDefaultConfig()
    {
    }
    
    public BaseSpell setIcon(ResourceLocation icon)
    {
        this.icon = icon;
        return this;
    }
    
    @Override
    public ResourceLocation getIcon()
    {
        return icon != null ? icon : IConfigurableSpell.super.getIcon();
    }
    
    @Override
    public boolean equals(Object o)
    {
        return this == o;
    }
    
    @Override
    public int hashCode()
    {
        return this.getRegistryName().hashCode();
    }
}
