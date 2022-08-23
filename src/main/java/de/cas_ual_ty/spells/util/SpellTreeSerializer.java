package de.cas_ual_ty.spells.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.requirement.IRequirementType;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.spell.ISpell;
import de.cas_ual_ty.spells.spelltree.SpellNode;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.spelltree.SpellTreeClass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    }
    
    public static SpellTree decodeTree(FriendlyByteBuf buf)
    {
        UUID id = buf.readUUID();
        Component title = buf.readComponent();
        ISpell icon = buf.readRegistryId();
        
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
        
        return builder.icon(icon).finish();
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
        
        return new SpellNode(spell, levelCost, requirements);
    }
    
    private static JsonObject nodeToJsonRec(SpellNode node)
    {
        JsonObject json = new JsonObject();
        
        json.addProperty("spell", node.getSpell().getRegistryName().toString());
        
        json.addProperty("levelCost", node.getLevelCost());
        
        JsonArray requirements = new JsonArray();
        node.getRequirements().forEach(requirement -> requirements.add(IRequirementType.writeToJson(requirement)));
        json.add("requirements", requirements);
        
        JsonArray children = new JsonArray();
        node.getChildren().forEach(child -> children.add(nodeToJsonRec(child)));
        
        json.add("children", children);
        
        return json;
    }
    
    public static JsonObject treeToJson(SpellTree tree)
    {
        JsonObject json = new JsonObject();
        
        json.addProperty("id", tree.getId().toString());
        
        json.add("title", Component.Serializer.toJsonTree(tree.getTitle()));
        
        json.addProperty("icon_spell", tree.getIconSpell().getRegistryName().toString());
        json.add("root_spell", tree.getRoot() != null ? nodeToJsonRec(tree.getRoot()) : JsonNull.INSTANCE);
        
        return json;
    }
    
    private static SpellNode nodeFromJson(JsonObject json)
    {
        ISpell spell = SpellsFileUtil.jsonSpell(json, "spell");
        int levelCost = SpellsFileUtil.jsonInt(json, "levelCost");
        JsonArray array = SpellsFileUtil.jsonArray(json, "requirements");
        JsonArray children = SpellsFileUtil.jsonArray(json, "children");
        
        List<Requirement> requirements = new LinkedList<>();
        array.forEach(jsonElement ->
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
        
        SpellNode node = new SpellNode(spell, levelCost, requirements);
        
        children.forEach(e -> SpellTree.connect(node, nodeFromJson(e.getAsJsonObject())));
        
        return node;
    }
    
    public static SpellTree treeFromJson(JsonObject json)
    {
        UUID id = UUID.fromString(SpellsFileUtil.jsonString(json, "id"));
        Component title = Component.Serializer.fromJson(SpellsFileUtil.jsonElement(json, "title"));
        ISpell icon = SpellsFileUtil.jsonSpell(json, "icon_spell");
        SpellNode root = nodeFromJson(SpellsFileUtil.jsonObject(json, "root_spell"));
        
        return new SpellTree(id, root, title, icon);
    }
    
    public static SpellTreeClass classFromJson(JsonObject json)
    {
        SpellTreeClass c = new SpellTreeClass();
        
        JsonArray modifiers = SpellsFileUtil.jsonArray(json, "modifiers");
        
        for(JsonElement eModifier : modifiers)
        {
            if(!eModifier.isJsonObject())
            {
                throw new IllegalStateException();
            }
            
            JsonObject modifier = eModifier.getAsJsonObject();
            
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(SpellsFileUtil.jsonString(modifier, "attribute")));
            
            if(attribute == null)
            {
                throw new IllegalStateException(new NullPointerException());
            }
            
            double amount = SpellsFileUtil.jsonDouble(modifier, "amount");
            AttributeModifier.Operation operation;
            
            try
            {
                operation = AttributeModifier.Operation.fromValue(SpellsFileUtil.jsonInt(modifier, "operation"));
            }
            catch(IllegalArgumentException e) // NumberFormatException included
            {
                throw new IllegalStateException(e);
            }
            
            String name = SpellsFileUtil.jsonString(modifier, "name");
            UUID id = SpellsUtil.generateUUIDForClassAttribute(attribute, name);
            
            c.addModifier(attribute, new AttributeModifier(id, name, amount, operation));
        }
        
        return c;
    }
    
    public static JsonObject classToJson(SpellTreeClass c)
    {
        JsonObject json = new JsonObject();
        
        JsonArray modifiers = new JsonArray();
        
        for(Map.Entry<Attribute, AttributeModifier> entry : c.modifiers.entrySet())
        {
            JsonObject modifier = new JsonObject();
            
            modifier.addProperty("attribute", ForgeRegistries.ATTRIBUTES.getKey(entry.getKey()).toString());
            modifier.addProperty("amount", entry.getValue().getAmount());
            modifier.addProperty("operation", entry.getValue().getOperation().toValue());
            modifier.addProperty("name", entry.getValue().getName());
            modifier.addProperty("id", SpellsUtil.generateUUIDForClassAttribute(entry.getKey(), c.getName()).toString());
            
            modifiers.add(modifier);
        }
        
        json.add("modifiers", modifiers);
        
        return json;
    }
}
