package de.cas_ual_ty.spells.util;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.network.FireSpellMessage;
import de.cas_ual_ty.spells.registers.BuiltinRegistries;
import de.cas_ual_ty.spells.spell.SpellInstance;
import net.minecraft.server.level.ServerPlayer;
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
        else if(player instanceof ServerPlayer serverPlayer && !isSilenced(player))
        {
            SpellHolder.getSpellHolder(player).ifPresent(spellHolder ->
            {
                SpellInstance spell = spellHolder.getSpell(slot);
                
                if(spell != null)
                {
                    spell.activate(spellHolder);
                }
            });
        }
    }
    
    public static boolean isSilenced(Player player)
    {
        return player.hasEffect(BuiltinRegistries.SILENCE_EFFECT.get());
    }
}
