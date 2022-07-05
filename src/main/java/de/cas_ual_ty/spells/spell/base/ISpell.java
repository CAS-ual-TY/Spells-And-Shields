package de.cas_ual_ty.spells.spell.base;

import com.google.common.collect.ImmutableList;
import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;

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
    
    default String getNameKey()
    {
        return "spell." + getRegistryName().getNamespace() + "." + getRegistryName().getPath();
    }
    
    default String getDescKey()
    {
        return getNameKey() + ".desc";
    }
    
    default Component getSpellName()
    {
        return new TranslatableComponent(getNameKey()).withStyle(ChatFormatting.YELLOW);
    }
    
    default List<Component> getSpellDescription()
    {
        return ImmutableList.of(new TranslatableComponent(getDescKey()));
    }
}
