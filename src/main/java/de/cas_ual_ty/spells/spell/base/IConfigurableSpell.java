package de.cas_ual_ty.spells.spell.base;

import com.google.gson.JsonObject;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IConfigurableSpell extends IForgeRegistryEntry<ISpell>
{
    JsonObject makeDefaultConfig();
    
    void readFromConfig(JsonObject json);
    
    void applyDefaultConfig();
    
    default String getFileName()
    {
        return this.getRegistryName().getNamespace() + "." + this.getRegistryName().getPath();
    }
}
