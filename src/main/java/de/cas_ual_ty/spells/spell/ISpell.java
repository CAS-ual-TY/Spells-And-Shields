package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.SpellIcon;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public interface ISpell
{
    boolean activate(ManaHolder manaHolder);
    
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
    
    default MutableComponent getSpellName()
    {
        return Component.translatable(getNameKey());
    }
    
    default MutableComponent getSpellDesc()
    {
        return Component.translatable(getDescKey());
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
