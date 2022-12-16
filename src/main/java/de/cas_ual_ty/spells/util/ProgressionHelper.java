package de.cas_ual_ty.spells.util;

import de.cas_ual_ty.spells.SpellTrees;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.requirement.WrappedRequirement;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spelltree.SpellNode;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ProgressionHelper
{
    public static List<SpellTree> stripSpellTrees(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access, Registry<SpellTree> registry, List<SpellTree> allAvailableSkillTrees)
    {
        List<SpellTree> strippedSkillTrees = new LinkedList<>();
        
        for(SpellTree spellTree0 : allAvailableSkillTrees)
        {
            if(spellTree0.getRoot() == null)
            {
                continue;
            }
            
            if(!spellTree0.canSee(spellProgressionHolder, access))
            {
                continue;
            }
            
            SpellTree stripped = spellTree0.copyWithId(registry);
            
            List<SpellNode> visibleNodes = new LinkedList<>();
            
            // add all active or previously bought spells
            stripped.forEach(spellNode ->
            {
                if(spellProgressionHolder.getSpellStatus(spellNode.getSpellDirect()).isVisible())
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
        Registry<SpellTree> registry = SpellTrees.getRegistry(spellProgressionHolder.getPlayer().level);
        ArrayList<SpellTree> allAvailableSkillTrees = new ArrayList<>(registry.size());
        registry.forEach(allAvailableSkillTrees::add);
        
        List<SpellTree> availableSpellTrees = stripSpellTrees(spellProgressionHolder, access, registry, allAvailableSkillTrees);
        availableSpellTrees.forEach(SpellTree::assignNodeIds);
        return availableSpellTrees;
    }
    
    public static boolean isFullyLinked(SpellNode spellNode, Map<Spell, SpellStatus> progression)
    {
        SpellNode parent = spellNode;
        
        while((parent = parent.getParent()) != null)
        {
            if(!progression.getOrDefault(parent.getSpellDirect(), SpellStatus.LOCKED).isAvailable())
            {
                return false;
            }
        }
        
        return true;
    }
    
    public static boolean tryBuySpell(SpellProgressionHolder spellProgressionHolder, SpellProgressionMenu menu, int id, Spell spell, ResourceLocation treeId)
    {
        Player player = menu.player;
        
        AtomicBoolean found = new AtomicBoolean(false);
        
        Registry<SpellTree> registry = SpellTrees.getRegistry(spellProgressionHolder.getPlayer().level);
        
        menu.spellTrees.stream().filter(tree -> tree.getClientId().equals(treeId)).findFirst().ifPresent(spellTree ->
        {
            SpellNode spellNode = spellTree.findNode(id);
            
            if(spellNode.getSpellDirect() == spell && spellNode.canLearn(spellProgressionHolder, menu.access))
            {
                found.set(true);
                
                if(!player.isCreative())
                {
                    player.giveExperienceLevels(-spellNode.getLevelCost());
                }
                
                spellNode.onSpellLearned(spellProgressionHolder, menu.access);
            }
        });
        
        return found.get();
    }
}
