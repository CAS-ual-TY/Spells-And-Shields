package de.cas_ual_ty.spells.util;

import de.cas_ual_ty.spells.Spells;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.requirement.RequirementType;
import de.cas_ual_ty.spells.spell.ISpell;
import de.cas_ual_ty.spells.spelltree.SpellNode;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedList;
import java.util.List;

public class SpellTreeSerializer
{
    private static byte TYPE_FINISH = 0;
    private static byte TYPE_SPELL = 1;
    private static byte TYPE_UP = 2;
    
    public static void encodeTree(SpellTree spellTree, FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(spellTree.getClientId());
        buf.writeComponent(spellTree.getTitle());
        buf.writeRegistryId(Spells.SPELLS_REGISTRY.get(), spellTree.getIconSpell());
        encodeRequirements(spellTree.getRequirements(), buf);
        SpellNode spellNode = spellTree.getRoot();
        encodeNode(spellNode, buf);
        
        for(SpellNode child : spellNode.getChildren())
        {
            encodeTreeRec(child, buf);
        }
        
        buf.writeByte(TYPE_FINISH);
    }
    
    private static void encodeTreeRec(SpellNode spellNode, FriendlyByteBuf buf)
    {
        buf.writeByte(TYPE_SPELL);
        encodeNode(spellNode, buf);
        
        for(SpellNode child : spellNode.getChildren())
        {
            encodeTreeRec(child, buf);
        }
        
        buf.writeByte(TYPE_UP);
    }
    
    private static void encodeRequirements(List<Requirement> list, FriendlyByteBuf buf)
    {
        buf.writeInt(list.size());
        list.forEach(requirement -> RequirementType.writeToBuf(buf, requirement));
    }
    
    private static void encodeNode(SpellNode spellNode, FriendlyByteBuf buf)
    {
        buf.writeRegistryId(Spells.SPELLS_REGISTRY.get(), spellNode.getSpell());
        buf.writeInt(spellNode.getLevelCost());
        encodeRequirements(spellNode.getRequirements(), buf);
        buf.writeShort(spellNode.getId());
    }
    
    public static SpellTree decodeTree(FriendlyByteBuf buf)
    {
        ResourceLocation id = buf.readResourceLocation();
        MutableComponent title = (MutableComponent) buf.readComponent();
        ISpell icon = buf.readRegistryId();
        List<Requirement> requirements = decodeRequirements(buf);
        
        SpellTree.Builder builder = SpellTree.builder(title, decodeNode(buf));
        
        byte next;
        
        while((next = buf.readByte()) != TYPE_FINISH)
        {
            if(next == TYPE_SPELL)
            {
                builder.add(decodeNode(buf));
            }
            else if(next == TYPE_UP)
            {
                builder.leaf();
            }
            else
            {
                throw new IllegalStateException();
            }
        }
        
        return builder.icon(icon).finish().setRequirements(requirements).setId(id);
    }
    
    private static List<Requirement> decodeRequirements(FriendlyByteBuf buf)
    {
        int size = buf.readInt();
        List<Requirement> list = new LinkedList<>();
        
        for(int i = 0; i < size; i++)
        {
            list.add(RequirementType.readFromBuf(buf));
        }
        
        return list;
    }
    
    public static SpellNode decodeNode(FriendlyByteBuf buf)
    {
        ISpell spell = buf.readRegistryId();
        int levelCost = buf.readInt();
        List<Requirement> requirements = decodeRequirements(buf);
        int id = buf.readShort();
        
        return new SpellNode(spell, levelCost, requirements, id);
    }
}
