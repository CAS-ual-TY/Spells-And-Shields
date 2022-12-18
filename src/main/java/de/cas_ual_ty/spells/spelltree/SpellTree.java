package de.cas_ual_ty.spells.spelltree;

import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.icon.SpellIcon;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ContainerLevelAccess;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class SpellTree
{
    private SpellNode root;
    private Component title;
    private Holder<Spell> icon;
    private List<Requirement> requirements;
    
    private ResourceLocation id;
    
    public SpellTree(SpellNode root, Component title, Holder<Spell> icon, List<Requirement> requirements)
    {
        this.root = root;
        this.title = title;
        this.icon = icon;
        this.requirements = requirements;
        
        id = null;
    }
    
    public SpellTree(SpellNode root, Component title, Holder<Spell> icon)
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
    
    public Component getTitle()
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
    
    public Holder<Spell> getIconSpell()
    {
        return icon;
    }
    
    public Spell getIconSpellDirect()
    {
        return getIconSpell().get();
    }
    
    public SpellIcon getIcon()
    {
        return getIconSpellDirect().getIcon();
    }
    
    public List<Requirement> getRequirements()
    {
        return requirements;
    }
    
    public ResourceLocation getId()
    {
        return id;
    }
    
    public SpellTree setId(ResourceLocation id)
    {
        this.id = id;
        return this;
    }
    
    public SpellTree setRequirements(List<Requirement> requirements)
    {
        // used temporarily on client side for synced trees
        this.requirements = requirements;
        return this;
    }
    
    public int getDepth(Spell spell)
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
    
    private int find(int depth, SpellNode spellNode, Spell spell)
    {
        if(spellNode.getSpellDirect() == spell)
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
    
    public void assignNodeIds(ResourceLocation spellTreeId)
    {
        setId(spellTreeId);
        
        AtomicInteger i = new AtomicInteger(0);
        forEach(spellNode -> spellNode.setId(spellTreeId, i.getAndIncrement()));
    }
    
    public SpellNode findNode(int id)
    {
        Stack<SpellNode> stack = new Stack<>();
        stack.push(root);
        
        while(!stack.isEmpty())
        {
            SpellNode node = stack.pop();
            
            if(node.getId().nodeId() == id)
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
        return new SpellTree(innerDeepCopy(root), title, icon, requirements).setId(id);
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
    
    public static Builder builder(Component title, Holder<Spell> root, int levelCost, Requirement... requirements)
    {
        return new Builder(title, root, levelCost, List.of(requirements));
    }
    
    public static Builder builder(Component title, Holder<Spell> root, int levelCost, List<Requirement> requirements)
    {
        return new Builder(title, root, levelCost, requirements);
    }
    
    public static Builder builder(Component title, SpellNode root)
    {
        return new Builder(title, root);
    }
    
    public static class Builder
    {
        private Component title;
        private SpellNode root;
        private Holder<Spell> icon;
        private List<Requirement> treeRequirements;
        
        private Stack<SpellNode> stack;
        
        private Builder(Component title, SpellNode root)
        {
            this.title = title;
            this.root = root;
            icon = null;
            treeRequirements = new LinkedList<>();
            
            this.stack = new Stack<>();
            this.stack.push(this.root);
        }
        
        private Builder(Component title, Holder<Spell> root, int levelCost, List<Requirement> requirements)
        {
            this(title, new SpellNode(new SpellInstance(root), levelCost, requirements));
        }
        
        public Builder requirement(Requirement requirement)
        {
            treeRequirements.add(requirement);
            return this;
        }
        
        public Builder add(Holder<Spell> spell, int levelCost, Requirement... requirements)
        {
            return add(new SpellNode(new SpellInstance(spell), levelCost, List.of(requirements)));
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
        
        public Builder icon(Holder<Spell> spell)
        {
            this.icon = spell;
            return this;
        }
        
        public SpellTree finish()
        {
            return new SpellTree(root, title, icon != null ? icon : root.getSpellInstance().getSpell(), treeRequirements);
        }
    }
}
