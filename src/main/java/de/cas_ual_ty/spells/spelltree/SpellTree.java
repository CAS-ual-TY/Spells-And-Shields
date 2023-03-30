package de.cas_ual_ty.spells.spelltree;

import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.icon.SpellIcon;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ContainerLevelAccess;

import javax.annotation.Nullable;
import java.util.Arrays;
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
    
    private ResourceLocation id;
    
    public SpellTree(SpellNode root, Component title, Holder<Spell> icon)
    {
        this.root = root;
        this.title = title;
        this.icon = icon;
        
        id = null;
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
        getRequirements().forEach(requirement -> tooltips.add(requirement.makeDescription(spellProgressionHolder, access).withStyle(ChatFormatting.GRAY)));
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
        return root.getHiddenRequirements();
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
        forEach(spellNode -> spellNode.setNodeId(spellTreeId, i.getAndIncrement()));
    }
    
    @Nullable
    public SpellNode findNode(int id)
    {
        Stack<SpellNode> stack = new Stack<>();
        stack.push(root);
        
        while(!stack.isEmpty())
        {
            SpellNode node = stack.pop();
            
            if(node.getNodeId().nodeId() == id)
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
        return new SpellTree(innerDeepCopy(root), title, icon).setId(id);
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
    
    public static Builder builder(Component title)
    {
        return new Builder(title);
    }
    
    public static class Builder
    {
        private Component title;
        private SpellNode root;
        private Holder<Spell> icon;
        
        private Stack<SpellNode> stack;
        
        private Builder(Component title)
        {
            this.title = title;
            root = null;
            icon = null;
            
            this.stack = new Stack<>();
        }
        
        public Builder icon(Holder<Spell> spell)
        {
            this.icon = spell;
            return this;
        }
        
        public Builder add(int nodeId, Holder<Spell> spell)
        {
            return add(new SpellNode(nodeId, new SpellInstance(spell)));
        }
        
        public Builder add(Holder<Spell> spell)
        {
            return add(new SpellNode(new SpellInstance(spell)));
        }
        
        public Builder add(SpellNode spellNode)
        {
            if(!stack.isEmpty())
            {
                connect(stack.peek(), spellNode);
            }
            else
            {
                root = spellNode;
            }
            stack.push(spellNode);
            return this;
        }
        
        public Builder leaf()
        {
            stack.pop();
            return this;
        }
        
        public Builder levelCost(int levelCost)
        {
            stack.peek().setLevelCost(levelCost);
            return this;
        }
        
        public Builder hiddenRequirements(Requirement... requirements)
        {
            Arrays.stream(requirements).forEach(this::hiddenRequirement);
            return this;
        }
        
        private Builder hiddenRequirement(Requirement requirement)
        {
            stack.peek().addHiddenRequirement(requirement);
            return this;
        }
        
        public Builder learnRequirements(Requirement... requirements)
        {
            Arrays.stream(requirements).forEach(this::learnRequirement);
            return this;
        }
        
        private Builder learnRequirement(Requirement requirement)
        {
            stack.peek().addLearnRequirement(requirement);
            return this;
        }
        
        public SpellTree finish()
        {
            return new SpellTree(root, title, icon != null ? icon : root.getSpellInstance().getSpell());
        }
    }
}
