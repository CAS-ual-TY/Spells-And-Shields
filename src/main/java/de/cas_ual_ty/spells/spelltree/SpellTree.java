package de.cas_ual_ty.spells.spelltree;

import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.spell.ISpell;
import de.cas_ual_ty.spells.spell.base.SpellIcon;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerLevelAccess;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SpellTree
{
    public UUID id;
    public String filename;
    
    public SpellNode root;
    public Component title;
    public ISpell icon;
    
    public List<Requirement> treeRequirements;
    
    public SpellTree(UUID id, SpellNode root, Component title, ISpell icon)
    {
        this.id = id;
        this.root = root;
        this.title = title;
        this.icon = icon;
        this.treeRequirements = new LinkedList<>();
    }
    
    public SpellTree(UUID id, SpellNode root, Component title)
    {
        this(id, root, title, root.getSpell());
    }
    
    public SpellTree setFilename(String filename)
    {
        this.filename = filename;
        return this;
    }
    
    public SpellTree setRequirements(List<Requirement> requirements)
    {
        treeRequirements = requirements;
        return this;
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
    
    public List<Component> getTooltip(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        List<Component> tooltips = new LinkedList<>();
        tooltips.add(getTitle());
        treeRequirements.forEach(requirement -> tooltips.add(requirement.makeDescription(spellProgressionHolder, access)));
        return tooltips;
    }
    
    public void setTitle(Component title)
    {
        this.title = title;
    }
    
    public ISpell getIconSpell()
    {
        return icon;
    }
    
    public SpellIcon getIcon()
    {
        return getIconSpell().getIcon();
    }
    
    public void setIcon(ISpell spell)
    {
        this.icon = spell;
    }
    
    public List<Requirement> getRequirements()
    {
        return treeRequirements;
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
    
    public boolean passes(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return treeRequirements.stream().allMatch(requirement -> requirement.passes(spellProgressionHolder, access));
    }
    
    public boolean canSee(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return spellProgressionHolder.getPlayer().isCreative() || passes(spellProgressionHolder, access);
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
    
    public SpellTree assignNodeIds()
    {
        AtomicInteger i = new AtomicInteger(0);
        forEach(spellNode -> spellNode.setId(i.getAndIncrement()));
        return this;
    }
    
    public SpellNode findNode(int id)
    {
        Stack<SpellNode> stack = new Stack<>();
        stack.push(root);
        
        while(!stack.isEmpty())
        {
            SpellNode node = stack.pop();
            
            if(node.getId() == id)
            {
                return node;
            }
            
            node.getChildren().forEach(stack::push);
        }
        
        return null;
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
        return new SpellTree(id, innerDeepCopy(root), title.copy(), icon).setFilename(filename).setRequirements(treeRequirements);
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
    
    public static Builder builder(UUID id, Component treeTitle, Supplier<ISpell> root, int levelCost, Requirement... requirements)
    {
        return new Builder(id, treeTitle, root, levelCost, requirements);
    }
    
    public static Builder builder(String filename, Component treeTitle, Supplier<ISpell> root, int levelCost, Requirement... requirements)
    {
        return new Builder(filename, treeTitle, root, levelCost, requirements);
    }
    
    public static Builder builder(UUID id, Component treeTitle, ISpell root, int levelCost, Requirement... requirements)
    {
        return new Builder(id, treeTitle, root, levelCost, requirements);
    }
    
    public static Builder builder(UUID id, Component treeTitle, SpellNode root)
    {
        return new Builder(id, treeTitle, root);
    }
    
    public static class Builder
    {
        private UUID id;
        private Component title;
        private Stack<SpellNode> stack;
        private SpellNode root;
        private ISpell icon;
        private String filename;
        private List<Requirement> treeRequirements;
        
        private Builder(UUID id, Component title, SpellNode root)
        {
            this.id = id;
            this.title = title;
            this.stack = new Stack<>();
            this.root = root;
            icon = null;
            this.stack.push(this.root);
            filename = null;
            treeRequirements = new LinkedList<>();
        }
        
        private Builder(String filename, Component title, SpellNode root)
        {
            this(SpellsUtil.generateUUIDForTree(filename), title, root);
            this.filename = filename;
        }
        
        private Builder(UUID id, Component title, ISpell root, int levelCost, Requirement... requirements)
        {
            this(id, title, new SpellNode(root, levelCost, List.of(requirements)));
        }
        
        private Builder(String filename, Component title, ISpell root, int levelCost, Requirement... requirements)
        {
            this(filename, title, new SpellNode(root, levelCost, List.of(requirements)));
        }
        
        public Builder(UUID id, Component title, Supplier<ISpell> root, int levelCost, Requirement... requirements)
        {
            this(id, title, root.get(), levelCost, requirements);
        }
        
        public Builder(String filename, Component title, Supplier<ISpell> root, int levelCost, Requirement... requirements)
        {
            this(filename, title, root.get(), levelCost, requirements);
        }
        
        public Builder requirement(Requirement requirement)
        {
            treeRequirements.add(requirement);
            return this;
        }
        
        public Builder add(Supplier<ISpell> spell, int levelCost, Requirement... requirements)
        {
            return add(spell.get(), levelCost, requirements);
        }
        
        public Builder add(ISpell spell, int levelCost, Requirement... requirements)
        {
            return add(new SpellNode(spell, levelCost, List.of(requirements)));
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
            return new SpellTree(id, root, title, icon != null ? icon : root.getSpell()).setFilename(filename).setRequirements(treeRequirements);
        }
    }
}
