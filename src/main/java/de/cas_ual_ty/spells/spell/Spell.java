package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.registers.SpellIconTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.icon.DefaultSpellIcon;
import de.cas_ual_ty.spells.spell.icon.SpellIcon;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import de.cas_ual_ty.spells.util.SpellsDowngrade;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Spell extends ForgeRegistryEntry<Spell>
{
    protected List<SpellAction> spellActions;
    protected SpellIcon icon;
    protected Component title;
    protected List<Component> tooltip;
    protected float manaCost;
    private List<CtxVar<?>> parameters;
    private List<String> events;
    
    public Spell(List<SpellAction> spellActions, SpellIcon icon, Component title, List<Component> tooltip, float manaCost, List<CtxVar<?>> parameters, List<String> events)
    {
        this.spellActions = spellActions;
        this.icon = icon;
        this.title = title;
        this.tooltip = tooltip;
        this.manaCost = manaCost;
        this.parameters = parameters;
        this.events = events;
    }
    
    public Spell(SpellIcon icon, Component title, List<Component> tooltip, float manaCost)
    {
        this(new ArrayList<>(), icon, title, tooltip, manaCost, new LinkedList<>(), new LinkedList<>());
    }
    
    public Spell(SpellIcon icon, Component title, float manaCost)
    {
        this(icon, title, new LinkedList<>(), manaCost);
    }
    
    public Spell(SpellIcon icon, String titleKey, float manaCost)
    {
        this(icon, SpellsDowngrade.translatable(titleKey), manaCost);
    }
    
    public Spell(ResourceLocation icon, Component title, float manaCost)
    {
        this(new DefaultSpellIcon(SpellIconTypes.DEFAULT.get(), icon), title, manaCost);
    }
    
    public Spell(String modId, String icon, String titleKey, float manaCost)
    {
        this(new ResourceLocation(modId, "textures/spell/" + icon + ".png"), SpellsDowngrade.translatable(titleKey), manaCost);
    }
    
    public Spell addTooltip(Component component)
    {
        tooltip.add(component);
        return this;
    }
    
    public Spell addAction(SpellAction action)
    {
        spellActions.add(action);
        return this;
    }
    
    public <T> Spell addParameter(CtxVar<T> ctxVar)
    {
        parameters.add(ctxVar);
        return this;
    }
    
    public <T> Spell addParameter(CtxVarType<T> type, String name, T value)
    {
        return addParameter(new CtxVar<>(type, name, value));
    }
    
    public Spell addEventHook(Object eventId)
    {
        events.add(eventId.toString());
        return this;
    }
    
    public List<SpellAction> getSpellActions()
    {
        return spellActions;
    }
    
    public SpellIcon getIcon()
    {
        return icon;
    }
    
    public Component getTitle()
    {
        return title;
    }
    
    public List<Component> getTooltip()
    {
        return tooltip;
    }
    
    public float getManaCost()
    {
        return manaCost;
    }
    
    public List<CtxVar<?>> getParameters()
    {
        return parameters;
    }
    
    public List<String> getEventsList()
    {
        return events;
    }
    
    public List<Component> makeTooltipList(@Nullable Component keyBindTooltip)
    {
        List<Component> tooltip = new LinkedList<>();
        tooltip.add(getTitle().copy().withStyle(ChatFormatting.YELLOW));
        
        if(keyBindTooltip != null)
        {
            tooltip.add(keyBindTooltip);
        }
        
        tooltip.addAll(getTooltip());
        
        return tooltip;
    }
}
