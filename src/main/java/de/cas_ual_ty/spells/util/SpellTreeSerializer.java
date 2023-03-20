package de.cas_ual_ty.spells.util;

import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.requirement.RequirementType;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spelltree.SpellNode;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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
        buf.writeResourceLocation(spellTree.getId());
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
        buf.writeResourceLocation(spellNode.getSpellInstance().getSpell().unwrap().map(ResourceKey::location, registry::getKey));
        buf.writeInt(spellNode.getLevelCost());
        encodeRequirements(spellNode.getRequirements(), buf);
        buf.writeResourceLocation(spellNode.getId().treeId());
        buf.writeShort(spellNode.getId().nodeId());
    }
    
    public static SpellTree decodeTree(Registry<Spell> registry, FriendlyByteBuf buf)
    {
        ResourceLocation id = buf.readResourceLocation();
        Component title = buf.readComponent();
        Holder<Spell> icon = registry.getHolderOrThrow(ResourceKey.create(Spells.REGISTRY_KEY, buf.readResourceLocation()));
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
    
    public static SpellNode decodeNode(Registry<Spell> registry, FriendlyByteBuf buf)
    {
        Holder<Spell> spell = registry.getHolderOrThrow(ResourceKey.create(Spells.REGISTRY_KEY, buf.readResourceLocation()));
        int levelCost = buf.readInt();
        List<Requirement> requirements = decodeRequirements(buf);
        SpellNodeId id = new SpellNodeId(buf.readResourceLocation(), buf.readShort());
        
        return new SpellNode(new SpellInstance(spell), levelCost, requirements, id);
    }
}
