package de.cas_ual_ty.spells.spell.tree;

import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.spell.base.EventSpell;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class AquaAffinitySpell extends EventSpell<PlayerEvent.BreakSpeed>
{
    public void playerBreakSpeed(PlayerEvent.BreakSpeed event)
    {
        Player player = event.getPlayer();
        
        if(player.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player))
        {
            SpellHolder.getSpellHolder(player).ifPresent(spellHolder ->
            {
                for(int i = 0; i < SpellHolder.SPELL_SLOTS; i++)
                {
                    if(spellHolder.getSpell(i) == this)
                    {
                        event.setNewSpeed(event.getNewSpeed() * 5.0F);
                        break;
                    }
                }
            });
        }
    }
    
    @Override
    public void registerEvents()
    {
        MinecraftForge.EVENT_BUS.addListener(this::playerBreakSpeed);
    }
}
