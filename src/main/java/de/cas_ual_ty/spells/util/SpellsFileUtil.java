package de.cas_ual_ty.spells.util;

import com.google.gson.*;
import de.cas_ual_ty.spells.Spells;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.spell.ISpell;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SpellsFileUtil
{
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configDir = null;
    
    public static Path getOrCreateConfigDir()
    {
        return configDir != null ? configDir : (configDir = FileUtils.getOrCreateDirectory(FMLPaths.CONFIGDIR.get().resolve(SpellsAndShields.MOD_ID), SpellsAndShields.MOD_ID));
    }
    
    public static Path getOrCreateSubConfigDir(String name)
    {
        return FileUtils.getOrCreateDirectory(getOrCreateConfigDir().resolve(name), SpellsAndShields.MOD_ID + "/" + name);
    }
    
    public static boolean doesSubConfigDirExist(String name)
    {
        return Files.isDirectory(getOrCreateConfigDir().resolve(name));
    }
    
    public static void writeJsonToFile(File file, JsonElement json) throws IOException
    {
        try(FileWriter fw = new FileWriter(file))
        {
            GSON.toJson(json, fw);
            fw.flush();
        }
    }
    
    public static JsonElement readJsonFromFile(File file) throws IOException
    {
        try(FileReader fr = new FileReader(file))
        {
            return JsonParser.parseReader(fr);
        }
    }
    
    public static String jsonString(JsonObject json, String key) throws IllegalStateException
    {
        JsonElement element = json.get(key);
        
        if(element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString())
        {
            throw new IllegalStateException();
        }
        
        return element.getAsString();
    }
    
    public static int jsonInt(JsonObject json, String key) throws IllegalStateException
    {
        JsonElement element = json.get(key);
        
        if(element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber())
        {
            throw new IllegalStateException();
        }
        
        try
        {
            return element.getAsInt();
        }
        catch(NumberFormatException e)
        {
            throw new IllegalStateException(e);
        }
    }
    
    public static float jsonFloat(JsonObject json, String key) throws IllegalStateException
    {
        JsonElement element = json.get(key);
        
        if(element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber())
        {
            throw new IllegalStateException();
        }
        
        return element.getAsFloat();
    }
    
    public static double jsonDouble(JsonObject json, String key) throws IllegalStateException
    {
        JsonElement element = json.get(key);
        
        if(element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber())
        {
            throw new IllegalStateException();
        }
        
        return element.getAsDouble();
    }
    
    public static JsonElement jsonElement(JsonObject json, String key) throws IllegalStateException
    {
        JsonElement element = json.get(key);
        
        if(element == null)
        {
            throw new IllegalStateException();
        }
        
        return element;
    }
    
    public static JsonObject jsonObject(JsonObject json, String key) throws IllegalStateException
    {
        JsonElement element = json.get(key);
        
        if(element == null || !element.isJsonObject())
        {
            throw new IllegalStateException();
        }
        
        return element.getAsJsonObject();
    }
    
    public static JsonArray jsonArray(JsonObject json, String key) throws IllegalStateException
    {
        JsonElement element = json.get(key);
        
        if(element == null || !element.isJsonArray())
        {
            throw new IllegalStateException();
        }
        
        return element.getAsJsonArray();
    }
    
    public static Item jsonItem(JsonObject json, String key, boolean allowNull) throws IllegalStateException
    {
        String id = jsonString(json, key);
        
        if(id.isEmpty())
        {
            if(allowNull)
            {
                return null;
            }
            else
            {
                throw new IllegalStateException();
            }
        }
        
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        
        if(item == null)
        {
            throw new IllegalStateException(new NullPointerException());
        }
        
        return item;
    }
    
    public static Item jsonItem(JsonObject json, String key) throws IllegalStateException
    {
        return jsonItem(json, key, false);
    }
    
    public static ISpell jsonSpell(JsonObject json, String key, boolean allowNull) throws IllegalStateException
    {
        String id = jsonString(json, key);
        
        if(id.isEmpty())
        {
            if(allowNull)
            {
                return null;
            }
            else
            {
                throw new IllegalStateException();
            }
        }
        
        ISpell spell = Spells.SPELLS_REGISTRY.get().getValue(new ResourceLocation(id));
        
        if(spell == null)
        {
            throw new IllegalStateException(new NullPointerException());
        }
        
        return spell;
    }
    
    public static ISpell jsonSpell(JsonObject json, String key) throws IllegalStateException
    {
        return jsonSpell(json, key, false);
    }
    
    public static void jsonSpell(JsonObject json, @Nullable ISpell spell, String key)
    {
        if(spell == null)
        {
            json.addProperty(key, "");
        }
        else
        {
            json.addProperty(key, SpellsUtil.getSpellKey(spell).toString());
        }
    }
    
    public static ItemStack jsonItemStack(JsonObject json, String itemKey, String countKey) throws IllegalStateException
    {
        Item item = jsonItem(json, itemKey, true);
        int count = jsonInt(json, countKey);
        return count <= 0 || item == null ? ItemStack.EMPTY : new ItemStack(item, count);
    }
    
    public static void jsonItemStack(JsonObject json, ItemStack itemStack, String itemKey, String countKey)
    {
        if(itemStack.isEmpty())
        {
            json.addProperty(itemKey, "");
            json.addProperty(countKey, 0);
        }
        else
        {
            json.addProperty(itemKey, ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString());
            json.addProperty(countKey, itemStack.getCount());
        }
    }
}
