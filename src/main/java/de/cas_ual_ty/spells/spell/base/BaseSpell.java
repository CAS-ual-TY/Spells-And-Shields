package de.cas_ual_ty.spells.spell.base;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.spell.IConfigurableSpell;
import net.minecraft.resources.ResourceLocation;

public abstract class BaseSpell implements IConfigurableSpell
{
    public SpellIcon icon;
    
    public String nameKey;
    public String descKey;
    
    public BaseSpell()
    {
        this.icon = null;
        this.nameKey = null;
        this.descKey = null;
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
    
    public BaseSpell setIcon(ResourceLocation icon, int size)
    {
        this.icon = new SpellIcon(icon, 0, 0, size, size, size, size);
        return this;
    }
    
    public BaseSpell setIcon(ResourceLocation icon)
    {
        return setIcon(icon, 18);
    }
    
    public BaseSpell setSmallIcon(ResourceLocation icon)
    {
        return setIcon(icon, 16);
    }
    
    @Override
    public SpellIcon getIcon()
    {
        if(this.icon == null)
        {
            icon = IConfigurableSpell.super.getIcon();
        }
        
        return icon;
    }
    
    @Override
    public String getNameKey()
    {
        if(this.nameKey == null)
        {
            nameKey = IConfigurableSpell.super.getNameKey();
        }
        
        return nameKey;
    }
    
    @Override
    public String getDescKey()
    {
        if(this.descKey == null)
        {
            descKey = IConfigurableSpell.super.getDescKey();
        }
        
        return descKey;
    }
    
    @Override
    public boolean equals(Object o)
    {
        return this == o;
    }
}
