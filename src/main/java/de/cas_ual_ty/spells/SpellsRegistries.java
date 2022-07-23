package de.cas_ual_ty.spells;

import de.cas_ual_ty.spells.command.SpellArgument;
import de.cas_ual_ty.spells.command.SpellCommand;
import de.cas_ual_ty.spells.effect.ExtraManaMobEffect;
import de.cas_ual_ty.spells.effect.InstantManaMobEffect;
import de.cas_ual_ty.spells.effect.ManaMobEffect;
import de.cas_ual_ty.spells.enchantment.MagicProtectionEnchantment;
import de.cas_ual_ty.spells.enchantment.ManaBladeEnchantment;
import de.cas_ual_ty.spells.enchantment.ManaShieldEnchantment;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.recipe.TippedSpearRecipe;
import de.cas_ual_ty.spells.spell.base.HomingSpellProjectile;
import de.cas_ual_ty.spells.spell.base.SpellProjectile;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static de.cas_ual_ty.spells.SpellsAndShields.MOD_ID;

public class SpellsRegistries
{
    
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, MOD_ID);
    private static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, MOD_ID);
    
    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MOD_ID);
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, MOD_ID);
    
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);
    
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
    
    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(Registry.COMMAND_ARGUMENT_TYPE_REGISTRY, MOD_ID);
    public static final RegistryObject<ArgumentTypeInfo<?, ?>> SPELL_ARGUMENT_TYPE = ARGUMENT_TYPES.register("spell", () -> ArgumentTypeInfos.registerByClass(SpellArgument.class, SingletonArgumentInfo.contextAware(SpellArgument::spell)));
    
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
    
    public static void register()
    {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        MOB_EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        POTIONS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ATTRIBUTES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENCHANTMENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ARGUMENT_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    
    private static void registerCommands(RegisterCommandsEvent event)
    {
        SpellCommand.register(event.getDispatcher(), event.getBuildContext());
    }
    
    private static void entityAttributeModification(EntityAttributeModificationEvent event)
    {
        event.add(EntityType.PLAYER, SpellsRegistries.MAX_MANA.get());
    }
    
    public static void registerEvents()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpellsRegistries::entityAttributeModification);
        MinecraftForge.EVENT_BUS.addListener(SpellsRegistries::registerCommands);
    }
}
