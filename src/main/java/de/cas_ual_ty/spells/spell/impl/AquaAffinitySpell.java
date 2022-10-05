package de.cas_ual_ty.spells.spell.impl;

import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.spell.IEventSpell;
import de.cas_ual_ty.spells.spell.base.PassiveSpell;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class AquaAffinitySpell extends PassiveSpell implements IEventSpell
{
    public void playerBreakSpeed(PlayerEvent.BreakSpeed event)
    {
        Player player = event.getPlayer();
        
        if(player.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player))
        {
            SpellHolder.getSpellHolder(player).ifPresent(spellHolder ->
            {
                if(spellHolder.getAmountSpellEquipped(this) > 0)
                {
                    event.setNewSpeed(event.getNewSpeed() * 5F);
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
