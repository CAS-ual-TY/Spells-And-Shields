package de.cas_ual_ty.spells.spell.impl;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.spell.IEventSpell;
import de.cas_ual_ty.spells.spell.base.PassiveSpell;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class ManaSolesSpell extends PassiveSpell implements IEventSpell
{
    public void livingHurt(LivingHurtEvent event)
    {
        if(event.getSource().isFall() && event.getEntityLiving() instanceof Player player)
        {
            SpellHolder.getSpellHolder(player).ifPresent(spellHolder ->
            {
                int amount = spellHolder.getAmountSpellEquipped(this);
                
                if(amount > 0)
                {
                    ManaHolder.getManaHolder(player).ifPresent(manaHolder ->
                    {
                        float mana = manaHolder.getMana() * amount;
                        float damage = event.getAmount();
                        
                        float min = Math.min(mana, damage);
                        
                        manaHolder.burn(min / amount);
                        
                        event.setAmount(damage - min);
                        
                        if(min >= damage)
                        {
                            event.setCanceled(true);
                        }
                    });
                }
            });
        }
    }
    
    @Override
    public void registerEvents()
    {
        MinecraftForge.EVENT_BUS.addListener(this::livingHurt);
    }
}
