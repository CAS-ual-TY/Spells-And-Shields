package de.cas_ual_ty.spells.spelltree;

import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.icon.SpellIcon;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ContainerLevelAccess;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

public class SpellTree
{
    private SpellNode root;
    private Component title;
    private SpellIcon icon;

    private ResourceLocation clientId;

    public SpellTree(SpellNode root, Component title, SpellIcon icon)
    {
        this.root = root;
        this.title = title;
        this.icon = icon;
    }

    public SpellTree()
    {
        // only to be used by the codecs
        this(null, null, null);
    }
    
    public ResourceLocation getId(Registry<SpellTree> registry)
    {
        return registry.getKey(this);
    }

    public ResourceLocation getClientId()
    {
        return clientId;
    }

    public SpellTree setClientId(ResourceLocation clientId)
    {
        this.clientId = clientId;
        return this;
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
        getRequirements().forEach(requirement -> requirement.makeDescription(tooltips, spellProgressionHolder, access));
        return tooltips;
    }
    
    public SpellIcon getIcon()
    {
        return icon;
    }
    
    public List<Requirement> getRequirements()
    {
        return root.getHiddenRequirements();
    }

    @Nullable
    public SpellNode findNode(ResourceLocation id)
    {
        Stack<SpellNode> stack = new Stack<>();
        stack.push(root);
        
        while(!stack.isEmpty())
        {
            SpellNode node = stack.pop();
            
            if(node.nodeId.equals(id))
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
        return new SpellTree(innerDeepCopy(root), title, icon);
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
        private SpellIcon icon;
        
        private Stack<SpellNode> stack;
        
        private Builder(Component title)
        {
            this.title = title;

            root = null;
            icon = null;
            
            stack = new Stack<>();
        }
        
        public Builder icon(SpellIcon spell)
        {
            icon = spell;
            return this;
        }
        
        public Builder add(ResourceLocation nodeId, Holder<Spell> spell)
        {
            return add(new SpellNode(nodeId, SpellInstance.direct(spell)));
        }

        public Builder add(Holder<Spell> spell)
        {
            return add(new SpellNode(spell.unwrapKey().map(ResourceKey::location).orElseThrow(IllegalArgumentException::new), SpellInstance.direct(spell)));
        }
        
        public Builder add(SpellNode spellNode)
        {
            if(!stack.isEmpty())
            {
                for(SpellNode n : stack)
                {
                    if(n.nodeId.equals(spellNode.nodeId))
                    {
                        throw new IllegalStateException();
                    }
                }

                SpellNode parent = stack.peek();
                connect(parent, spellNode);
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
        
        public Builder frame(int frame)
        {
            stack.peek().setFrame(Mth.clamp(frame, 0, 2));
            return this;
        }
        
        public Builder manaCost(float manaCost)
        {
            stack.peek().getSpellInstance().setManaCost(manaCost);
            return this;
        }
        
        public Builder noManaCost()
        {
            return manaCost(0);
        }
        
        public Builder addParameter(CtxVar<?> ctxVar)
        {
            stack.peek().getSpellInstance().addParameter(ctxVar);
            return this;
        }
        
        public Builder goalFrame()
        {
            return frame(2);
        }
        
        public Builder challengeFrame()
        {
            return frame(1);
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
            return new SpellTree(root, title, icon != null ? icon : root.getSpellInstance().getSpell().value().getIcon());
        }
    }
}
