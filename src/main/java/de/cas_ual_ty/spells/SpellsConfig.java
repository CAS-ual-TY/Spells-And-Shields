package de.cas_ual_ty.spells;

import net.minecraftforge.common.ForgeConfigSpec;

public class SpellsConfig
{
    public static final ForgeConfigSpec GENERAL_SPEC;
    
    public static final ForgeConfigSpec.BooleanValue LOAD_SPELL_TREES;
    public static final ForgeConfigSpec.BooleanValue CREATE_DEFAULT_SPELL_TREES;
    public static final ForgeConfigSpec.BooleanValue ADD_DEFAULT_SPELL_TREES;
    
    public static final ForgeConfigSpec.BooleanValue CREATE_SPELLS_CONFIGS;
    public static final ForgeConfigSpec.BooleanValue LOAD_SPELLS_CONFIGS;
    
    public static final ForgeConfigSpec.BooleanValue RESPAWN_WITH_FULL_MANA;
    public static final ForgeConfigSpec.BooleanValue CLEAR_SLOTS_ON_DEATH;
    public static final ForgeConfigSpec.BooleanValue FORGET_SPELLS_ON_DEATH;
    
    static
    {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        
        LOAD_SPELL_TREES = configBuilder
                .comment("Load spell trees from configuration files in the configuration folder (true) or use the default spell trees (false).")
                .define("loadSpellTrees", false);
        
        CREATE_DEFAULT_SPELL_TREES = configBuilder
                .comment("Create default spell tree configuration files in the configuration folder if said folder does not exist (true) or not (false).")
                .define("createDefaultSpellTrees", false);
        
        ADD_DEFAULT_SPELL_TREES = configBuilder
                .comment("Add default mod spell trees to the game (true) or not (false).")
                .define("addDefaultSpellTrees", true);
        
        LOAD_SPELLS_CONFIGS = configBuilder
                .comment("Load spell settings from existing configuration files in the configuration folder (true) or use the default settings (false).")
                .define("loadSpells", false);
        
        CREATE_SPELLS_CONFIGS = configBuilder
                .comment("Create default spell configuration files in the configuration folder (true) or not (false).")
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
        
        GENERAL_SPEC = configBuilder.build();
    }
}
