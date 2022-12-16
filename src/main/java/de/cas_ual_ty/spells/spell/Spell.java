package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.context.BuiltinActivations;
import de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.icon.DefaultSpellIcon;
import de.cas_ual_ty.spells.spell.icon.SpellIcon;
import de.cas_ual_ty.spells.spell.target.Target;
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
    
    protected Optional<TooltipComponent> tooltipComponent;
    
    public Spell(List<SpellAction> spellActions, SpellIcon icon, Component title, List<Component> tooltip, float manaCost)
    {
        this.spellActions = spellActions;
        this.icon = icon;
        this.title = title;
        this.tooltip = tooltip;
        this.manaCost = manaCost;
        tooltipComponent = manaCost != 0 ? Optional.of(new ManaTooltipComponent(manaCost)) : Optional.empty();
    }
    
    public Spell(ResourceLocation icon, Component title, float manaCost)
    {
        this(new LinkedList<>(), new DefaultSpellIcon(SpellsRegistries.DEFAULT_SPELL_ICON.get(), icon), title, new LinkedList<>(), manaCost);
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
    
    public List<Component> getTooltip(@Nullable Component keyBindTooltip)
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
    
    public void activate(SpellHolder spellHolder)
    {
        activate(spellHolder, BuiltinActivations.ACTIVE.activation);
    }
    
    public void activate(SpellHolder spellHolder, String activation)
    {
        SpellContext ctx = new SpellContext(spellHolder.getPlayer().level, spellHolder, this);
        ctx.activate(activation);
        ctx.getOrCreateTargetGroup(BuiltinTargetGroups.OWNER.targetGroup).addTargets(Target.of(spellHolder.getPlayer()));
        run(ctx);
    }
    
    public void run(SpellContext ctx)
    {
        for(SpellAction spellAction : spellActions)
        {
            spellAction.doAction(ctx);
        }
    }
}
