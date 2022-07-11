package de.cas_ual_ty.spells.spell.tree;

import com.google.gson.JsonElement;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsConfig;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.event.AvailableSpellTreesEvent;
import de.cas_ual_ty.spells.util.SpellTreeSerializer;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class SpellTrees
{
    public static final List<SpellTree> LOADED_SPELL_TREES = new LinkedList<>();
    
    public static List<SpellTree> addBaseTrees()
    {
        LOADED_SPELL_TREES.add(makeBaseTree());
        LOADED_SPELL_TREES.add(waterTree());
        LOADED_SPELL_TREES.add(airTree());
        LOADED_SPELL_TREES.add(fireTree());
        LOADED_SPELL_TREES.add(earthTree());
        return LOADED_SPELL_TREES;
    }
    
    public static SpellTree makeBaseTree()
    {
        return SpellTree.builder(SpellsUtil.generateUUIDForTree("baseTree"), SpellsRegistries.FIRE_BALL, 1, 20, new TextComponent("Basic Spell Tree"))
                .add(SpellsRegistries.PASSIVE_WATER_BREATHING)
                .add(SpellsRegistries.PASSIVE_SLOW_FALLING)
                .add(SpellsRegistries.PASSIVE_REGENERATION)
                .leaf()
                .leaf()
                .add(SpellsRegistries.PASSIVE_DIG_SPEED)
                .add(SpellsRegistries.PASSIVE_REPLENISHMENT)
                .leaf()
                .leaf()
                .leaf()
                .add(SpellsRegistries.LEAP)
                .add(SpellsRegistries.POCKET_BOW)
                .leaf()
                .add(SpellsRegistries.SMELT)
                .add(SpellsRegistries.PASSIVE_SPEED)
                .leaf()
                .add(SpellsRegistries.PASSIVE_JUMP_BOOST)
                .leaf()
                .add(SpellsRegistries.PASSIVE_DOLPHINS_GRACE)
                .leaf()
                .leaf()
                .add(SpellsRegistries.MANA_BOOST)
                .add(SpellsRegistries.HEALTH_BOOST)
                .leaf()
                .leaf()
                .add(SpellsRegistries.SUMMON_ANIMAL)
                .add(SpellsRegistries.WATER_LEAP)
                .leaf()
                .add(SpellsRegistries.PASSIVE_AQUA_AFFINITY)
                .finish();
    }
    
    public static SpellTree waterTree()
    {
        return SpellTree.builder(SpellsUtil.generateUUIDForTree("water"), SpellsRegistries.WATER_LEAP, 1, 20, new TextComponent("Water"))
                .add(SpellsRegistries.PASSIVE_AQUA_AFFINITY)
                .add(SpellsRegistries.PASSIVE_WATER_BREATHING)
                .leaf()
                .add(SpellsRegistries.PASSIVE_DOLPHINS_GRACE)
                .add(SpellsRegistries.PASSIVE_FROST_WALKER)
                .leaf()
                .leaf()
                .add(SpellsRegistries.PASSIVE_REGENERATION)
                .leaf()
                .add(SpellsRegistries.WATER_WHIP)
                .add(SpellsRegistries.POTION_SHOT)
                .finish();
    }
    
    public static SpellTree airTree()
    {
        return SpellTree.builder(SpellsUtil.generateUUIDForTree("air"), SpellsRegistries.LEAP, 1, 20, new TextComponent("Air"))
                .add(SpellsRegistries.JUMP)
                .leaf()
                .add(SpellsRegistries.PASSIVE_SPEED)
                .leaf()
                .add(SpellsRegistries.PASSIVE_JUMP_BOOST)
                .add(SpellsRegistries.PASSIVE_SLOW_FALLING)
                .leaf()
                .leaf()
                .add(SpellsRegistries.POCKET_BOW)
                .finish();
    }
    
    public static SpellTree fireTree()
    {
        return SpellTree.builder(SpellsUtil.generateUUIDForTree("fire"), SpellsRegistries.FIRE_BALL, 1, 20, new TextComponent("Fire"))
                .finish();
    }
    
    public static SpellTree earthTree()
    {
        return SpellTree.builder(SpellsUtil.generateUUIDForTree("earth"), SpellsRegistries.PASSIVE_DIG_SPEED, 1, 20, new TextComponent("Earth"))
                .add(SpellsRegistries.SMELT)
                .finish();
    }
    
    public static void readOrWriteSpellTreeConfigs()
    {
        boolean makeConfigs = !SpellsFileUtil.doesSubConfigDirExist("spell_trees");
        
        Path p = SpellsFileUtil.getOrCreateSubConfigDir("spell_trees");
        
        File folder = p.toFile();
        
        if(!folder.isDirectory() || folder.listFiles() == null)
        {
            SpellsAndShields.LOGGER.error("Can not read or write spell tree files in {} (is it a folder?).", p);
            return;
        }
        
        if(makeConfigs)
        {
            if(!SpellsConfig.CREATE_DEFAULT_SPELL_TREES.get())
            {
                return;
            }
            
            int i = 0;
            for(SpellTree t : SpellTrees.LOADED_SPELL_TREES)
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
        else
        {
            if(!SpellsConfig.LOAD_SPELL_TREES.get())
            {
                return;
            }
            
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
