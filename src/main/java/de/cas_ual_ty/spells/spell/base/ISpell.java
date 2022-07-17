package de.cas_ual_ty.spells.spell.base;

import com.google.common.collect.ImmutableList;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface ISpell
{
    boolean activate(ManaHolder manaHolder);
    
    default boolean performOnClient()
    {
        return false;
    }
    
    default SpellIcon getIcon()
    {
        return SpellsUtil.getDefaultSpellIcon(this);
    }
    
    default String getNameKey()
    {
        return SpellsUtil.getDefaultSpellNameKey(this);
    }
    
    default String getDescKey()
    {
        return SpellsUtil.getDefaultSpellDescKey(this);
    }
    
    default Component getSpellName()
    {
        return Component.translatable(getNameKey()).withStyle(ChatFormatting.YELLOW);
    }
    
    default List<Component> getSpellDescription()
    {
        return ImmutableList.of(Component.translatable(getDescKey()));
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
