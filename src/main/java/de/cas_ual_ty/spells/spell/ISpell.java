package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.SpellIcon;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public interface ISpell extends IForgeRegistryEntry<ISpell>
{
    boolean activate(ManaHolder manaHolder);
    
    SpellIcon getIcon();
    
    default String getNameKey()
    {
        return "spell." + getRegistryName().getNamespace() + "." + getRegistryName().getPath();
    }
    
    default String getDescKey()
    {
        return getNameKey() + ".desc";
    }
    
    default MutableComponent getSpellName()
    {
        return new TranslatableComponent(getNameKey());
    }
    
    default MutableComponent getSpellDesc()
    {
        return new TranslatableComponent(getDescKey());
    }
    
    default void addSpellDesc(List<Component> tooltip)
    {
        tooltip.add(getSpellDesc());
    }
    
    default List<Component> getTooltip(@Nullable Component keyBindTooltip)
    {
        List<Component> tooltip = new LinkedList<>();
        
        tooltip.add(getSpellName().withStyle(ChatFormatting.YELLOW));
        
        if(keyBindTooltip != null)
        {
            tooltip.add(keyBindTooltip);
        }
        
        addSpellDesc(tooltip);
        
        return tooltip;
    }
    
    default Optional<TooltipComponent> getTooltipComponent()
    {
        return Optional.empty();
    }
}
