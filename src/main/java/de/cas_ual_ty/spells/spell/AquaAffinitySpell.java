package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.spell.base.IEventSpell;
import de.cas_ual_ty.spells.spell.base.PassiveSpell;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class AquaAffinitySpell extends PassiveSpell implements IEventSpell
{
    public void playerBreakSpeed(PlayerEvent.BreakSpeed event)
    {
        Player player = event.getEntity();
        
        if(player.isEyeInFluidType(ForgeMod.WATER_TYPE.get()) && !EnchantmentHelper.hasAquaAffinity(player))
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
