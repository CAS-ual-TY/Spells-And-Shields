package de.cas_ual_ty.spells.event;

import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.List;

public class AvailableSpellTreesEvent extends PlayerEvent
{
    protected final SpellProgressionHolder spellProgressionHolder;
    protected final List<SpellTree> availableSpellTrees;
    
    public AvailableSpellTreesEvent(Player player, SpellProgressionHolder spellProgressionHolder, List<SpellTree> availableSpellTrees)
    {
        super(player);
        this.spellProgressionHolder = spellProgressionHolder;
        this.availableSpellTrees = availableSpellTrees;
    }
    
    public SpellProgressionHolder getSpellProgressionHolder()
    {
        return this.spellProgressionHolder;
    }
    
    public void addSpellTree(SpellTree spellTree)
    {
        availableSpellTrees.add(spellTree);
    }
    
    public List<SpellTree> getSpellTreesList()
    {
        return availableSpellTrees;
    }
    
    @Override
    public boolean isCancelable()
    {
        return false;
    }
}
