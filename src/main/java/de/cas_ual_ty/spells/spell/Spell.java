package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsConfig;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.SpellIconTypes;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.icon.DefaultSpellIcon;
import de.cas_ual_ty.spells.spell.icon.SpellIcon;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.util.ManaTooltipComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Spell
{
    protected List<SpellAction> spellActions;
    protected SpellIcon icon;
    protected Component title;
    protected List<Component> tooltip;
    protected float manaCost;
    private List<CtxVar<?>> parameters;
    
    protected Optional<TooltipComponent> tooltipComponent;
    
    public Spell(List<SpellAction> spellActions, SpellIcon icon, Component title, List<Component> tooltip, float manaCost, List<CtxVar<?>> parameters)
    {
        this.spellActions = spellActions;
        this.icon = icon;
        this.title = title;
        this.tooltip = tooltip;
        this.manaCost = manaCost;
        this.parameters = parameters;
        tooltipComponent = manaCost != 0 ? Optional.of(new ManaTooltipComponent(manaCost)) : Optional.empty();
    }
    
    public Spell(ResourceLocation icon, Component title, float manaCost)
    {
        this(new LinkedList<>(), new DefaultSpellIcon(SpellIconTypes.DEFAULT.get(), icon), title, new LinkedList<>(), manaCost, new LinkedList<>());
    }
    
    public Spell(String modId, String icon, String titleKey, float manaCost)
    {
        this(new ResourceLocation(modId, "textures/spell/" + icon + ".png"), Component.translatable(titleKey), manaCost);
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
    
    public List<Component> makeTooltipList(@Nullable Component keyBindTooltip)
    {
        List<Component> tooltip = new LinkedList<>();
        
        tooltip.add(MutableComponent.create(getTitle().getContents()).withStyle(ChatFormatting.YELLOW));
        
        if(keyBindTooltip != null)
        {
            tooltip.add(keyBindTooltip);
        }
        
        tooltip.addAll(getTooltip());
        
        return tooltip;
    }
    
    public Optional<TooltipComponent> getTooltipComponent()
    {
        return tooltipComponent;
    }
    
    public void run(SpellContext ctx)
    {
        if(SpellsConfig.DEBUG_SPELLS.get())
        {
            SpellsAndShields.LOGGER.info("Running spell " + Spells.getRegistry(ctx.getLevel()).getKey(this));
            SpellsAndShields.LOGGER.info("Initial state:");
            ctx.debugCtxVars();
            ctx.debugTargetGroups();
        }
        
        for(SpellAction spellAction : spellActions)
        {
            if(SpellsConfig.DEBUG_SPELLS.get())
            {
                SpellsAndShields.LOGGER.info("Starting action " + SpellActionTypes.REGISTRY.get().getKey(spellAction.getType()));
            }
            
            spellAction.doAction(ctx);
            
            if(SpellsConfig.DEBUG_SPELLS.get())
            {
                SpellsAndShields.LOGGER.info("Finish action " + SpellActionTypes.REGISTRY.get().getKey(spellAction.getType()));
                ctx.debugCtxVars();
                ctx.debugTargetGroups();
            }
        }
    }
}
