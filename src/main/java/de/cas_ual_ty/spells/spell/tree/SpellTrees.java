package de.cas_ual_ty.spells.spell.tree;

import com.google.gson.JsonElement;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsConfig;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.event.AvailableSpellTreesEvent;
import de.cas_ual_ty.spells.util.SpellTreeSerializer;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SpellTrees
{
    public static final List<SpellTree> LOADED_SPELL_TREES = new ArrayList<>();
    
    public static List<SpellTree> getBaseTrees()
    {
        List<SpellTree> list = new ArrayList<>(4);
        list.add(fireTree());
        list.add(waterTree());
        list.add(earthTree());
        list.add(airTree());
        return list;
    }
    
    public static SpellTree fireTree()
    {
        return SpellTree.builder(SpellsUtil.generateUUIDForTree("nether"), SpellsRegistries.FIRE_BALL, 15, 28, Component.literal("Nether"))
                .icon(SpellsRegistries.PASSIVE_FIRE_RESISTANCE.get())
                .add(SpellsRegistries.PASSIVE_FIRE_RESISTANCE, 30, 30)
                .leaf()
                .add(SpellsRegistries.FIRE_CHARGE, 10, 28)
                .finish();
    }
    
    public static SpellTree waterTree()
    {
        return SpellTree.builder(SpellsUtil.generateUUIDForTree("ocean"), SpellsRegistries.PASSIVE_WATER_BREATHING, 10, 0, Component.literal("Ocean"))
                .icon(SpellsRegistries.PASSIVE_DOLPHINS_GRACE.get())
                .add(SpellsRegistries.PASSIVE_REGENERATION, 20, 20)
                .add(SpellsRegistries.PASSIVE_AQUA_AFFINITY, 20, 20)
                .leaf()
                .leaf()
                .add(SpellsRegistries.WATER_LEAP, 5, 10)
                .add(SpellsRegistries.PASSIVE_DOLPHINS_GRACE, 30, 30)
                .leaf()
                .add(SpellsRegistries.PASSIVE_FROST_WALKER, 10, 14)
                .leaf()
                .leaf()
                .add(SpellsRegistries.WATER_WHIP, 10, 10)
                .add(SpellsRegistries.POTION_SHOT, 10, 10)
                .finish();
    }
    
    public static SpellTree earthTree()
    {
        return SpellTree.builder(SpellsUtil.generateUUIDForTree("mining"), SpellsRegistries.SMELT, 5, 8, Component.literal("Mining"))
                .icon(SpellsRegistries.PASSIVE_DIG_SPEED.get())
                .add(SpellsRegistries.INSTANT_MINE, 15, 18)
                .add(SpellsRegistries.PASSIVE_DIG_SPEED, 25, 24)
                .finish();
    }
    
    public static SpellTree airTree()
    {
        return SpellTree.builder(SpellsUtil.generateUUIDForTree("movement"), SpellsRegistries.PASSIVE_JUMP_BOOST, 15, 12, Component.literal("Movement"))
                .icon(SpellsRegistries.PASSIVE_JUMP_BOOST.get())
                .add(SpellsRegistries.LEAP, 10, 14)
                .add(SpellsRegistries.PASSIVE_SPEED, 20, 20)
                .leaf()
                .add(SpellsRegistries.JUMP, 14, 14)
                .leaf()
                .leaf()
                .add(SpellsRegistries.MANA_SOLES, 15, 12)
                .add(SpellsRegistries.PASSIVE_SLOW_FALLING, 15, 16)
                .leaf()
                .leaf()
                .add(SpellsRegistries.BLOW_ARROW, 10, 16)
                .add(SpellsRegistries.PRESSURIZE, 20, 18)
                .leaf()
                .leaf()
                .finish();
    }
    
    public static void readOrWriteSpellTreeConfigs()
    {
        List<SpellTree> baseTrees = SpellTrees.getBaseTrees();
    
        if(SpellsConfig.ADD_DEFAULT_SPELL_TREES.get())
        {
            LOADED_SPELL_TREES.addAll(baseTrees);
        }
        
        boolean makeConfigs = !SpellsFileUtil.doesSubConfigDirExist("spell_trees");
        
        Path p = SpellsFileUtil.getOrCreateSubConfigDir("spell_trees");
        
        File folder = p.toFile();
        
        if(!folder.isDirectory() || folder.listFiles() == null)
        {
            SpellsAndShields.LOGGER.error("Can not read or write spell tree files in {} (is it a folder?).", p);
            return;
        }
        
        if(SpellsConfig.CREATE_DEFAULT_SPELL_TREES.get())
        {
            SpellsConfig.CREATE_DEFAULT_SPELL_TREES.set(false);
            SpellsConfig.CREATE_DEFAULT_SPELL_TREES.save();
            
            int i = 0;
            for(SpellTree t : baseTrees)
            {
                File f = p.resolve("tree_" + i++ + ".json").toFile();
                
                try
                {
                    SpellsFileUtil.writeJsonToFile(f, SpellTreeSerializer.treeToJson(t));
                    SpellsAndShields.LOGGER.info("Wrote default spell tree {} to file {}.", t.getId(), f.toPath());
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    SpellsAndShields.LOGGER.error("Failed writing default spell tree {} to file {}.", t.getId().toString(), f.toPath(), e);
                }
            }
        }
        
        if(SpellsConfig.LOAD_SPELL_TREES.get())
        {
            for(File f : folder.listFiles())
            {
                if(f.getName().toLowerCase().endsWith(".json"))
                {
                    boolean failed = false;
                    JsonElement json = null;
                    
                    try
                    {
                        json = SpellsFileUtil.readJsonFromFile(f);
                    }
                    catch(Exception e)
                    {
                        failed = true;
                        SpellsAndShields.LOGGER.error("Failed reading spell tree from file {}.", f.toPath(), e);
                        e.printStackTrace();
                    }
                    
                    if(json != null && json.isJsonObject())
                    {
                        SpellTree t = null;
                        
                        try
                        {
                            t = SpellTreeSerializer.treeFromJson(json.getAsJsonObject());
                            SpellsAndShields.LOGGER.info("Successfully read spell tree from file {}.", f.toPath());
                        }
                        catch(IllegalStateException e)
                        {
                            SpellsAndShields.LOGGER.error("Failed reading spell tree from file {}.", f.toPath(), e);
                            e.printStackTrace();
                        }
                        
                        if(t != null)
                        {
                            LOADED_SPELL_TREES.add(t);
                        }
                    }
                    else if(!failed)
                    {
                        SpellsAndShields.LOGGER.error("Failed reading spell tree from file {}.", f.toPath());
                    }
                }
            }
        }
    }
    
    private static void availableSpellTrees(AvailableSpellTreesEvent event)
    {
        SpellTrees.LOADED_SPELL_TREES.forEach(tree -> event.addSpellTree(tree.copy()));
    }
    
    public static void registerEvents()
    {
        MinecraftForge.EVENT_BUS.addListener(SpellTrees::availableSpellTrees);
    }
}
