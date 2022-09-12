package de.cas_ual_ty.spells.spelltree;

import com.google.gson.JsonElement;
import de.cas_ual_ty.spells.Spells;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsConfig;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.event.AvailableSpellTreesEvent;
import de.cas_ual_ty.spells.requirement.AdvancementRequirement;
import de.cas_ual_ty.spells.requirement.BookshelvesRequirement;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.util.SpellTreeSerializer;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SpellTrees
{
    public static final List<SpellTree> LOADED_SPELL_TREES = new ArrayList<>();
    
    public static final String KEY_NETHER = "spell_tree.nether";
    public static final String KEY_OCEAN = "spell_tree.ocean";
    public static final String KEY_MINING = "spell_tree.mining";
    public static final String KEY_MOVEMENT = "spell_tree.movement";
    public static final String KEY_END = "spell_tree.end";
    
    public static List<SpellTree> getBaseTrees()
    {
        List<SpellTree> list = new ArrayList<>(5);
        list.add(fireTree());
        list.add(waterTree());
        list.add(earthTree());
        list.add(airTree());
        list.add(enderTree());
        return list;
    }
    
    public static SpellTree fireTree()
    {
        return SpellTree.builder("nether", Component.translatable(KEY_NETHER), Spells.FIRE_BALL, 15, bookshelves(28))
                .icon(Spells.FIRE_RESISTANCE.get())
                .add(Spells.LAVA_WALKER, 20, bookshelves(19))
                .add(Spells.FIRE_RESISTANCE, 30, bookshelves(30))
                .leaf()
                .leaf()
                .add(Spells.FIRE_CHARGE, 10, bookshelves(20))
                .add(Spells.FLAMETHROWER, 20, bookshelves(24))
                .finish();
    }
    
    public static SpellTree waterTree()
    {
        return SpellTree.builder("ocean", Component.translatable(KEY_OCEAN), Spells.WATER_BREATHING, 10)
                .icon(Spells.DOLPHINS_GRACE.get())
                .add(Spells.REGENERATION, 20, bookshelves(20))
                .add(Spells.AQUA_AFFINITY, 20, bookshelves(20))
                .leaf()
                .leaf()
                .add(Spells.WATER_LEAP, 5, bookshelves(10))
                .add(Spells.DOLPHINS_GRACE, 30, bookshelves(30))
                .leaf()
                .add(Spells.FROST_WALKER, 10, bookshelves(14))
                .leaf()
                .leaf()
                .add(Spells.WATER_WHIP, 10, bookshelves(10))
                .add(Spells.POTION_SHOT, 10, bookshelves(10))
                .finish();
    }
    
    public static SpellTree earthTree()
    {
        return SpellTree.builder("mining", Component.translatable(KEY_MINING), Spells.BLAST_SMELT, 5, bookshelves(8))
                .icon(Spells.HASTE.get())
                .add(Spells.SILENCE_TARGET, 25, bookshelves(26))
                .leaf()
                .add(Spells.INSTANT_MINE, 15, bookshelves(18))
                .add(Spells.HASTE, 25, bookshelves(24))
                .leaf()
                .leaf()
                .add(Spells.SPIT_METAL, 10, bookshelves(12))
                .finish();
    }
    
    public static SpellTree airTree()
    {
        return SpellTree.builder("movement", Component.translatable(KEY_MOVEMENT), Spells.JUMP_BOOST, 15, bookshelves(12))
                .icon(Spells.JUMP_BOOST.get())
                .add(Spells.LEAP, 10, bookshelves(14))
                .add(Spells.SPEED, 20, bookshelves(20))
                .leaf()
                .add(Spells.JUMP, 14, bookshelves(14))
                .leaf()
                .leaf()
                .add(Spells.MANA_SOLES, 15, bookshelves(12))
                .add(Spells.SLOW_FALLING, 15, bookshelves(16))
                .leaf()
                .leaf()
                .add(Spells.BLOW_ARROW, 10, bookshelves(16))
                .add(Spells.PRESSURIZE, 20, bookshelves(18))
                .leaf()
                .leaf()
                .finish();
    }
    
    public static SpellTree enderTree()
    {
        return SpellTree.builder("end", Component.translatable(KEY_END), Spells.RANDOM_TELEPORT, 20, bookshelves(28), advancement("end/root"))
                .icon(Spells.TELEPORT.get())
                .add(Spells.FORCED_TELEPORT.get(), 30, bookshelves(28))
                .leaf()
                .add(Spells.TELEPORT.get(), 30, bookshelves(28), advancement("end/respawn_dragon"))
                .finish();
    }
    
    public static Requirement bookshelves(int bookshelves)
    {
        return new BookshelvesRequirement(SpellsRegistries.BOOKSHELVES_REQUIREMENT.get(), bookshelves);
    }
    
    public static Requirement advancement(String advancementRL)
    {
        return new AdvancementRequirement(SpellsRegistries.ADVANCEMENT_REQUIREMENT.get(), new ResourceLocation(advancementRL));
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
                String filename = t.filename != null ? t.filename : ("tree_" + i);
                File f = p.resolve(filename + ".json").toFile();
                i++;
                
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
