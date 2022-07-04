package de.cas_ual_ty.spells;

import net.minecraftforge.common.ForgeConfigSpec;

public class SpellsConfig
{
    public static final ForgeConfigSpec GENERAL_SPEC;
    
    public static final ForgeConfigSpec.BooleanValue LOAD_SPELL_TREES;
    public static final ForgeConfigSpec.BooleanValue CREATE_DEFAULT_SPELL_TREES;
    
    static
    {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        
        LOAD_SPELL_TREES = configBuilder
                .comment("Load spell trees from configuration files in the configuration folder (true) or not (false).")
                .define("loadSpellTrees", true);
        
        CREATE_DEFAULT_SPELL_TREES = configBuilder
                .comment("Create default spell tree configuration files in the configuration folder if said folder does not exist (true) or no (false).")
                .define("createDefaultSpellTrees", true);
        
        GENERAL_SPEC = configBuilder.build();
    }
}
