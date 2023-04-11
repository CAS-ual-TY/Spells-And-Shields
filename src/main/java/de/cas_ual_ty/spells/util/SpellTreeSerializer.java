package de.cas_ual_ty.spells.util;

import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.requirement.RequirementType;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.icon.SpellIcon;
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
        SpellIcon.iconToBuf(buf, spellTree.getIcon());
        
        SpellNode spellNode = spellTree.getRoot();
        encodeTreeRec(spellNode, registry, buf);
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
        encodeRequirements(spellNode.getHiddenRequirements(), buf);
        encodeRequirements(spellNode.getLearnRequirements(), buf);
        buf.writeResourceLocation(spellNode.getNodeId().treeId());
        buf.writeInt(spellNode.getNodeId().nodeId());
        buf.writeByte(spellNode.getFrame());
        buf.writeFloat(spellNode.getSpellInstance().getManaCost());
    }
    
    public static SpellTree decodeTree(Registry<Spell> registry, FriendlyByteBuf buf)
    {
        ResourceLocation id = buf.readResourceLocation();
        Component title = buf.readComponent();
        SpellIcon icon = SpellIcon.iconFromBuf(buf);
        
        SpellTree.Builder builder = SpellTree.builder(title);
        
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
        
        return builder.icon(icon).finish().setId(id);
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
        List<Requirement> hiddenRequirements = decodeRequirements(buf);
        List<Requirement> learnRequirements = decodeRequirements(buf);
        SpellNodeId id = new SpellNodeId(buf.readResourceLocation(), buf.readInt());
        int frame = buf.readByte();
        float manaCost = buf.readFloat();
        
        return new SpellNode(id, new SpellInstance(spell, manaCost), levelCost, hiddenRequirements, learnRequirements, frame);
    }
}
