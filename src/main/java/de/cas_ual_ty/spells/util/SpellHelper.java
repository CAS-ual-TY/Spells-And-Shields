package de.cas_ual_ty.spells.util;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.network.FireSpellMessage;
import de.cas_ual_ty.spells.spell.ISpell;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

public class SpellHelper
{
    public static void fireSpellSlot(Player player, int slot)
    {
        if(player.level.isClientSide)
        {
            SpellsAndShields.CHANNEL.send(PacketDistributor.SERVER.noArg(), new FireSpellMessage(slot));
        }
        
        SpellHolder.getSpellHolder(player).ifPresent(spellHolder ->
        {
            ISpell spell = spellHolder.getSpell(slot);
            
            if(spell != null && (!spellHolder.getPlayer().level.isClientSide || spell.performOnClient()))
            {
                ManaHolder.getManaHolder(player).ifPresent(manaHolder -> fireSpell(manaHolder, spell));
            }
        });
    }
    
    public static void fireSpell(ManaHolder manaHolder, ISpell spell)
    {
        spell.activate(manaHolder);
    }
}
