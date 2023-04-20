package de.cas_ual_ty.spells.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DocsGen implements DataProvider
{
    public static final Map<String, String> PREFIX_MAP = new HashMap<>();
    
    private static final File ROOT = new File("./docsgen");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    protected DataGenerator gen;
    protected String modId;
    protected ExistingFileHelper exFileHelper;
    protected RegistryAccess registryAccess;
    protected RegistryOps<JsonElement> registryOps;
    
    public DocsGen(DataGenerator gen, String modId, ExistingFileHelper exFileHelper)
    {
        this.gen = gen;
        this.modId = modId;
        this.exFileHelper = exFileHelper;
        this.registryAccess = RegistryAccess.builtinCopy();
        this.registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
    }
    
    public static void generateSingleFiles(String modId)
    {
        ROOT.mkdirs();
        SpellsAndShields.LOGGER.info("Generated reduced json files for docs in: " + ROOT.getAbsolutePath());
        
        for(Map.Entry<ResourceKey<SpellActionType<?>>, SpellActionType<?>> entry : SpellActionTypes.REGISTRY.get().getEntries())
        {
            ResourceLocation rl = entry.getKey().location();
            
            if(!rl.getNamespace().equals(modId))
            {
                continue;
            }
            
            SpellActionType<?> type = entry.getValue();
            createFile(rl, type);
        }
    }
    
    public static void generateBigFile(String modId)
    {
        ROOT.mkdirs();
        File full = new File(ROOT, modId + ".md");
        
        SpellsAndShields.LOGGER.info("Generated big file containing reduced json entries for docs in: " + full.getAbsolutePath());
        
        try(FileWriter fw = new FileWriter(full))
        {
            for(Map.Entry<ResourceKey<SpellActionType<?>>, SpellActionType<?>> entry : SpellActionTypes.REGISTRY.get().getEntries())
            {
                ResourceLocation rl = entry.getKey().location();
                
                if(!rl.getNamespace().equals(modId))
                {
                    continue;
                }
                
                SpellActionType<?> type = entry.getValue();
                
                create(rl, type, fw);
            }
        }
        catch(IOException e)
        {
        
        }
    }
    
    private static <A extends SpellAction> void createFile(ResourceLocation rl, SpellActionType<A> type)
    {
        File folder = new File(ROOT, rl.getNamespace());
        folder.mkdir();
        
        File f = new File(folder, rl.getPath() + ".md");
        
        try(FileWriter fw = new FileWriter(f))
        {
            create(rl, type, fw);
        }
        catch(IOException | RuntimeException ex)
        {
            SpellsAndShields.LOGGER.error("Failed to generate doc for " + rl);
        }
    }
    
    private static <A extends SpellAction> void create(ResourceLocation rl, SpellActionType<A> type, FileWriter fw) throws IOException
    {
        if(type.getCodec() instanceof MapCodec.MapCodecCodec<A> mapCodecCodec)
        {
            fw.write('\n');
            fw.write("***\n");
            fw.write('\n');
            fw.write("### `" + rl.getPath() + "` Type\n");
            fw.write('\n');
            fw.write("ABCDE Text WIP\n");
            fw.write('\n');
            fw.write("JSON Format:\n");
            fw.write("```jsonc\n");
            fw.write("{\n");
            
            List<String> keys = new LinkedList<>();
            mapCodecCodec.codec().keys(JsonOps.INSTANCE).map(JsonElement::getAsString).forEach(key ->
            {
                if(!keys.contains(key) && !key.equals("activation"))
                {
                    keys.add(key);
                }
            });
            
            fw.write("  \"type\": \"" + rl.toString() + "\"" + (keys.isEmpty() ? "\n" : ",\n"));
            fw.write("  \"activation\": String" + (keys.isEmpty() ? "\n" : ",\n"));
            fw.write(keys.stream().map(key -> "  \"" + key + "\": " +
                    (PREFIX_MAP.entrySet().stream()
                            .filter(e -> key.contains(e.getKey()))
                            .map(Map.Entry::getValue).findFirst().orElse("ABCDE")
                    )
            ).collect(Collectors.joining(",\n")) + "\n");
            
            fw.write("}\n");
            fw.write("```\n");
            fw.write("Elements:\n");
            fw.write(keys.stream()
                    .map(key -> "- `" + key + "`: ABCDE.")
                    .collect(Collectors.joining("\n")) + "\n");
        }
    }
    
    @Override
    public void run(CachedOutput pOutput) throws IOException
    {
        generateSingleFiles(modId);
        generateBigFile(modId);
    }
    
    @Override
    public String getName()
    {
        return "Spells & Shields Docs";
    }
}
