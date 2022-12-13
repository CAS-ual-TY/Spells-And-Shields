package de.cas_ual_ty.spells.spelltree;

import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.spell.ISpell;
import de.cas_ual_ty.spells.spell.base.SpellIcon;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ContainerLevelAccess;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SpellTree
{
    private SpellNode root;
    private MutableComponent title;
    private ISpell icon;
    private List<Requirement> requirements;
    
    private ResourceLocation id;
    
    public SpellTree(SpellNode root, MutableComponent title, ISpell icon, List<Requirement> requirements)
    {
        this.root = root;
        this.title = title;
        this.icon = icon;
        this.requirements = requirements;
        
        id = null;
        
        if(!(title.getContents() instanceof TranslatableContents))
        {
            throw new IllegalArgumentException("SpellTree: 'title' constructor param must be a translatable-component");
        }
    }
    
    public SpellTree(SpellNode root, MutableComponent title, ISpell icon)
    {
        this(root, title, icon, new LinkedList<>());
    }
    
    public ResourceLocation getId(Registry<SpellTree> registry)
    {
        return registry.getKey(this);
    }
    
    public SpellNode getRoot()
    {
        return root;
    }
    
    public MutableComponent getTitle()
    {
        return title;
    }
    
    public List<Component> getTooltip(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        List<Component> tooltips = new LinkedList<>();
        tooltips.add(getTitle());
        requirements.forEach(requirement -> tooltips.add(requirement.makeDescription(spellProgressionHolder, access)));
        return tooltips;
    }
    
    public ISpell getIconSpell()
    {
        return icon;
    }
    
    public SpellIcon getIcon()
    {
        return getIconSpell().getIcon();
    }
    
    public List<Requirement> getRequirements()
    {
        return requirements;
    }
    
    public ResourceLocation getClientId()
    {
        // used temporarily on client side for synced trees
        return id;
    }
    
    public SpellTree setId(ResourceLocation id)
    {
        // used temporarily on client side for synced trees
        this.id = id;
        return this;
    }
    
    public SpellTree setRequirements(List<Requirement> requirements)
    {
        // used temporarily on client side for synced trees
        this.requirements = requirements;
        return this;
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
        return requirements.stream().allMatch(requirement -> requirement.passes(spellProgressionHolder, access));
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
    
    protected SpellTree copy() // deep copy
    {
        return new SpellTree(innerDeepCopy(root), title, icon, requirements);
    }
    
    public SpellTree copyWithId(Registry<SpellTree> registry) // deep copy
    {
        return copy().setId(getId(registry));
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
    
    public static Builder builder(MutableComponent title, Supplier<ISpell> root, int levelCost, Requirement... requirements)
    {
        return new Builder(title, root, levelCost, List.of(requirements));
    }
    
    public static Builder builder(MutableComponent title, ISpell root, int levelCost, Requirement... requirements)
    {
        return new Builder(title, root, levelCost, List.of(requirements));
    }
    
    public static Builder builder(MutableComponent title, Supplier<ISpell> root, int levelCost, List<Requirement> requirements)
    {
        return new Builder(title, root, levelCost, requirements);
    }
    
    public static Builder builder(MutableComponent title, ISpell root, int levelCost, List<Requirement> requirements)
    {
        return new Builder(title, root, levelCost, requirements);
    }
    
    public static Builder builder(MutableComponent title, SpellNode root)
    {
        return new Builder(title, root);
    }
    
    public static class Builder
    {
        private MutableComponent title;
        private SpellNode root;
        private ISpell icon;
        private List<Requirement> treeRequirements;
        
        private Stack<SpellNode> stack;
        
        private Builder(MutableComponent title, SpellNode root)
        {
            this.title = title;
            this.root = root;
            icon = null;
            treeRequirements = new LinkedList<>();
            
            this.stack = new Stack<>();
            this.stack.push(this.root);
        }
        
        private Builder(MutableComponent title, ISpell root, int levelCost, List<Requirement> requirements)
        {
            this(title, new SpellNode(root, levelCost, requirements));
        }
        
        public Builder(MutableComponent title, Supplier<ISpell> root, int levelCost, List<Requirement> requirements)
        {
            this(title, root.get(), levelCost, requirements);
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
            return new SpellTree(root, title, icon != null ? icon : root.getSpell(), treeRequirements);
        }
    }
}
