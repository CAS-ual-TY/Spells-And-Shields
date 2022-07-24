package de.cas_ual_ty.spells.util;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.network.FireSpellMessage;
import de.cas_ual_ty.spells.spell.IClientSpell;
import de.cas_ual_ty.spells.spell.ISpell;
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
        else if(player instanceof ServerPlayer serverPlayer)
        {
            SpellHolder.getSpellHolder(player).ifPresent(spellHolder ->
            {
                ISpell spell = spellHolder.getSpell(slot);
                
                if(spell != null)
                {
                    ManaHolder.getManaHolder(player).ifPresent(manaHolder ->
                    {
                        if(spell.activate(manaHolder) && spell instanceof IClientSpell clientSpell)
                        {
                            clientSpell.notifyClient(serverPlayer);
                        }
                    });
                }
            });
        }
    }
}
