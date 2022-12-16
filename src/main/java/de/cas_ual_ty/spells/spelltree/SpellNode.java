package de.cas_ual_ty.spells.spelltree;

import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.spell.NewSpell;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerLevelAccess;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class SpellNode
{
    protected final Holder<NewSpell> spell;
    protected int levelCost;
    protected List<Requirement> requirements;
    
    protected SpellNode parent;
    protected List<SpellNode> children;
    
    protected int id;
    
    public SpellNode(Holder<NewSpell> spell, int levelCost, List<Requirement> requirements, List<SpellNode> children)
    {
        this.spell = spell;
        this.levelCost = Math.max(0, levelCost);
        this.requirements = requirements;
        this.children = children;
        this.id = 0;
    }
    
    public SpellNode(Holder<NewSpell> spell, int levelCost, List<Requirement> requirements, int id)
    {
        this.spell = spell;
        this.levelCost = Math.max(0, levelCost);
        this.requirements = requirements;
        children = new LinkedList<>();
        this.id = id;
    }
    
    public SpellNode(Holder<NewSpell> spell, int levelCost, List<Requirement> requirements)
    {
        this(spell, levelCost, requirements, -1);
    }
    
    public Holder<NewSpell> getSpell()
    {
        return spell;
    }
    
    public int getLevelCost()
    {
        return this.levelCost;
    }
    
    public void setLevelCost(int levelCost)
    {
        this.levelCost = levelCost;
    }
    
    public List<Requirement> getRequirements()
    {
        return requirements;
    }
    
    public void addRequirement(Requirement requirement)
    {
        this.requirements.add(requirement);
    }
    
    public void setRequirements(List<Requirement> requirements)
    {
        this.requirements = requirements;
    }
    
    public NewSpell getSpellDirect()
    {
        return spell.get();
    }
    
    public boolean passes(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return requirements.stream().allMatch(requirement -> requirement.passes(spellProgressionHolder, access));
    }
    
    public void onSpellLearned(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        requirements.forEach(requirement -> requirement.onSpellLearned(spellProgressionHolder, access));
    }
    
    public boolean canLearn(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return spellProgressionHolder.getPlayer().isCreative() || (spellProgressionHolder.getPlayer().experienceLevel >= this.levelCost && passes(spellProgressionHolder, access));
    }
    
    public List<Component> getTooltip(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        List<Component> tooltips = new LinkedList<>();
        tooltips.add(getSpellDirect().getTitle());
        requirements.forEach(requirement -> tooltips.add(requirement.makeDescription(spellProgressionHolder, access)));
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
        return new SpellNode(spell, levelCost, requirements);
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
    
    public int getId()
    {
        return id;
    }
    
    public void setId(int id)
    {
        this.id = id;
    }
}
