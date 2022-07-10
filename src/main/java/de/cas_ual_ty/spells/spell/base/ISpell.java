package de.cas_ual_ty.spells.spell.base;

import com.google.common.collect.ImmutableList;
import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;

public interface ISpell extends IForgeRegistryEntry<ISpell>
{
    boolean activate(ManaHolder manaHolder);
    
    default boolean performOnClient()
    {
        return false;
    }
    
    SpellIcon getIcon();
    
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
    
    default float getInertia()
    {
        return 1F;
    }
    
    default ParticleOptions getTrailParticle()
    {
        return ParticleTypes.POOF;
    }
}
