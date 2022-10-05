package de.cas_ual_ty.spells.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.requirement.IRequirementType;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.spell.ISpell;
import de.cas_ual_ty.spells.spelltree.SpellNode;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class SpellTreeSerializer
{
    private static byte TYPE_FINISH = 0;
    private static byte TYPE_SPELL = 1;
    private static byte TYPE_UP = 2;
    
    public static void encodeTree(SpellTree spellTree, FriendlyByteBuf buf)
    {
        buf.writeUUID(spellTree.getId());
        buf.writeComponent(spellTree.getTitle());
        buf.writeRegistryId(spellTree.getIconSpell());
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
        list.forEach(requirement -> IRequirementType.writeToBuf(buf, requirement));
    }
    
    private static void encodeNode(SpellNode spellNode, FriendlyByteBuf buf)
    {
        buf.writeRegistryId(spellNode.getSpell());
        buf.writeInt(spellNode.getLevelCost());
        encodeRequirements(spellNode.getRequirements(), buf);
        buf.writeShort(spellNode.getId());
    }
    
    public static SpellTree decodeTree(FriendlyByteBuf buf)
    {
        UUID id = buf.readUUID();
        Component title = buf.readComponent();
        ISpell icon = buf.readRegistryId();
        List<Requirement> requirements = decodeRequirements(buf);
        
        SpellTree.Builder builder = SpellTree.builder(id, title, decodeNode(buf));
        
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
        
        return builder.icon(icon).finish().setRequirements(requirements);
    }
    
    private static List<Requirement> decodeRequirements(FriendlyByteBuf buf)
    {
        int size = buf.readInt();
        List<Requirement> list = new LinkedList<>();
        
        for(int i = 0; i < size; i++)
        {
            list.add(IRequirementType.readFromBuf(buf));
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
    
    private static JsonObject nodeToJsonRec(SpellNode node)
    {
        JsonObject json = new JsonObject();
        
        json.addProperty("spell", node.getSpell().getRegistryName().toString());
        json.addProperty("levelCost", node.getLevelCost());
        json.add("requirements", requirementsToJson(node.getRequirements()));
        
        JsonArray children = new JsonArray();
        node.getChildren().forEach(child -> children.add(nodeToJsonRec(child)));
        
        json.add("children", children);
        
        return json;
    }
    
    private static JsonArray requirementsToJson(List<Requirement> requirements)
    {
        JsonArray json = new JsonArray();
        requirements.forEach(requirement -> json.add(IRequirementType.writeToJson(requirement)));
        return json;
    }
    
    public static JsonObject treeToJson(SpellTree tree)
    {
        JsonObject json = new JsonObject();
        
        json.addProperty("id", tree.getId().toString());
        json.add("title", Component.Serializer.toJsonTree(tree.getTitle()));
        json.addProperty("icon_spell", tree.getIconSpell().getRegistryName().toString());
        json.add("requirements", requirementsToJson(tree.getRequirements()));
        json.add("root_spell", tree.getRoot() != null ? nodeToJsonRec(tree.getRoot()) : JsonNull.INSTANCE);
        
        return json;
    }
    
    private static SpellNode nodeFromJson(JsonObject json)
    {
        ISpell spell = SpellsFileUtil.jsonSpell(json, "spell");
        int levelCost = SpellsFileUtil.jsonInt(json, "levelCost");
        List<Requirement> requirements = requirementsFromJson(SpellsFileUtil.jsonArray(json, "requirements"));
        
        SpellNode node = new SpellNode(spell, levelCost, requirements);
        
        JsonArray children = SpellsFileUtil.jsonArray(json, "children");
        children.forEach(e -> SpellTree.connect(node, nodeFromJson(e.getAsJsonObject())));
        
        return node;
    }
    
    public static List<Requirement> requirementsFromJson(JsonArray json)
    {
        List<Requirement> requirements = new LinkedList<>();
        json.forEach(jsonElement ->
        {
            if(!jsonElement.isJsonObject())
            {
                return;
            }
            
            Requirement requirement = IRequirementType.readFromJson(jsonElement.getAsJsonObject());
            
            if(requirement != null)
            {
                requirements.add(requirement);
            }
        });
        return requirements;
    }
    
    public static SpellTree treeFromJson(JsonObject json)
    {
        UUID id = UUID.fromString(SpellsFileUtil.jsonString(json, "id"));
        Component title = Component.Serializer.fromJson(SpellsFileUtil.jsonElement(json, "title"));
        ISpell icon = SpellsFileUtil.jsonSpell(json, "icon_spell");
        List<Requirement> requirements = requirementsFromJson(SpellsFileUtil.jsonArray(json, "requirements"));
        SpellNode root = nodeFromJson(SpellsFileUtil.jsonObject(json, "root_spell"));
        
        return new SpellTree(id, root, title, icon).setRequirements(requirements);
    }
}
