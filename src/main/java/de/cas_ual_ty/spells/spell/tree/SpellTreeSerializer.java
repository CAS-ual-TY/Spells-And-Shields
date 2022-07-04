package de.cas_ual_ty.spells.spell.tree;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.SpellsFileUtil;
import de.cas_ual_ty.spells.SpellsUtil;
import de.cas_ual_ty.spells.spell.base.ISpell;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

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
    
    private static void encodeNode(SpellNode spellNode, FriendlyByteBuf buf)
    {
        buf.writeRegistryId(spellNode.getSpell());
        buf.writeInt(spellNode.getLevelCost());
        buf.writeByte(spellNode.getRequiredBookshelves());
    }
    
    public static SpellTree decodeTree(FriendlyByteBuf buf)
    {
        UUID id = buf.readUUID();
        Component title = buf.readComponent();
        ISpell icon = buf.readRegistryId();
        
        SpellTree.Builder builder = SpellTree.builder(id, decodeNode(buf), title);
        
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
    
    public static SpellNode decodeNode(FriendlyByteBuf buf)
    {
        ISpell spell = buf.readRegistryId();
        int levelCost = buf.readInt();
        byte requiredBookshelves = buf.readByte();
        
        return new SpellNode(spell, levelCost, requiredBookshelves);
    }
    
    private static JsonObject nodeToJsonRec(SpellNode node)
    {
        JsonObject json = new JsonObject();
        
        json.addProperty("spell", node.getSpell().getRegistryName().toString());
        
        json.addProperty("levelCost", node.getLevelCost());
        json.addProperty("requiredBookshelves", node.getRequiredBookshelves());
        
        JsonArray children = new JsonArray();
        node.getChildren().forEach(child -> children.add(nodeToJsonRec(child)));
        
        json.add("children", children);
        
        return json;
    }
    
    public static JsonObject treeToJson(SpellTree tree)
    {
        JsonObject json = new JsonObject();
        
        json.addProperty("id", tree.getId().toString());
        
        if(tree.getTitle() instanceof TranslatableComponent t)
        {
            json.addProperty("title", t.getKey());
        }
        else
        {
            json.addProperty("title", tree.getTitle().getContents());
        }
        
        json.addProperty("icon_spell", tree.getIconSpell().getRegistryName().toString());
        json.add("root_spell", tree.getRoot() != null ? nodeToJsonRec(tree.getRoot()) : JsonNull.INSTANCE);
        
        return json;
    }
    
    private static SpellNode nodeFromJson(JsonObject json)
    {
        ISpell spell = SpellsFileUtil.jsonSpell(json, "spell");
        int levelCost = SpellsFileUtil.jsonInt(json, "levelCost");
        int requiredBookshelves = SpellsFileUtil.jsonInt(json, "requiredBookshelves");
        JsonArray children = SpellsFileUtil.jsonArray(json, "children");
        
        SpellNode node = new SpellNode(spell, levelCost, requiredBookshelves);
        
        children.forEach(e -> SpellTree.connect(node, nodeFromJson(e.getAsJsonObject())));
        
        return node;
    }
    
    public static SpellTree treeFromJson(JsonObject json)
    {
        UUID id = UUID.fromString(SpellsFileUtil.jsonString(json, "id"));
        TranslatableComponent title = new TranslatableComponent(SpellsFileUtil.jsonString(json, "title"));
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
            UUID id = SpellsUtil.getUUIDFromAttribute(attribute);
            
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
            
            modifier.addProperty("attribute", entry.getKey().getRegistryName().toString());
            modifier.addProperty("amount", entry.getValue().getAmount());
            modifier.addProperty("operation", entry.getValue().getOperation().toValue());
            modifier.addProperty("name", entry.getValue().getName());
            modifier.addProperty("id", SpellsUtil.getUUIDFromAttribute(entry.getKey()).toString());
            
            modifiers.add(modifier);
        }
        
        json.add("modifiers", modifiers);
        
        return json;
    }
}
