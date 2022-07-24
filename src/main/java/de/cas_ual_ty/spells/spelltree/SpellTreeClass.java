package de.cas_ual_ty.spells.spelltree;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.HashMap;
import java.util.Map;

public class SpellTreeClass
{
    public Map<Attribute, AttributeModifier> modifiers;
    
    public SpellTreeClass()
    {
        this.modifiers = new HashMap<>();
    }
    
    public String getName()
    {
        return ""; //TODO
    }
    
    public void addModifier(Attribute attribute, AttributeModifier modifier)
    {
        modifiers.put(attribute, modifier);
    }
    
    public void removeModifiers(LivingEntity entity)
    {
        for(Map.Entry<Attribute, AttributeModifier> entry : this.modifiers.entrySet())
        {
            AttributeInstance ai = entity.getAttribute(entry.getKey());
            
            if(ai.hasModifier(entry.getValue()))
            {
                ai.removeModifier(entry.getValue().getId());
            }
        }
    }
    
    public void applyModifiers(LivingEntity entity)
    {
        for(Map.Entry<Attribute, AttributeModifier> entry : this.modifiers.entrySet())
        {
            AttributeInstance ai = entity.getAttribute(entry.getKey());
            
            if(ai.hasModifier(entry.getValue()))
            {
                ai.removeModifier(entry.getValue().getId());
            }
            
            ai.addPermanentModifier(entry.getValue());
        }
    }
}
