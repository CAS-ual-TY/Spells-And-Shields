package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.network.FireClientSpellMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public interface IClientSpell extends ISpell
{
    void performOnClient(ManaHolder manaHolder);
    
    default void notifyClient(ServerPlayer player)
    {
        SpellsAndShields.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new FireClientSpellMessage(this));
    }
}