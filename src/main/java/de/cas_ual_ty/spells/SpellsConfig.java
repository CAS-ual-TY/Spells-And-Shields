package de.cas_ual_ty.spells;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class SpellsConfig
{
    public static final ForgeConfigSpec GENERAL_SPEC;
    
    public static final ForgeConfigSpec.BooleanValue CREATE_SPELLS_CONFIGS;
    public static final ForgeConfigSpec.BooleanValue LOAD_SPELLS_CONFIGS;
    
    public static final ForgeConfigSpec.BooleanValue RESPAWN_WITH_FULL_MANA;
    public static final ForgeConfigSpec.BooleanValue CLEAR_SLOTS_ON_DEATH;
    public static final ForgeConfigSpec.BooleanValue FORGET_SPELLS_ON_DEATH;
    
    public static final ForgeConfigSpec.BooleanValue GEN_SPELLS_LIST;
    
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ENCHANTING_TABLE;
    
    static
    {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        
        LOAD_SPELLS_CONFIGS = configBuilder
                .comment("Load spell settings from existing configuration files in the configuration folder (true) or use the default settings (false).")
                .define("loadSpells", false);
        
        CREATE_SPELLS_CONFIGS = configBuilder
                .comment("Create default spell configuration files in the configuration folder (true) or not (false). Automatically switches back to false after creating the files.")
                .define("createSpells", false);
        
        RESPAWN_WITH_FULL_MANA = configBuilder
                .comment("Players respawn with their mana bar filled (true) or empty (false).")
                .define("respawnWithFullMana", false);
        
        CLEAR_SLOTS_ON_DEATH = configBuilder
                .comment("Clear a player's spell slots on death (true) or not (false). If forgetSpellsOnDeath is set to true, this always behaves as if it was also set to true.")
                .define("clearSlotsOnDeath", true);
        
        FORGET_SPELLS_ON_DEATH = configBuilder
                .comment("Make a player forget all learned spells on death (true) or not (false). Forgotten spells are still visible in spell trees but must be relearned before you can equip them.")
                .define("forgetSpellsOnDeath", true);
        
        ENCHANTING_TABLE = configBuilder
                .comment("Resource location of the enchanting table. Some mods could change that.")
                .defineList("enchantingTables", ImmutableList.of("minecraft:enchanting_table", "quark:matrix_enchanter"), s -> true);
        
        GEN_SPELLS_LIST = configBuilder
                .comment("Create a file containing a list of all spells with their IDs. Automatically switches back to false after creating the file.")
                .define("createSpellsList", false);
        
        GENERAL_SPEC = configBuilder.build();
    }
}
