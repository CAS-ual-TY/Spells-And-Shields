package de.cas_ual_ty.spells.spelltree;

import de.cas_ual_ty.spells.spell.SpellInstance;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public record FullSpellNodeId(ResourceLocation treeId, ResourceLocation nodeId)
{
    public SpellTree getSpellTree(Registry<SpellTree> registry)
    {
        return registry.get(treeId);
    }
    
    public SpellNode getSpellNode(Registry<SpellTree> registry)
    {
        SpellTree tree = getSpellTree(registry);
        return tree.findNode(nodeId);
    }
    
    public SpellInstance getSpellInstance(Registry<SpellTree> registry)
    {
        SpellNode node = getSpellNode(registry);
        return node == null ? null : node.getSpellInstance();
    }
    
    public boolean isValid(Registry<SpellTree> registry)
    {
        SpellTree tree = getSpellTree(registry);
        
        if(tree == null)
        {
            return false;
        }
        
        return tree.findNode(nodeId) != null;
    }
    
    public String getIDText()
    {
        //return treeId + " " + nodeId;
        return nodeId.toString();
    }
    
    public void toNbt(CompoundTag nbt)
    {
        nbt.putString("treeId", treeId().toString());
        nbt.putString("nodeId", nodeId().toString());
    }
    
    @Nullable
    public static FullSpellNodeId fromNbt(CompoundTag nbt)
    {
        if(!nbt.contains("treeId") || !nbt.contains("nodeId"))
        {
            return null;
        }
        
        ResourceLocation treeId = ResourceLocation.parse(nbt.getString("treeId"));
        ResourceLocation nodeId = ResourceLocation.parse(nbt.getString("nodeId"));
        return new FullSpellNodeId(treeId, nodeId);
    }
    
    public void toBuf(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(treeId());
        buf.writeResourceLocation(nodeId());
    }

    public static FullSpellNodeId fromBuf(FriendlyByteBuf buf)
    {
        ResourceLocation treeId = buf.readResourceLocation();
        ResourceLocation nodeId = buf.readResourceLocation();
        return new FullSpellNodeId(treeId, nodeId);
    }
    
    @Override
    public String toString()
    {
        return treeId + "/" + nodeId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FullSpellNodeId other && (treeId.equals(other.treeId) && nodeId.equals(other.nodeId));
    }
}
