package de.cas_ual_ty.spells;

import com.google.gson.JsonElement;
import de.cas_ual_ty.spells.effect.ExtraManaMobEffect;
import de.cas_ual_ty.spells.effect.InstantManaMobEffect;
import de.cas_ual_ty.spells.effect.ManaMobEffect;
import de.cas_ual_ty.spells.enchantment.MagicProtectionEnchantment;
import de.cas_ual_ty.spells.enchantment.ManaBladeEnchantment;
import de.cas_ual_ty.spells.enchantment.ManaShieldEnchantment;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.recipe.TippedSpearRecipe;
import de.cas_ual_ty.spells.spell.*;
import de.cas_ual_ty.spells.spell.base.*;
import de.cas_ual_ty.spells.spell.tree.AquaAffinitySpell;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.*;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Supplier;

import static de.cas_ual_ty.spells.SpellsAndShields.MOD_ID;

public class SpellsRegistries
{
    public static Supplier<IForgeRegistry<ISpell>> SPELLS_REGISTRY;
    
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, MOD_ID);
    private static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, MOD_ID);
    
    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MOD_ID);
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, MOD_ID);
    
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);
    
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_ID);
    
    private static final DeferredRegister<ISpell> SPELLS = DeferredRegister.create(new ResourceLocation(MOD_ID, "spells"), MOD_ID);
    
    public static final RegistryObject<RangedAttribute> MAX_MANA = ATTRIBUTES.register("generic.max_mana", () -> (RangedAttribute) new RangedAttribute("attribute.name.generic.max_mana", 20.0D, 1.0D, 1024.0D).setSyncable(true));
    public static final RegistryObject<Enchantment> MAGIC_PROTECTION = ENCHANTMENTS.register("magic_protection", () -> new MagicProtectionEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET));
    public static final RegistryObject<Enchantment> MANA_BLADE = ENCHANTMENTS.register("mana_blade", () -> new ManaBladeEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.MAINHAND));
    public static final RegistryObject<Enchantment> MANA_SHIELD = ENCHANTMENTS.register("mana_shield", () -> new ManaShieldEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.OFFHAND));
    
    public static final RegistryObject<InstantManaMobEffect> INSTANT_MANA_EFFECT = MOB_EFFECTS.register("instant_mana", () -> new InstantManaMobEffect(MobEffectCategory.BENEFICIAL, 0x06B7BD));
    public static final RegistryObject<InstantManaMobEffect> MANA_BOMB_EFFECT = MOB_EFFECTS.register("mana_bomb", () -> new InstantManaMobEffect(MobEffectCategory.HARMFUL, 0x820A60));
    public static final RegistryObject<ManaMobEffect> REPLENISHMENT_EFFECT = MOB_EFFECTS.register("replenishment", () -> new ManaMobEffect(MobEffectCategory.BENEFICIAL, 0x9E17BD));
    public static final RegistryObject<ManaMobEffect> LEAKING_MOB_EFFECT = MOB_EFFECTS.register("leaking", () -> new ManaMobEffect(MobEffectCategory.HARMFUL, 0x3EDE63));
    public static final RegistryObject<MobEffect> MANA_BOOST_EFFECT = MOB_EFFECTS.register("mana_boost", () -> new ManaMobEffect(MobEffectCategory.BENEFICIAL, 0x4E20B3).addAttributeModifier(MAX_MANA.get(), "65CAA54F-F98E-4AA0-99F1-B4AC438C6DB8", 0.5F, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final RegistryObject<MobEffect> EXTRA_MANA_EFFECT = MOB_EFFECTS.register("extra_mana", () -> new ExtraManaMobEffect(MobEffectCategory.BENEFICIAL, 0x3E55E6));
    
    public static final RegistryObject<Potion> INSTANT_MANA = POTIONS.register("instant_mana", () -> new Potion(new MobEffectInstance(INSTANT_MANA_EFFECT.get(), 1)));
    public static final RegistryObject<Potion> STRONG_INSTANT_MANA = POTIONS.register("strong_instant_mana", () -> new Potion(new MobEffectInstance(INSTANT_MANA_EFFECT.get(), 1, 1)));
    
    public static final RegistryObject<Potion> MANA_BOMB = POTIONS.register("mana_bomb", () -> new Potion(new MobEffectInstance(MANA_BOMB_EFFECT.get(), 1)));
    public static final RegistryObject<Potion> STRONG_MANA_BOMB = POTIONS.register("strong_mana_bomb", () -> new Potion(new MobEffectInstance(MANA_BOMB_EFFECT.get(), 1, 1)));
    
    public static final RegistryObject<Potion> REPLENISHMENT = POTIONS.register("replenishment", () -> new Potion(new MobEffectInstance(REPLENISHMENT_EFFECT.get(), 900)));
    public static final RegistryObject<Potion> LONG_REPLENISHMENT = POTIONS.register("long_replenishment", () -> new Potion(new MobEffectInstance(REPLENISHMENT_EFFECT.get(), 1800)));
    public static final RegistryObject<Potion> STRONG_REPLENISHMENT = POTIONS.register("strong_replenishment", () -> new Potion(new MobEffectInstance(REPLENISHMENT_EFFECT.get(), 450, 1)));
    
    public static final RegistryObject<Potion> LEAKING = POTIONS.register("leaking", () -> new Potion(new MobEffectInstance(LEAKING_MOB_EFFECT.get(), 900)));
    public static final RegistryObject<Potion> LONG_LEAKING = POTIONS.register("long_leaking", () -> new Potion(new MobEffectInstance(LEAKING_MOB_EFFECT.get(), 1800)));
    public static final RegistryObject<Potion> STRONG_LEAKING = POTIONS.register("strong_leaking", () -> new Potion(new MobEffectInstance(LEAKING_MOB_EFFECT.get(), 432, 1)));
    
    public static final RegistryObject<MenuType<SpellProgressionMenu>> SPELL_PROGRESSION_MENU = CONTAINERS.register("spell_progression", () -> new MenuType<>((IContainerFactory<SpellProgressionMenu>) SpellProgressionMenu::construct));
    public static final RegistryObject<SimpleRecipeSerializer<?>> TIPPED_SPEAR = RECIPE_SERIALIZERS.register("tipped_spear", () -> new SimpleRecipeSerializer<>(TippedSpearRecipe::new));
    
    public static final RegistryObject<EntityType<SpellProjectile>> SPELL_PROJECTILE = ENTITY_TYPES.register("spell_projectile", () -> EntityType.Builder.<SpellProjectile>of(SpellProjectile::new, MobCategory.MISC).clientTrackingRange(20).updateInterval(10).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F).build("spell_projectile"));
    public static final RegistryObject<EntityType<HomingSpellProjectile>> HOMING_SPELL_PROJECTILE = ENTITY_TYPES.register("homing_spell_projectile", () -> EntityType.Builder.<HomingSpellProjectile>of(HomingSpellProjectile::new, MobCategory.MISC).clientTrackingRange(20).updateInterval(2).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F).build("homing_spell_projectile"));
    
    public static final RegistryObject<ISpell> LEAP = SPELLS.register("leap", () -> new LeapSpell(7F));
    public static final RegistryObject<ISpell> SUMMON_ANIMAL = SPELLS.register("summon_animal", () -> new SummonAnimalSpell(18F));
    public static final RegistryObject<ISpell> FIRE_BALL = SPELLS.register("fire_ball", () -> new FireSpell(5F));
    public static final RegistryObject<ISpell> SMELT = SPELLS.register("smelt", () -> new SmeltSpell(6F).setSmallIcon(new ResourceLocation("textures/item/coal.png")));
    public static final RegistryObject<ISpell> TRANSFER_MANA = SPELLS.register("transfer_mana", () -> new TransferManaSpell(4F));
    public static final RegistryObject<ISpell> POCKET_BOW = SPELLS.register("pocket_bow", () -> new BowSpell(7F).setSmallIcon(new ResourceLocation("textures/item/bow_pulling_0.png")));
    public static final RegistryObject<ISpell> HEALTH_BOOST = SPELLS.register("health_boost", () -> new AttributeSpell(() -> Attributes.MAX_HEALTH, 4.0D, AttributeModifier.Operation.ADDITION).setIcon(new ResourceLocation("textures/mob_effect/health_boost.png")));
    public static final RegistryObject<ISpell> MANA_BOOST = SPELLS.register("mana_boost", () -> new AttributeSpell(MAX_MANA::get, 4.0D, AttributeModifier.Operation.ADDITION).setIcon(new ResourceLocation(MOD_ID, "textures/mob_effect/mana_boost.png")));
    public static final RegistryObject<ISpell> PASSIVE_SPEED = SPELLS.register("passive_speed", () -> new MobEffectSpell(MobEffects.MOVEMENT_SPEED));
    public static final RegistryObject<ISpell> PASSIVE_JUMP_BOOST = SPELLS.register("passive_jump_boost", () -> new MobEffectSpell(MobEffects.JUMP));
    public static final RegistryObject<ISpell> PASSIVE_DOLPHINS_GRACE = SPELLS.register("passive_dolphins_grace", () -> new MobEffectSpell(MobEffects.DOLPHINS_GRACE));
    public static final RegistryObject<ISpell> WATER_LEAP = SPELLS.register("water_leap", () -> new WaterLeapSpell(7F));
    public static final RegistryObject<ISpell> PASSIVE_AQUA_AFFINITY = SPELLS.register("passive_aqua_affinity", () -> new AquaAffinitySpell().setSmallIcon(new ResourceLocation("textures/item/enchanted_book.png")));
    public static final RegistryObject<ISpell> PASSIVE_WATER_BREATHING = SPELLS.register("passive_water_breathing", () -> new MobEffectSpell(MobEffects.WATER_BREATHING));
    public static final RegistryObject<ISpell> PASSIVE_SLOW_FALLING = SPELLS.register("passive_slow_falling", () -> new MobEffectSpell(MobEffects.SLOW_FALLING));
    public static final RegistryObject<ISpell> PASSIVE_DIG_SPEED = SPELLS.register("passive_dig_speed", () -> new MobEffectSpell(MobEffects.DIG_SPEED));
    public static final RegistryObject<ISpell> PASSIVE_REGENERATION = SPELLS.register("passive_regeneration", () -> new MobEffectSpell(MobEffects.REGENERATION, 50));
    public static final RegistryObject<ISpell> PASSIVE_REPLENISHMENT = SPELLS.register("passive_replenishment", () -> new MobEffectSpell(REPLENISHMENT_EFFECT.get(), 50));
    public static final RegistryObject<ISpell> WATER_WHIP = SPELLS.register("water_whip", () -> new WaterWhipSpell(2.0F).setSmallIcon(new ResourceLocation("textures/item/water_bucket.png")));
    public static final RegistryObject<ISpell> POTION_SHOT = SPELLS.register("potion_shot", () -> new ShootPotionSpell(2.0F).setSmallIcon(new ResourceLocation("textures/item/potion.png")));
    public static final RegistryObject<ISpell> PASSIVE_FROST_WALKER = SPELLS.register("frost_walker", () -> new WalkerSpell().setSmallIcon(new ResourceLocation("textures/item/golden_boots.png")));
    public static final RegistryObject<ISpell> JUMP = SPELLS.register("jump", () -> new JumpSpell(5F).setSmallIcon(new ResourceLocation("textures/item/feather.png")));
    public static final RegistryObject<ISpell> FALL_DAMAGE_REDUCTION = SPELLS.register("fall_damage_reduction", () -> new ReduceFallDamageSpell().setSmallIcon(new ResourceLocation("textures/item/diamond_boots.png")));
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpellsRegistries::newRegistry);
        
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        MOB_EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        POTIONS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ATTRIBUTES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENCHANTMENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        SPELLS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    
    private static void newRegistry(NewRegistryEvent event)
    {
        SPELLS_REGISTRY = event.create(new RegistryBuilder<ISpell>().setType(ISpell.class).setMaxID(1024).setName(new ResourceLocation(MOD_ID, "spells")));
    }
    
    public static void spellsConfigs()
    {
        Path p = SpellsFileUtil.getOrCreateSubConfigDir("spells");
        
        File folder = p.toFile();
        
        if(!folder.isDirectory() || folder.listFiles() == null)
        {
            SpellsAndShields.LOGGER.error("Can not read or write spell config files in {} (is it a folder?).", p);
            SPELLS_REGISTRY.get().getValues().stream().filter(s -> s instanceof IConfigurableSpell).map(s -> (IConfigurableSpell) s).forEach(IConfigurableSpell::applyDefaultConfig);
            return;
        }
        
        SPELLS_REGISTRY.get().getValues().stream().filter(s -> s instanceof IConfigurableSpell).map(s -> (IConfigurableSpell) s).forEach(spell ->
        {
            File f = p.resolve(spell.getFileName() + ".json").toFile();
            
            if(!f.exists())
            {
                try
                {
                    SpellsFileUtil.writeJsonToFile(f, spell.makeDefaultConfig());
                    SpellsAndShields.LOGGER.info("Successfully wrote default config of spell {} to file {}.", spell.getRegistryName().toString(), f.toPath());
                }
                catch(Exception e)
                {
                    SpellsAndShields.LOGGER.error("Failed writing default config of spell {} to file {}.", spell.getRegistryName().toString(), f.toPath(), e);
                    e.printStackTrace();
                }
                
                spell.applyDefaultConfig();
            }
            else
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
                    SpellsAndShields.LOGGER.error("Failed reading config of spell {} from file {}, applying default config.", spell.getRegistryName().toString(), f.toPath(), e);
                    e.printStackTrace();
                    spell.applyDefaultConfig();
                }
                
                if(json != null && json.isJsonObject())
                {
                    try
                    {
                        spell.readFromConfig(json.getAsJsonObject());
                        SpellsAndShields.LOGGER.info("Successfully read config of spell {} from file {}.", spell.getRegistryName().toString(), f.toPath());
                    }
                    catch(IllegalStateException e)
                    {
                        SpellsAndShields.LOGGER.error("Failed reading config of spell {} from file {}, applying default config.", spell.getRegistryName().toString(), f.toPath(), e);
                        e.printStackTrace();
                        spell.applyDefaultConfig();
                    }
                }
                else if(!failed)
                {
                    SpellsAndShields.LOGGER.error("Failed reading config of spell {} from file {}, applying default config.", spell.getRegistryName().toString(), f.toPath());
                    spell.applyDefaultConfig();
                }
            }
        });
    }
    
    public static void registerEventSpells()
    {
        SpellsRegistries.SPELLS_REGISTRY.get().forEach(spell ->
        {
            if(spell instanceof IEventSpell eventSpell)
            {
                eventSpell.registerEvents();
            }
        });
    }
}
