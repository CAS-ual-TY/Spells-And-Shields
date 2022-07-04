package de.cas_ual_ty.spells.spell.tree;

import de.cas_ual_ty.spells.spell.base.ISpell;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SpellTree
{
    public UUID id;
    
    public SpellNode root;
    public Component title;
    public ISpell icon;
    
    @Nullable
    public SpellTreeClass treeClass;
    
    public SpellTree(UUID id, SpellNode root, Component title, ISpell icon)
    {
        this.id = id;
        this.root = root;
        this.title = title;
        this.icon = icon;
        this.treeClass = null;
    }
    
    public SpellTree(UUID id, SpellNode root, Component title)
    {
        this(id, root, title, root.getSpell());
    }
    
    public UUID getId()
    {
        return this.id;
    }
    
    public SpellNode getRoot()
    {
        return root;
    }
    
    public Component getTitle()
    {
        return title;
    }
    
    public void setTitle(Component title)
    {
        this.title = title;
    }
    
    public ISpell getIcon()
    {
        return icon;
    }
    
    public void setIcon(ISpell spell)
    {
        this.icon = spell;
    }
    
    public int getDepth(ISpell spell)
    {
        if(root == null)
        {
            return 0;
        }
        else
        {
            return find(1, root, spell);
        }
    }
    
    private int find(int depth, SpellNode spellNode, ISpell spell)
    {
        if(spellNode.getSpell() == spell)
        {
            return depth;
        }
        else
        {
            depth++;
            
            for(SpellNode child : spellNode.getChildren())
            {
                int found = find(depth, child, spell);
                
                if(found != 0)
                {
                    return found;
                }
            }
            
            return 0;
        }
    }
    
    public void forEach(Consumer<SpellNode> consumer)
    {
        if(root != null)
        {
            innerForEach(root, consumer);
        }
    }
    
    private void innerForEach(SpellNode spellNode, Consumer<SpellNode> consumer)
    {
        consumer.accept(spellNode);
        
        for(SpellNode child : spellNode.getChildren())
        {
            innerForEach(child, consumer);
        }
    }
    
    public SpellTree copy() // deep copy
    {
        return new SpellTree(id, innerDeepCopy(root), title.copy(), icon);
    }
    
    private SpellNode innerDeepCopy(SpellNode original)
    {
        SpellNode copy = original.copy();
        
        for(SpellNode child : original.getChildren())
        {
            connect(copy, innerDeepCopy(child));
        }
        
        return copy;
    }
    
    public static void connect(SpellNode parent, SpellNode child)
    {
        parent.addChild(child);
        child.setParent(parent);
    }
    
    public static Builder builder(UUID id, Supplier<ISpell> root, int levelCost, int requiredBookshelves, Component treeTitle)
    {
        return new Builder(id, root, levelCost, requiredBookshelves, treeTitle);
    }
    
    public static Builder builder(UUID id, ISpell root, int levelCost, int requiredBookshelves, Component treeTitle)
    {
        return new Builder(id, root, levelCost, requiredBookshelves, treeTitle);
    }
    
    public static Builder builder(UUID id, SpellNode root, Component treeTitle)
    {
        return new Builder(id, root, treeTitle);
    }
    
    public static class Builder
    {
        private UUID id;
        private Stack<SpellNode> stack;
        private SpellNode root;
        private Component title;
        private ISpell icon;
        
        private Builder(UUID id, SpellNode root, Component title)
        {
            this.id = id;
            this.stack = new Stack<>();
            this.root = root;
            this.title = title;
            icon = null;
            this.stack.push(this.root);
        }
        
        private Builder(UUID id, ISpell root, int levelCost, int requiredBookshelves, Component title)
        {
            this(id, new SpellNode(root, levelCost, requiredBookshelves), title);
        }
        
        public Builder(UUID id, Supplier<ISpell> root, int levelCost, int requiredBookshelves, Component title)
        {
            this(id, root.get(), levelCost, requiredBookshelves, title);
        }
        
        public Builder add(Supplier<ISpell> spell, int levelCost, int requiredBookshelves)
        {
            return add(spell.get(), levelCost, requiredBookshelves);
        }
        
        public Builder add(Supplier<ISpell> spell, int levelCost)
        {
            return add(spell.get(), levelCost, root.getRequiredBookshelves());
        }
        
        public Builder add(Supplier<ISpell> spell)
        {
            return add(spell, root.getLevelCost());
        }
        
        public Builder add(ISpell spell, int levelCost, int requiredBookshelves)
        {
            add(new SpellNode(spell, levelCost, requiredBookshelves));
            return this;
        }
        
        public Builder add(ISpell spell, int levelCost)
        {
            return add(spell, levelCost, root.getRequiredBookshelves());
        }
        
        public Builder add(ISpell spell)
        {
            return add(spell, root.getLevelCost());
        }
        
        public Builder add(SpellNode spellNode)
        {
            connect(stack.peek(), spellNode);
            stack.push(spellNode);
            return this;
        }
        
        public Builder leaf()
        {
            stack.pop();
            return this;
        }
        
        public Builder icon(ISpell spell)
        {
            this.icon = spell;
            return this;
        }
        
        public SpellTree finish()
        {
            return new SpellTree(id, root, title, icon != null ? icon : root.getSpell());
        }
    }
}
