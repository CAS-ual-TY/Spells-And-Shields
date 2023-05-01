package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.util.SpellsCodecs;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class Spells
{
    private static Supplier<IForgeRegistry<Spell>> REGISTRY;
    public static ResourceKey<Registry<Spell>> REGISTRY_KEY;
    
    public static Registry<Spell> getRegistry(LevelAccessor level)
    {
        return level.registryAccess().registryOrThrow(REGISTRY_KEY);
    }
    
    public static final ResourceLocation DUMMY = rl("dummy");
    public static final String KEY_DUMMY = key(DUMMY);
    public static final String KEY_DUMMY_DESC = descKey(DUMMY);
    
    public static final ResourceLocation LEAP = rl("leap");
    public static final String KEY_LEAP = key(LEAP);
    public static final String KEY_LEAP_DESC = descKey(LEAP);
    
    public static final ResourceLocation SUMMON_ANIMAL = rl("summon_animal");
    public static final String KEY_SUMMON_ANIMAL = key(SUMMON_ANIMAL);
    public static final String KEY_SUMMON_ANIMAL_DESC = descKey(SUMMON_ANIMAL);
    
    // TODO ingredients, mid-flight particles
    public static final ResourceLocation FIRE_BALL = rl("fire_ball");
    public static final String KEY_FIRE_BALL = key(FIRE_BALL);
    public static final String KEY_FIRE_BALL_DESC = descKey(FIRE_BALL);
    
    // TODO all
    public static final ResourceLocation BLAST_SMELT = rl("blast_smelt");
    public static final String KEY_BLAST_SMELT = key(BLAST_SMELT);
    public static final String KEY_BLAST_SMELT_DESC = descKey(BLAST_SMELT);
    public static final String KEY_BLAST_SMELT_DESC_COST = descKey(BLAST_SMELT) + ".cost";
    
    public static final ResourceLocation TRANSFER_MANA = rl("transfer_mana");
    public static final String KEY_TRANSFER_MANA = key(TRANSFER_MANA);
    public static final String KEY_TRANSFER_MANA_DESC = descKey(TRANSFER_MANA);
    
    public static final ResourceLocation BLOW_ARROW = rl("blow_arrow");
    public static final String KEY_BLOW_ARROW = key(BLOW_ARROW);
    public static final String KEY_BLOW_ARROW_DESC = descKey(BLOW_ARROW);
    
    public static final ResourceLocation HEALTH_BOOST = rl("health_boost");
    public static final String KEY_HEALTH_BOOST = key(HEALTH_BOOST);
    public static final String KEY_HEALTH_BOOST_DESC = descKey(HEALTH_BOOST);
    
    public static final ResourceLocation MANA_BOOST = rl("mana_boost");
    public static final String KEY_MANA_BOOST = key(MANA_BOOST);
    public static final String KEY_MANA_BOOST_DESC = descKey(MANA_BOOST);
    
    public static final ResourceLocation WATER_LEAP = rl("water_leap");
    public static final String KEY_WATER_LEAP = key(WATER_LEAP);
    public static final String KEY_WATER_LEAP_DESC = descKey(WATER_LEAP);
    
    public static final ResourceLocation PERMANENT_AQUA_AFFINITY = rl("permanent_aqua_affinity");
    public static final String KEY_PERMANENT_AQUA_AFFINITY = key(PERMANENT_AQUA_AFFINITY);
    public static final String KEY_PERMANENT_AQUA_AFFINITY_DESC = descKey(PERMANENT_AQUA_AFFINITY);
    
    //TODO fx
    public static final ResourceLocation WATER_WHIP = rl("water_whip");
    public static final String KEY_WATER_WHIP = key(WATER_WHIP);
    public static final String KEY_WATER_WHIP_DESC = descKey(WATER_WHIP);
    
    public static final ResourceLocation POTION_SHOT = rl("potion_shot");
    public static final String KEY_POTION_SHOT = key(POTION_SHOT);
    public static final String KEY_POTION_SHOT_DESC = descKey(POTION_SHOT);
    
    public static final ResourceLocation PERMANENT_FROST_WALKER = rl("permanent_frost_walker");
    public static final String KEY_PERMANENT_FROST_WALKER = key(PERMANENT_FROST_WALKER);
    public static final String KEY_PERMANENT_FROST_WALKER_DESC = descKey(PERMANENT_FROST_WALKER);
    
    public static final ResourceLocation TEMPORARY_FROST_WALKER = rl("temporary_frost_walker");
    public static final String KEY_TEMPORARY_FROST_WALKER = key(TEMPORARY_FROST_WALKER);
    public static final String KEY_TEMPORARY_FROST_WALKER_DESC = descKey(TEMPORARY_FROST_WALKER);
    
    public static final ResourceLocation TOGGLE_FROST_WALKER = rl("toggle_frost_walker");
    public static final String KEY_TOGGLE_FROST_WALKER = key(TOGGLE_FROST_WALKER);
    public static final String KEY_TOGGLE_FROST_WALKER_DESC = descKey(TOGGLE_FROST_WALKER);
    
    public static final ResourceLocation JUMP = rl("jump");
    public static final String KEY_JUMP = key(JUMP);
    public static final String KEY_JUMP_DESC = descKey(JUMP);
    
    public static final ResourceLocation MANA_SOLES = rl("mana_soles");
    public static final String KEY_MANA_SOLES = key(MANA_SOLES);
    public static final String KEY_MANA_SOLES_DESC = descKey(MANA_SOLES);
    
    public static final ResourceLocation FIRE_CHARGE = rl("fire_charge");
    public static final String KEY_FIRE_CHARGE = key(FIRE_CHARGE);
    public static final String KEY_FIRE_CHARGE_DESC = descKey(FIRE_CHARGE);
    
    public static final ResourceLocation PRESSURIZE = rl("pressurize");
    public static final String KEY_PRESSURIZE = key(PRESSURIZE);
    public static final String KEY_PRESSURIZE_DESC = descKey(PRESSURIZE);
    
    public static final ResourceLocation INSTANT_MINE = rl("instant_mine");
    public static final String KEY_INSTANT_MINE = key(INSTANT_MINE);
    public static final String KEY_INSTANT_MINE_DESC = descKey(INSTANT_MINE);
    public static final String KEY_INSTANT_MINE_DESC_REQUIREMENT = descKey(INSTANT_MINE) + ".requirement";
    
    public static final ResourceLocation SPIT_METAL = rl("spit_metal");
    public static final String KEY_SPIT_METAL = key(SPIT_METAL);
    public static final String KEY_SPIT_METAL_DESC = descKey(SPIT_METAL);
    
    public static final ResourceLocation FLAMETHROWER = rl("flamethrower");
    public static final String KEY_FLAMETHROWER = key(FLAMETHROWER);
    public static final String KEY_FLAMETHROWER_DESC = descKey(FLAMETHROWER);
    
    public static final ResourceLocation PERMANENT_LAVA_WALKER = rl("permanent_lava_walker");
    public static final String KEY_PERMANENT_LAVA_WALKER = key(PERMANENT_LAVA_WALKER);
    public static final String KEY_PERMANENT_LAVA_WALKER_DESC = descKey(PERMANENT_LAVA_WALKER);
    
    public static final ResourceLocation TEMPORARY_LAVA_WALKER = rl("temporary_lava_walker");
    public static final String KEY_TEMPORARY_LAVA_WALKER = key(TEMPORARY_LAVA_WALKER);
    public static final String KEY_TEMPORARY_LAVA_WALKER_DESC = descKey(TEMPORARY_LAVA_WALKER);
    
    public static final ResourceLocation TOGGLE_LAVA_WALKER = rl("toggle_lava_walker");
    public static final String KEY_TOGGLE_LAVA_WALKER = key(TOGGLE_LAVA_WALKER);
    public static final String KEY_TOGGLE_LAVA_WALKER_DESC = descKey(TOGGLE_LAVA_WALKER);
    
    public static final ResourceLocation SILENCE_TARGET = rl("silence_target");
    public static final String KEY_SILENCE_TARGET = key(SILENCE_TARGET);
    public static final String KEY_SILENCE_TARGET_DESC = descKey(SILENCE_TARGET);
    
    public static final ResourceLocation RANDOM_TELEPORT = rl("random_teleport");
    public static final String KEY_RANDOM_TELEPORT = key(RANDOM_TELEPORT);
    public static final String KEY_RANDOM_TELEPORT_DESC = descKey(RANDOM_TELEPORT);
    
    public static final ResourceLocation FORCED_TELEPORT = rl("forced_teleport");
    public static final String KEY_FORCED_TELEPORT = key(FORCED_TELEPORT);
    public static final String KEY_FORCED_TELEPORT_DESC = descKey(FORCED_TELEPORT);
    
    public static final ResourceLocation TELEPORT = rl("teleport");
    public static final String KEY_TELEPORT = key(TELEPORT);
    public static final String KEY_TELEPORT_DESC = descKey(TELEPORT);
    
    public static final ResourceLocation LIGHTNING_STRIKE = rl("lightning_strike");
    public static final String KEY_LIGHTNING_STRIKE = key(LIGHTNING_STRIKE);
    public static final String KEY_LIGHTNING_STRIKE_DESC = descKey(LIGHTNING_STRIKE);
    
    public static final ResourceLocation DRAIN_FLAME = rl("drain_flame");
    public static final String KEY_DRAIN_FLAME = key(DRAIN_FLAME);
    public static final String KEY_DRAIN_FLAME_DESC = descKey(DRAIN_FLAME);
    
    public static final ResourceLocation GROWTH = rl("growth");
    public static final String KEY_GROWTH = key(GROWTH);
    public static final String KEY_GROWTH_DESC = descKey(GROWTH);
    
    public static final ResourceLocation GHAST = rl("ghast");
    public static final String KEY_GHAST = key(GHAST);
    public static final String KEY_GHAST_DESC = descKey(GHAST);
    
    public static final ResourceLocation ENDER_ARMY = rl("ender_army");
    public static final String KEY_ENDER_ARMY = key(ENDER_ARMY);
    public static final String KEY_ENDER_ARMY_DESC = descKey(ENDER_ARMY);
    
    public static final ResourceLocation EVOKER_FANGS = rl("evoker_fangs");
    public static final String KEY_EVOKER_FANGS = key(EVOKER_FANGS);
    public static final String KEY_EVOKER_FANGS_DESC = descKey(EVOKER_FANGS);
    
    public static final ResourceLocation POCKET_ROCKET = rl("pocket_rocket");
    public static final String KEY_POCKET_ROCKET = key(POCKET_ROCKET);
    public static final String KEY_POCKET_ROCKET_DESC = descKey(POCKET_ROCKET);
    
    public static final ResourceLocation PERMANENT_REPLENISHMENT = rl("permanent_replenishment");
    public static final String KEY_PERMANENT_REPLENISHMENT = key(PERMANENT_REPLENISHMENT);
    public static final String KEY_PERMANENT_REPLENISHMENT_DESC = descKey(PERMANENT_REPLENISHMENT);
    public static final ResourceLocation TEMPORARY_REPLENISHMENT = rl("temporary_replenishment");
    public static final String KEY_TEMPORARY_REPLENISHMENT = key(TEMPORARY_REPLENISHMENT);
    public static final String KEY_TEMPORARY_REPLENISHMENT_DESC = descKey(TEMPORARY_REPLENISHMENT);
    public static final ResourceLocation TOGGLE_REPLENISHMENT = rl("toggle_replenishment");
    public static final String KEY_TOGGLE_REPLENISHMENT = key(TOGGLE_REPLENISHMENT);
    public static final String KEY_TOGGLE_REPLENISHMENT_DESC = descKey(TOGGLE_REPLENISHMENT);
    
    public static final ResourceLocation PERMANENT_MAGIC_IMMUNE = rl("permanent_magic_immune");
    public static final String KEY_PERMANENT_MAGIC_IMMUNE = key(PERMANENT_MAGIC_IMMUNE);
    public static final String KEY_PERMANENT_MAGIC_IMMUNE_DESC = descKey(PERMANENT_MAGIC_IMMUNE);
    public static final ResourceLocation TEMPORARY_MAGIC_IMMUNE = rl("temporary_magic_immune");
    public static final String KEY_TEMPORARY_MAGIC_IMMUNE = key(TEMPORARY_MAGIC_IMMUNE);
    public static final String KEY_TEMPORARY_MAGIC_IMMUNE_DESC = descKey(TEMPORARY_MAGIC_IMMUNE);
    public static final ResourceLocation TOGGLE_MAGIC_IMMUNE = rl("toggle_magic_immune");
    public static final String KEY_TOGGLE_MAGIC_IMMUNE = key(TOGGLE_MAGIC_IMMUNE);
    public static final String KEY_TOGGLE_MAGIC_IMMUNE_DESC = descKey(TOGGLE_MAGIC_IMMUNE);
    
    public static final ResourceLocation PERMANENT_SPEED = rl("permanent_speed");
    public static final String KEY_PERMANENT_SPEED = key(PERMANENT_SPEED);
    public static final String KEY_PERMANENT_SPEED_DESC = descKey(PERMANENT_SPEED);
    public static final ResourceLocation TEMPORARY_SPEED = rl("temporary_speed");
    public static final String KEY_TEMPORARY_SPEED = key(TEMPORARY_SPEED);
    public static final String KEY_TEMPORARY_SPEED_DESC = descKey(TEMPORARY_SPEED);
    public static final ResourceLocation TOGGLE_SPEED = rl("toggle_speed");
    public static final String KEY_TOGGLE_SPEED = key(TOGGLE_SPEED);
    public static final String KEY_TOGGLE_SPEED_DESC = descKey(TOGGLE_SPEED);
    
    public static final ResourceLocation PERMANENT_JUMP_BOOST = rl("permanent_jump_boost");
    public static final String KEY_PERMANENT_JUMP_BOOST = key(PERMANENT_JUMP_BOOST);
    public static final String KEY_PERMANENT_JUMP_BOOST_DESC = descKey(PERMANENT_JUMP_BOOST);
    public static final ResourceLocation TEMPORARY_JUMP_BOOST = rl("temporary_jump_boost");
    public static final String KEY_TEMPORARY_JUMP_BOOST = key(TEMPORARY_JUMP_BOOST);
    public static final String KEY_TEMPORARY_JUMP_BOOST_DESC = descKey(TEMPORARY_JUMP_BOOST);
    public static final ResourceLocation TOGGLE_JUMP_BOOST = rl("toggle_jump_boost");
    public static final String KEY_TOGGLE_JUMP_BOOST = key(TOGGLE_JUMP_BOOST);
    public static final String KEY_TOGGLE_JUMP_BOOST_DESC = descKey(TOGGLE_JUMP_BOOST);
    
    public static final ResourceLocation PERMANENT_DOLPHINS_GRACE = rl("permanent_dolphins_grace");
    public static final String KEY_PERMANENT_DOLPHINS_GRACE = key(PERMANENT_DOLPHINS_GRACE);
    public static final String KEY_PERMANENT_DOLPHINS_GRACE_DESC = descKey(PERMANENT_DOLPHINS_GRACE);
    public static final ResourceLocation TEMPORARY_DOLPHINS_GRACE = rl("temporary_dolphins_grace");
    public static final String KEY_TEMPORARY_DOLPHINS_GRACE = key(TEMPORARY_DOLPHINS_GRACE);
    public static final String KEY_TEMPORARY_DOLPHINS_GRACE_DESC = descKey(TEMPORARY_DOLPHINS_GRACE);
    public static final ResourceLocation TOGGLE_DOLPHINS_GRACE = rl("toggle_dolphins_grace");
    public static final String KEY_TOGGLE_DOLPHINS_GRACE = key(TOGGLE_DOLPHINS_GRACE);
    public static final String KEY_TOGGLE_DOLPHINS_GRACE_DESC = descKey(TOGGLE_DOLPHINS_GRACE);
    
    public static final ResourceLocation PERMANENT_WATER_BREATHING = rl("permanent_water_breathing");
    public static final String KEY_PERMANENT_WATER_BREATHING = key(PERMANENT_WATER_BREATHING);
    public static final String KEY_PERMANENT_WATER_BREATHING_DESC = descKey(PERMANENT_WATER_BREATHING);
    public static final ResourceLocation TEMPORARY_WATER_BREATHING = rl("temporary_water_breathing");
    public static final String KEY_TEMPORARY_WATER_BREATHING = key(TEMPORARY_WATER_BREATHING);
    public static final String KEY_TEMPORARY_WATER_BREATHING_DESC = descKey(TEMPORARY_WATER_BREATHING);
    public static final ResourceLocation TOGGLE_WATER_BREATHING = rl("toggle_water_breathing");
    public static final String KEY_TOGGLE_WATER_BREATHING = key(TOGGLE_WATER_BREATHING);
    public static final String KEY_TOGGLE_WATER_BREATHING_DESC = descKey(TOGGLE_WATER_BREATHING);
    
    public static final ResourceLocation PERMANENT_SLOW_FALLING = rl("permanent_slow_falling");
    public static final String KEY_PERMANENT_SLOW_FALLING = key(PERMANENT_SLOW_FALLING);
    public static final String KEY_PERMANENT_SLOW_FALLING_DESC = descKey(PERMANENT_SLOW_FALLING);
    public static final ResourceLocation TEMPORARY_SLOW_FALLING = rl("temporary_slow_falling");
    public static final String KEY_TEMPORARY_SLOW_FALLING = key(TEMPORARY_SLOW_FALLING);
    public static final String KEY_TEMPORARY_SLOW_FALLING_DESC = descKey(TEMPORARY_SLOW_FALLING);
    public static final ResourceLocation TOGGLE_SLOW_FALLING = rl("toggle_slow_falling");
    public static final String KEY_TOGGLE_SLOW_FALLING = key(TOGGLE_SLOW_FALLING);
    public static final String KEY_TOGGLE_SLOW_FALLING_DESC = descKey(TOGGLE_SLOW_FALLING);
    
    public static final ResourceLocation PERMANENT_HASTE = rl("permanent_haste");
    public static final String KEY_PERMANENT_HASTE = key(PERMANENT_HASTE);
    public static final String KEY_PERMANENT_HASTE_DESC = descKey(PERMANENT_HASTE);
    public static final ResourceLocation TEMPORARY_HASTE = rl("temporary_haste");
    public static final String KEY_TEMPORARY_HASTE = key(TEMPORARY_HASTE);
    public static final String KEY_TEMPORARY_HASTE_DESC = descKey(TEMPORARY_HASTE);
    public static final ResourceLocation TOGGLE_HASTE = rl("toggle_haste");
    public static final String KEY_TOGGLE_HASTE = key(TOGGLE_HASTE);
    public static final String KEY_TOGGLE_HASTE_DESC = descKey(TOGGLE_HASTE);
    
    public static final ResourceLocation PERMANENT_REGENERATION = rl("permanent_regeneration");
    public static final String KEY_PERMANENT_REGENERATION = key(PERMANENT_REGENERATION);
    public static final String KEY_PERMANENT_REGENERATION_DESC = descKey(PERMANENT_REGENERATION);
    public static final ResourceLocation TEMPORARY_REGENERATION = rl("temporary_regeneration");
    public static final String KEY_TEMPORARY_REGENERATION = key(TEMPORARY_REGENERATION);
    public static final String KEY_TEMPORARY_REGENERATION_DESC = descKey(TEMPORARY_REGENERATION);
    public static final ResourceLocation TOGGLE_REGENERATION = rl("toggle_regeneration");
    public static final String KEY_TOGGLE_REGENERATION = key(TOGGLE_REGENERATION);
    public static final String KEY_TOGGLE_REGENERATION_DESC = descKey(TOGGLE_REGENERATION);
    
    public static final ResourceLocation PERMANENT_FIRE_RESISTANCE = rl("permanent_fire_resistance");
    public static final String KEY_PERMANENT_FIRE_RESISTANCE = key(PERMANENT_FIRE_RESISTANCE);
    public static final String KEY_PERMANENT_FIRE_RESISTANCE_DESC = descKey(PERMANENT_FIRE_RESISTANCE);
    public static final ResourceLocation TEMPORARY_FIRE_RESISTANCE = rl("temporary_fire_resistance");
    public static final String KEY_TEMPORARY_FIRE_RESISTANCE = key(TEMPORARY_FIRE_RESISTANCE);
    public static final String KEY_TEMPORARY_FIRE_RESISTANCE_DESC = descKey(TEMPORARY_FIRE_RESISTANCE);
    public static final ResourceLocation TOGGLE_FIRE_RESISTANCE = rl("toggle_fire_resistance");
    public static final String KEY_TOGGLE_FIRE_RESISTANCE = key(TOGGLE_FIRE_RESISTANCE);
    public static final String KEY_TOGGLE_FIRE_RESISTANCE_DESC = descKey(TOGGLE_FIRE_RESISTANCE);
    
    public static final ResourceLocation PERMANENT_NIGHT_VISION = rl("permanent_night_vision");
    public static final String KEY_PERMANENT_NIGHT_VISION = key(PERMANENT_NIGHT_VISION);
    public static final String KEY_PERMANENT_NIGHT_VISION_DESC = descKey(PERMANENT_NIGHT_VISION);
    public static final ResourceLocation TEMPORARY_NIGHT_VISION = rl("temporary_night_vision");
    public static final String KEY_TEMPORARY_NIGHT_VISION = key(TEMPORARY_NIGHT_VISION);
    public static final String KEY_TEMPORARY_NIGHT_VISION_DESC = descKey(TEMPORARY_NIGHT_VISION);
    public static final ResourceLocation TOGGLE_NIGHT_VISION = rl("toggle_night_vision");
    public static final String KEY_TOGGLE_NIGHT_VISION = key(TOGGLE_NIGHT_VISION);
    public static final String KEY_TOGGLE_NIGHT_VISION_DESC = descKey(TOGGLE_NIGHT_VISION);
    
    public static final ResourceLocation PERMANENT_STRENGTH = rl("permanent_strength");
    public static final String KEY_PERMANENT_STRENGTH = key(PERMANENT_STRENGTH);
    public static final String KEY_PERMANENT_STRENGTH_DESC = descKey(PERMANENT_STRENGTH);
    public static final ResourceLocation TEMPORARY_STRENGTH = rl("temporary_strength");
    public static final String KEY_TEMPORARY_STRENGTH = key(TEMPORARY_STRENGTH);
    public static final String KEY_TEMPORARY_STRENGTH_DESC = descKey(TEMPORARY_STRENGTH);
    public static final ResourceLocation TOGGLE_STRENGTH = rl("toggle_strength");
    public static final String KEY_TOGGLE_STRENGTH = key(TOGGLE_STRENGTH);
    public static final String KEY_TOGGLE_STRENGTH_DESC = descKey(TOGGLE_STRENGTH);
    
    public static final ResourceLocation PERMANENT_RESISTANCE = rl("permanent_resistance");
    public static final String KEY_PERMANENT_RESISTANCE = key(PERMANENT_RESISTANCE);
    public static final String KEY_PERMANENT_RESISTANCE_DESC = descKey(PERMANENT_RESISTANCE);
    public static final ResourceLocation TEMPORARY_RESISTANCE = rl("temporary_resistance");
    public static final String KEY_TEMPORARY_RESISTANCE = key(TEMPORARY_RESISTANCE);
    public static final String KEY_TEMPORARY_RESISTANCE_DESC = descKey(TEMPORARY_RESISTANCE);
    public static final ResourceLocation TOGGLE_RESISTANCE = rl("toggle_resistance");
    public static final String KEY_TOGGLE_RESISTANCE = key(TOGGLE_RESISTANCE);
    public static final String KEY_TOGGLE_RESISTANCE_DESC = descKey(TOGGLE_RESISTANCE);
    
    public static final ResourceLocation PERMANENT_INVISIBILITY = rl("permanent_invisibility");
    public static final String KEY_PERMANENT_INVISIBILITY = key(PERMANENT_INVISIBILITY);
    public static final String KEY_PERMANENT_INVISIBILITY_DESC = descKey(PERMANENT_INVISIBILITY);
    public static final ResourceLocation TEMPORARY_INVISIBILITY = rl("temporary_invisibility");
    public static final String KEY_TEMPORARY_INVISIBILITY = key(TEMPORARY_INVISIBILITY);
    public static final String KEY_TEMPORARY_INVISIBILITY_DESC = descKey(TEMPORARY_INVISIBILITY);
    public static final ResourceLocation TOGGLE_INVISIBILITY = rl("toggle_invisibility");
    public static final String KEY_TOGGLE_INVISIBILITY = key(TOGGLE_INVISIBILITY);
    public static final String KEY_TOGGLE_INVISIBILITY_DESC = descKey(TOGGLE_INVISIBILITY);
    
    public static final ResourceLocation PERMANENT_GLOWING = rl("permanent_glowing");
    public static final String KEY_PERMANENT_GLOWING = key(PERMANENT_GLOWING);
    public static final String KEY_PERMANENT_GLOWING_DESC = descKey(PERMANENT_GLOWING);
    public static final ResourceLocation TEMPORARY_GLOWING = rl("temporary_glowing");
    public static final String KEY_TEMPORARY_GLOWING = key(TEMPORARY_GLOWING);
    public static final String KEY_TEMPORARY_GLOWING_DESC = descKey(TEMPORARY_GLOWING);
    public static final ResourceLocation TOGGLE_GLOWING = rl("toggle_glowing");
    public static final String KEY_TOGGLE_GLOWING = key(TOGGLE_GLOWING);
    public static final String KEY_TOGGLE_GLOWING_DESC = descKey(TOGGLE_GLOWING);
    
    public static final ResourceLocation PERMANENT_LUCK = rl("permanent_luck");
    public static final String KEY_PERMANENT_LUCK = key(PERMANENT_LUCK);
    public static final String KEY_PERMANENT_LUCK_DESC = descKey(PERMANENT_LUCK);
    public static final ResourceLocation TEMPORARY_LUCK = rl("temporary_luck");
    public static final String KEY_TEMPORARY_LUCK = key(TEMPORARY_LUCK);
    public static final String KEY_TEMPORARY_LUCK_DESC = descKey(TEMPORARY_LUCK);
    public static final ResourceLocation TOGGLE_LUCK = rl("toggle_luck");
    public static final String KEY_TOGGLE_LUCK = key(TOGGLE_LUCK);
    public static final String KEY_TOGGLE_LUCK_DESC = descKey(TOGGLE_LUCK);
    
    public static final ResourceLocation PERMANENT_CONDUIT_POWER = rl("permanent_conduit_power");
    public static final String KEY_PERMANENT_CONDUIT_POWER = key(PERMANENT_CONDUIT_POWER);
    public static final String KEY_PERMANENT_CONDUIT_POWER_DESC = descKey(PERMANENT_CONDUIT_POWER);
    public static final ResourceLocation TEMPORARY_CONDUIT_POWER = rl("temporary_conduit_power");
    public static final String KEY_TEMPORARY_CONDUIT_POWER = key(TEMPORARY_CONDUIT_POWER);
    public static final String KEY_TEMPORARY_CONDUIT_POWER_DESC = descKey(TEMPORARY_CONDUIT_POWER);
    public static final ResourceLocation TOGGLE_CONDUIT_POWER = rl("toggle_conduit_power");
    public static final String KEY_TOGGLE_CONDUIT_POWER = key(TOGGLE_CONDUIT_POWER);
    public static final String KEY_TOGGLE_CONDUIT_POWER_DESC = descKey(TOGGLE_CONDUIT_POWER);
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Spells::newRegistry);
    }
    
    private static void newRegistry(NewRegistryEvent event)
    {
        REGISTRY = event.create(new RegistryBuilder<Spell>().setMaxID(2048).dataPackRegistry(SpellsCodecs.SPELL_CONTENTS).setName(new ResourceLocation(SpellsAndShields.MOD_ID, "spells"))
                .onCreate((registry, stage) -> REGISTRY_KEY = registry.getRegistryKey())
        );
    }
    
    private static ResourceLocation rl(String path)
    {
        return new ResourceLocation(SpellsAndShields.MOD_ID, path);
    }
    
    public static String key(ResourceLocation rl, String suffix)
    {
        return "spell." + rl.getNamespace() + "." + rl.getPath() + (!suffix.isEmpty() ? "." + suffix : "");
    }
    
    public static String key(ResourceLocation rl)
    {
        return key(rl, "");
    }
    
    public static String descKey(ResourceLocation rl)
    {
        return key(rl, "desc");
    }
    
    public static String descKey(ResourceLocation rl, int index)
    {
        return key(rl, "desc_" + index);
    }
}
