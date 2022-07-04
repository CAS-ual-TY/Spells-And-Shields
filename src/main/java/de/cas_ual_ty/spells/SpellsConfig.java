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
                .comment("")
                .define("loadSpellTrees", true);
        
        CREATE_DEFAULT_SPELL_TREES = configBuilder
                .comment("")
                .define("createDefaultSpellTrees", true);
        
        GENERAL_SPEC = configBuilder.build();
    }
}
