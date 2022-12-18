package de.cas_ual_ty.spells.spelltree;

import de.cas_ual_ty.spells.spell.SpellInstance;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public record SpellNodeId(ResourceLocation treeId, int nodeId)
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
    
    public void toNbt(CompoundTag nbt)
    {
        nbt.putString("treeId", treeId().toString());
        nbt.putInt("nodeId", nodeId());
    }
    
    @Nullable
    public static SpellNodeId fromNbt(CompoundTag nbt)
    {
        if(!nbt.contains("treeId") || !nbt.contains("nodeId"))
        {
            return null;
        }
        
        ResourceLocation treeId = new ResourceLocation(nbt.getString("treeId"));
        int nodeId = nbt.getInt("nodeId");
        return new SpellNodeId(treeId, nodeId);
    }
    
    public void toBuf(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(treeId());
        buf.writeShort(nodeId());
    }
    
    public static SpellNodeId fromBuf(FriendlyByteBuf buf)
    {
        ResourceLocation treeId = buf.readResourceLocation();
        int nodeId = buf.readShort();
        return new SpellNodeId(treeId, nodeId);
    }
}
