package de.cas_ual_ty.spells.spell.base;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class BaseSpell extends ForgeRegistryEntry<ISpell> implements IConfigurableSpell
{
    public SpellIcon icon;
    
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
    
    public BaseSpell setIcon(SpellIcon icon)
    {
        this.icon = icon;
        return this;
    }
    
    public BaseSpell setIcon(ResourceLocation icon)
    {
        this.icon = new SpellIcon(icon, 0, 0, 18, 18, 18, 18);
        return this;
    }
    
    public BaseSpell setSmallIcon(ResourceLocation icon)
    {
        this.icon = new SpellIcon(icon, 0, 0, 16, 16, 16, 16);
        return this;
    }
    
    @Override
    public SpellIcon getIcon()
    {
        if(this.icon == null)
        {
            icon = new SpellIcon(new ResourceLocation(getRegistryName().getNamespace(), "textures/spell/" + getRegistryName().getPath() + ".png"));
        }
        
        return icon;
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
