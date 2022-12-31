package de.cas_ual_ty.spells.spell.target;

import net.minecraft.world.entity.player.Player;

public class PlayerTarget extends LivingEntityTarget
{
    protected Player player;
    
    public PlayerTarget(ITargetType<?> type, Player player)
    {
        super(type, player);
        this.player = player;
    }
    
    public Player getPlayer()
    {
        return player;
    }
}
