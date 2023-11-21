package de.cas_ual_ty.spells.spelltree;

import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ContainerLevelAccess;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class SpellNode
{
    protected SpellNodeId nodeId;
    
    protected final SpellInstance spell;
    protected int levelCost;
    protected List<Requirement> hiddenRequirements;
    protected List<Requirement> learnRequirements;
    protected int frame;
    
    protected SpellNode parent;
    protected List<SpellNode> children;
    
    public SpellNode(SpellNodeId nodeId, SpellInstance spell, int levelCost, List<Requirement> hiddenRequirements, List<Requirement> learnRequirements, List<SpellNode> children, int frame)
    {
        this.nodeId = nodeId;
        this.spell = spell;
        this.levelCost = Math.max(0, levelCost);
        this.hiddenRequirements = hiddenRequirements;
        this.learnRequirements = learnRequirements;
        this.children = children;
        this.frame = frame;
    }
    
    public SpellNode(SpellNodeId nodeId, SpellInstance spell, int levelCost, List<Requirement> hiddenRequirements, List<Requirement> learnRequirements, int frame)
    {
        this(nodeId, spell, levelCost, hiddenRequirements, learnRequirements, new LinkedList<>(), frame);
    }
    
    public SpellNode(int nodeId, SpellInstance spell, int levelCost, List<Requirement> hiddenRequirements, List<Requirement> learnRequirements, int frame)
    {
        this(new SpellNodeId(null, nodeId), spell, levelCost, hiddenRequirements, learnRequirements, new LinkedList<>(), frame);
    }
    
    public SpellNode(int nodeId, SpellInstance spell)
    {
        this(new SpellNodeId(null, nodeId), spell, 0, new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), 0);
    }
    
    public SpellNode(SpellInstance spell, int levelCost, List<Requirement> hiddenRequirements, List<Requirement> learnRequirements, int frame)
    {
        this(null, spell, levelCost, hiddenRequirements, learnRequirements, frame);
    }
    
    public SpellNode(SpellInstance spell)
    {
        this(null, spell, 0, new LinkedList<>(), new LinkedList<>(), 0);
    }
    
    public SpellInstance getSpellInstance()
    {
        return spell;
    }
    
    public int getLevelCost()
    {
        return levelCost;
    }
    
    public void setLevelCost(int levelCost)
    {
        this.levelCost = levelCost;
    }
    
    public int getFrame()
    {
        return frame;
    }
    
    public void setFrame(int frame)
    {
        this.frame = frame;
    }
    
    public List<Requirement> getHiddenRequirements()
    {
        return hiddenRequirements;
    }
    
    public List<Requirement> getLearnRequirements()
    {
        return learnRequirements;
    }
    
    public void addHiddenRequirement(Requirement requirement)
    {
        hiddenRequirements.add(requirement);
    }
    
    public void addLearnRequirement(Requirement requirement)
    {
        learnRequirements.add(requirement);
    }
    
    public void setHiddenRequirements(List<Requirement> requirements)
    {
        hiddenRequirements = requirements;
    }
    
    public void setLearnRequirements(List<Requirement> requirements)
    {
        learnRequirements = requirements;
    }
    
    public Spell getSpellDirect()
    {
        return spell.getSpell().value();
    }
    
    public boolean passesHidden(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return hiddenRequirements.stream().allMatch(requirement -> requirement.passes(spellProgressionHolder, access));
    }
    
    public boolean passesLearn(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return hiddenRequirements.stream().allMatch(requirement -> requirement.passes(spellProgressionHolder, access)) && learnRequirements.stream().allMatch(requirement -> requirement.passes(spellProgressionHolder, access));
    }
    
    public void onSpellLearned(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        hiddenRequirements.forEach(requirement -> requirement.onSpellLearned(spellProgressionHolder, access));
        learnRequirements.forEach(requirement -> requirement.onSpellLearned(spellProgressionHolder, access));
    }
    
    public boolean canSee(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return passesHidden(spellProgressionHolder, access);
    }
    
    public boolean canLearn(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return spellProgressionHolder.getPlayer().experienceLevel >= levelCost && passesLearn(spellProgressionHolder, access);
    }
    
    public List<Component> getTooltip(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        List<Component> tooltips = new LinkedList<>();
        tooltips.add(getSpellDirect().getTitle());
        hiddenRequirements.stream().map(requirement -> (requirement.makeDescription(spellProgressionHolder, access))).filter(c -> c.getContents() != ComponentContents.EMPTY).forEach(tooltips::add);
        learnRequirements.stream().map(requirement -> (requirement.makeDescription(spellProgressionHolder, access))).filter(c -> c.getContents() != ComponentContents.EMPTY).forEach(tooltips::add);
        return tooltips;
    }
    
    public void setParent(@Nullable SpellNode parent)
    {
        this.parent = parent;
    }
    
    public void addChild(SpellNode child)
    {
        children.add(child);
    }
    
    public SpellNode copy()
    {
        return new SpellNode(nodeId, spell.copy(), levelCost, hiddenRequirements, learnRequirements, frame);
    }
    
    @Nullable
    public SpellNode getParent()
    {
        return parent;
    }
    
    public List<SpellNode> getChildren()
    {
        return children;
    }
    
    public SpellNodeId getNodeId()
    {
        return nodeId;
    }
    
    public void setNodeId(ResourceLocation sourceTree, int id)
    {
        if(nodeId != null)
        {
            nodeId = new SpellNodeId(sourceTree, nodeId.nodeId());
        }
        else
        {
            nodeId = new SpellNodeId(sourceTree, id);
        }
        spell.initId(nodeId);
    }
}
