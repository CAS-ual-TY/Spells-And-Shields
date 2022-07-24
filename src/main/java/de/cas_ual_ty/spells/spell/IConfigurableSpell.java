package de.cas_ual_ty.spells.spell;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public interface IConfigurableSpell extends ISpell
{
    JsonObject makeDefaultConfig();
    
    void readFromConfig(JsonObject json);
    
    void applyDefaultConfig();
    
    default String getFileName(ResourceLocation registryName)
    {
        return registryName.getNamespace() + "." + registryName.getPath();
    }
}
