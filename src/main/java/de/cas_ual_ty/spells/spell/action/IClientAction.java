package de.cas_ual_ty.spells.spell.action;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface IClientAction
{
    void writeToBuf(RegistryFriendlyByteBuf buf);

    void readFromBuf(RegistryFriendlyByteBuf buf);
    
    void execute(Level clientLevel, Player clientPlayer);
}
