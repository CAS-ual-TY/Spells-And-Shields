package de.cas_ual_ty.spells.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.registers.SpellTrees;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spelltree.SpellNode;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerLevelAccess;

import java.util.List;

public class LearnedRequirement extends Requirement
{
    public static Codec<LearnedRequirement> makeCodec(RequirementType<LearnedRequirement> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("spell_tree").forGetter(r -> r.getNodeId().treeId()),
                Codec.INT.fieldOf("node_id").forGetter(r -> r.getNodeId().nodeId())
        ).apply(instance, (treeId, nodeId) -> new LearnedRequirement(type, new SpellNodeId(treeId, nodeId))));
    }
    
    public static final String ERROR_TREE_SUFFIX = ".error.tree";
    public static final String ERROR_NODE_SUFFIX = ".error.node";
    
    protected SpellNodeId nodeId;
    
    public LearnedRequirement(RequirementType<?> type)
    {
        super(type);
    }
    
    public LearnedRequirement(RequirementType<?> type, SpellNodeId nodeId)
    {
        this(type);
        this.nodeId = nodeId;
    }
    
    public SpellNodeId getNodeId()
    {
        return nodeId;
    }
    
    @Override
    protected boolean doesPlayerPass(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        if(spellProgressionHolder.getPlayer().level instanceof ServerLevel level)
        {
            return !nodeId.isValid(SpellTrees.getRegistry(level)) || spellProgressionHolder.getSpellStatus(nodeId) == SpellStatus.LEARNED;
        }
        
        return false;
    }
    
    @Override
    public void makeDescription(List<Component> tooltip, SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        if(spellProgressionHolder.getPlayer().level instanceof ServerLevel level)
        {
            SpellTree tree = nodeId.getSpellTree(SpellTrees.getRegistry(level));
            
            if(tree == null)
            {
                tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(descriptionId + ERROR_TREE_SUFFIX, nodeId.treeId())));
                return;
            }
            
            SpellNode node = tree.findNode(nodeId.nodeId());
            
            if(node == null)
            {
                tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(descriptionId + ERROR_NODE_SUFFIX, tree.getTitle(), nodeId.nodeId())));
                return;
            }
            
            SpellInstance spell = node.getSpellInstance();
            tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(descriptionId, spell.getSpell().value().getTitle(), tree.getTitle())));
        }
    }
    
    @Override
    public void writeToBuf(FriendlyByteBuf buf)
    {
        nodeId.toBuf(buf);
    }
    
    @Override
    public void readFromBuf(FriendlyByteBuf buf)
    {
        nodeId = SpellNodeId.fromBuf(buf);
    }
}