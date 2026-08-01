package de.cas_ual_ty.spells.client;

import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SpellsClientUtil
{
    public static java.util.Optional<ManaHolder> getClientManaHolder()
    {
        if(Minecraft.getInstance().player != null)
        {
            return ManaHolder.getManaHolder(Minecraft.getInstance().player);
        }
        else
        {
            return java.util.Optional.empty();
        }
    }
    
    public static Level getClientLevel()
    {
        return Minecraft.getInstance().level;
    }
    
    public static Player getClientPlayer()
    {
        return Minecraft.getInstance().player;
    }
}
