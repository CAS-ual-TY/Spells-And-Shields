package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface ISpell extends IForgeRegistryEntry<ISpell>
{
    boolean activate(ManaHolder manaHolder);
    
    default boolean performOnClient()
    {
        return false;
    }
    
    default ResourceLocation getIcon()
    {
        return new ResourceLocation(getRegistryName().getNamespace(), "textures/spell/" + getRegistryName().getPath() + ".png");
    }
}
