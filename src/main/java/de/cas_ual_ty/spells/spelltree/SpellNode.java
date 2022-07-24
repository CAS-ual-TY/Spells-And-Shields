package de.cas_ual_ty.spells.spelltree;

import de.cas_ual_ty.spells.spell.ISpell;

import javax.annotation.Nullable;
import java.util.LinkedList;

public class SpellNode
{
    protected final ISpell spell;
    protected int levelCost;
    protected int requiredBookshelves;
    
    protected SpellNode parent;
    protected LinkedList<SpellNode> children;
    
    public SpellNode(ISpell spell, int levelCost, int requiredBookshelves)
    {
        this.spell = spell;
        this.levelCost = Math.max(0, levelCost);
        this.requiredBookshelves = Math.max(0, Math.min(32, requiredBookshelves));
        children = new LinkedList<>();
    }
    
    public ISpell getSpell()
    {
        return spell;
    }
    
    public int getLevelCost()
    {
        return this.levelCost;
    }
    
    public int getRequiredBookshelves()
    {
        return this.requiredBookshelves;
    }
    
    public void setLevelCost(int levelCost)
    {
        this.levelCost = levelCost;
    }
    
    public void setRequiredBookshelves(int requiredBookshelves)
    {
        this.requiredBookshelves = requiredBookshelves;
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
        return new SpellNode(spell, levelCost, requiredBookshelves);
    }
    
    @Nullable
    public SpellNode getParent()
    {
        return parent;
    }
    
    public LinkedList<SpellNode> getChildren()
    {
        return children;
    }
}
