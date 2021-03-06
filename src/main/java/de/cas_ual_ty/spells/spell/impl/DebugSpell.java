package de.cas_ual_ty.spells.spell.impl;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.Spell;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class DebugSpell extends Spell
{
    public String message;
    
    public DebugSpell(float manaCost, String message)
    {
        super(manaCost);
        this.message = message;
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        if(manaHolder.getPlayer() instanceof ServerPlayer player)
        {
            player.displayClientMessage(Component.literal(message + " " + System.currentTimeMillis()), true);
        }
    }
}
