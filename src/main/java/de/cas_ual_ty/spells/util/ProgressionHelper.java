package de.cas_ual_ty.spells.util;

import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.event.AvailableSpellTreesEvent;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.requirement.WrappedRequirement;
import de.cas_ual_ty.spells.spell.ISpell;
import de.cas_ual_ty.spells.spelltree.SpellNode;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraftforge.common.MinecraftForge;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ProgressionHelper
{
    public static List<SpellTree> stripSpellTrees(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access, List<SpellTree> allAvailableSkillTrees)
    {
        List<SpellTree> strippedSkillTrees = new LinkedList<>();
        
        for(SpellTree spellTree0 : allAvailableSkillTrees)
        {
            if(spellTree0.getRoot() == null)
            {
                continue;
            }
            
            if(!spellTree0.passes(spellProgressionHolder, access))
            {
                continue;
            }
            
            SpellTree stripped = spellTree0.copy();
            
            List<SpellNode> visibleNodes = new LinkedList<>();
            
            // add all active or previously bought spells
            stripped.forEach(spellNode ->
            {
                if(spellProgressionHolder.getSpellStatus(spellNode.getSpell()).isVisible())
                {
                    visibleNodes.add(spellNode);
                }
            });
            
            // add root
            if(!visibleNodes.contains(stripped.getRoot()))
            {
                visibleNodes.add(stripped.getRoot());
                
                if(visibleNodes.size() == 1)
                {
                    stripped.getRoot().getChildren().clear();
                    strippedSkillTrees.add(stripped);
                    continue;
                }
            }
            
            // add all spells above any visible spell
            for(SpellNode spellNode : visibleNodes.stream().toList())
            {
                SpellNode parent;
                
                while((parent = spellNode.getParent()) != null && !visibleNodes.contains(parent))
                {
                    visibleNodes.add(parent);
                    spellNode = parent;
                }
            }
            
            List<SpellNode> invisibleNodes = new LinkedList<>();
            
            // remove all invisible grandchildren of leaves of the visible tree
            stripped.forEach(spellNode ->
            {
                if(!visibleNodes.contains(spellNode))
                {
                    boolean fullyLinked = ProgressionHelper.isFullyLinked(spellNode, spellProgressionHolder.getProgression());
                    
                    if(fullyLinked)
                    {
                        spellNode.getChildren().clear();
                    }
                    else
                    {
                        invisibleNodes.add(spellNode);
                    }
                }
            });
            
            invisibleNodes.forEach(spellNode -> spellNode.getParent().getChildren().remove(spellNode));
            
            strippedSkillTrees.add(stripped);
        }
        
        strippedSkillTrees.forEach(tree ->
        {
            tree.forEach(node ->
            {
                node.setRequirements(node.getRequirements().stream().map(r -> WrappedRequirement.wrap(r, spellProgressionHolder, access)).collect(Collectors.toList()));
            });
            
            tree.setRequirements(tree.getRequirements().stream().map(r -> WrappedRequirement.wrap(r, spellProgressionHolder, access)).collect(Collectors.toList()));
        });
        
        return strippedSkillTrees;
    }
    
    public static List<SpellTree> getStrippedSpellTrees(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        List<SpellTree> allAvailableSkillTrees = getAllAvailableSpellTrees(spellProgressionHolder, access);
        
        List<SpellTree> availableSpellTrees = stripSpellTrees(spellProgressionHolder, access, allAvailableSkillTrees);
        availableSpellTrees.forEach(SpellTree::assignNodeIds);
        
        return availableSpellTrees;
    }
    
    public static List<SpellTree> getAllAvailableSpellTrees(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess containerLevelAccess)
    {
        List<SpellTree> availableSpellTrees = new LinkedList<>();
        
        AvailableSpellTreesEvent event = new AvailableSpellTreesEvent(spellProgressionHolder.getPlayer(), spellProgressionHolder, availableSpellTrees);
        MinecraftForge.EVENT_BUS.post(event);
        
        return availableSpellTrees;
    }
    
    public static boolean isFullyLinked(SpellNode spellNode, Map<ISpell, SpellStatus> progression)
    {
        SpellNode parent = spellNode;
        
        while((parent = parent.getParent()) != null)
        {
            if(!progression.getOrDefault(parent.getSpell(), SpellStatus.LOCKED).isAvailable())
            {
                return false;
            }
        }
        
        return true;
    }
    
    public static boolean tryBuySpell(SpellProgressionHolder spellProgressionHolder, SpellProgressionMenu menu, int id, ISpell spell, UUID uuid)
    {
        Player player = menu.player;
        
        AtomicBoolean found = new AtomicBoolean(false);
        
        menu.spellTrees.stream().filter(tree -> tree.getId().equals(uuid)).findFirst().ifPresent(spellTree ->
        {
            SpellNode spellNode = spellTree.findNode(id);
            
            if(spellNode.getSpell() == spell && ((spellNode.passes(spellProgressionHolder, menu.access) && player.experienceLevel >= spellNode.getLevelCost()) || player.isCreative()))
            {
                found.set(true);
                player.giveExperienceLevels(-spellNode.getLevelCost());
            }
        });
        
        return found.get();
    }
}
