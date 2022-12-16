package de.cas_ual_ty.spells.util;

import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.requirement.RequirementType;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spelltree.SpellNode;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedList;
import java.util.List;

public class SpellTreeSerializer
{
    private static byte TYPE_FINISH = 0;
    private static byte TYPE_SPELL = 1;
    private static byte TYPE_UP = 2;
    
    public static void encodeTree(SpellTree spellTree, Registry<Spell> registry, FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(spellTree.getClientId());
        buf.writeComponent(spellTree.getTitle());
        buf.writeResourceLocation(spellTree.getIconSpell().unwrapKey().orElseThrow().location());
        encodeRequirements(spellTree.getRequirements(), buf);
        
        SpellNode spellNode = spellTree.getRoot();
        encodeNode(spellNode, registry, buf);
        
        for(SpellNode child : spellNode.getChildren())
        {
            encodeTreeRec(child, registry, buf);
        }
        
        buf.writeByte(TYPE_FINISH);
    }
    
    private static void encodeTreeRec(SpellNode spellNode, Registry<Spell> registry, FriendlyByteBuf buf)
    {
        buf.writeByte(TYPE_SPELL);
        encodeNode(spellNode, registry, buf);
        
        for(SpellNode child : spellNode.getChildren())
        {
            encodeTreeRec(child, registry, buf);
        }
        
        buf.writeByte(TYPE_UP);
    }
    
    private static void encodeRequirements(List<Requirement> list, FriendlyByteBuf buf)
    {
        buf.writeInt(list.size());
        list.forEach(requirement -> RequirementType.writeToBuf(buf, requirement));
    }
    
    private static void encodeNode(SpellNode spellNode, Registry<Spell> registry, FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(registry.getKey(spellNode.getSpellDirect()));
        buf.writeInt(spellNode.getLevelCost());
        encodeRequirements(spellNode.getRequirements(), buf);
        buf.writeShort(spellNode.getId());
    }
    
    public static SpellTree decodeTree(Registry<Spell> registry, FriendlyByteBuf buf)
    {
        ResourceLocation id = buf.readResourceLocation();
        Component title = buf.readComponent();
        Spell icon = registry.get(buf.readResourceLocation());
        List<Requirement> requirements = decodeRequirements(buf);
        
        SpellTree.Builder builder = SpellTree.builder(title, decodeNode(registry, buf));
        
        byte next;
        
        while((next = buf.readByte()) != TYPE_FINISH)
        {
            if(next == TYPE_SPELL)
            {
                builder.add(decodeNode(registry, buf));
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
        
        return builder.icon(Holder.direct(icon)).finish().setRequirements(requirements).setId(id);
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
    
    public static SpellNode decodeNode(Registry<Spell> registry, FriendlyByteBuf buf)
    {
        Spell spell = registry.get(buf.readResourceLocation());
        int levelCost = buf.readInt();
        List<Requirement> requirements = decodeRequirements(buf);
        int id = buf.readShort();
        
        return new SpellNode(Holder.direct(spell), levelCost, requirements, id);
    }
}
