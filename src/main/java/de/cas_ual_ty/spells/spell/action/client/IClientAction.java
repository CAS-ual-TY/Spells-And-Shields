package de.cas_ual_ty.spells.spell.action.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface IClientAction
{
    void writeToBuf(FriendlyByteBuf buf);
    
    void readFromBuf(FriendlyByteBuf buf);
    
    void execute(Level clientLevel, Player clientPlayer);
}
