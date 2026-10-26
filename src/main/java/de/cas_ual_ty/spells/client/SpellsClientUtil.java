package de.cas_ual_ty.spells.client;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.progression.FullSpellNodeId;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class SpellsClientUtil
{
    public static java.util.Optional<ManaHolder> getClientManaHolder()
    {
        if(Minecraft.getInstance().player != null)
        {
            return ManaHolder.getManaHolder(Minecraft.getInstance().player);
        }
        else
        {
            return java.util.Optional.empty();
        }
    }
    
    public static Level getClientLevel()
    {
        return Minecraft.getInstance().level;
    }
    
    public static Player getClientPlayer()
    {
        return Minecraft.getInstance().player;
    }
    
    public static void renderSpellTooltip(GuiGraphics guiGraphics, Font font, SpellInstance spellInstance, @Nullable Component keyBindTooltip, @Nullable FullSpellNodeId nodeId, int mouseX, int mouseY)
    {
        List<Component> tooltip = spellInstance.getSpell().value().makeTooltipList(keyBindTooltip);
        Optional<TooltipComponent> tooltipComponent = spellInstance.getTooltipComponent();
        
        if(SpellsClientConfig.SHOW_IDS.get())
        {
            Registry<Spell> spellRegistry = Spells.getRegistry(getClientLevel());
            tooltip.add(Component.literal(spellInstance.getSpell().unwrap().map(ResourceKey::location, spellRegistry::getKey).toString()).withStyle(ChatFormatting.DARK_GRAY));
            
            if(nodeId != null)
            {
                tooltip.add(Component.literal(nodeId.getIDText()).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        
        guiGraphics.renderTooltip(font, tooltip, tooltipComponent, mouseX, mouseY);
    }
}
