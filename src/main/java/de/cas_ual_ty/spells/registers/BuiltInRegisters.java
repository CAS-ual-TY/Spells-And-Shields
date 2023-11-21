package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.command.SpellArgument;
import de.cas_ual_ty.spells.command.SpellCommand;
import de.cas_ual_ty.spells.command.SpellTreeArgument;
import de.cas_ual_ty.spells.effect.ExtraManaMobEffect;
import de.cas_ual_ty.spells.effect.InstantManaMobEffect;
import de.cas_ual_ty.spells.effect.ManaMobEffect;
import de.cas_ual_ty.spells.effect.SimpleEffect;
import de.cas_ual_ty.spells.enchantment.*;
import de.cas_ual_ty.spells.network.SpellProgressionSyncMessage;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.projectile.HomingSpellProjectile;
import de.cas_ual_ty.spells.spell.projectile.SpellProjectile;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.util.ProgressionHelper;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.network.NetworkHooks;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.List;

import static de.cas_ual_ty.spells.SpellsAndShields.MOD_ID;

public class BuiltInRegisters
{
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, MOD_ID);
    public static final DeferredHolder<Attribute, RangedAttribute> MAX_MANA_ATTRIBUTE = ATTRIBUTES.register("generic.max_mana", () -> (RangedAttribute) new RangedAttribute("attribute.name.generic.max_mana", 20D, 0D, 1024D).setSyncable(true));
    public static final DeferredHolder<Attribute, RangedAttribute> MANA_REGENERATION_ATTRIBUTE = ATTRIBUTES.register("generic.mana_regeneration", () -> (RangedAttribute) new RangedAttribute("attribute.name.generic.mana_regen", 1D, 0D, 50D).setSyncable(true));
    
    private static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(BuiltInRegistries.ENCHANTMENT, MOD_ID);
    public static final EnchantmentCategory SHIELD_ENCHANTMENT_CATEGORY = EnchantmentCategory.create("SHIELD", item -> item instanceof ShieldItem);
    public static final EnchantmentCategory SWORD_OR_AXE_ENCHANTMENT_CATEGORY = EnchantmentCategory.create("SWORD_OR_AXE", item -> item instanceof AxeItem || item instanceof SwordItem);
    public static final DeferredHolder<Enchantment, Enchantment> MAGIC_PROTECTION_ENCHANTMENT = ENCHANTMENTS.register("magic_protection", () -> new MagicProtectionEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET));
    public static final DeferredHolder<Enchantment, Enchantment> MANA_BLADE_ENCHANTMENT = ENCHANTMENTS.register("mana_blade", () -> new ManaBladeEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.MAINHAND));
    public static final DeferredHolder<Enchantment, Enchantment> MANA_SHIELD_ENCHANTMENT = ENCHANTMENTS.register("mana_shield", () -> new ManaShieldEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.OFFHAND));
    public static final DeferredHolder<Enchantment, MaxManaEnchantment> MAX_MANA_ENCHANTMENT = ENCHANTMENTS.register("max_mana", () -> new MaxManaEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET));
    public static final DeferredHolder<Enchantment, ManaRegenEnchantment> MANA_REGENERATION_ENCHANTMENT = ENCHANTMENTS.register("mana_regeneration", () -> new ManaRegenEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET));
    
    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, MOD_ID);
    public static final DeferredHolder<MobEffect, MobEffect> INSTANT_MANA_EFFECT = MOB_EFFECTS.register("instant_mana", () -> new InstantManaMobEffect(MobEffectCategory.BENEFICIAL, 0x06B7BD));
    public static final DeferredHolder<MobEffect, MobEffect> MANA_BOMB_EFFECT = MOB_EFFECTS.register("mana_bomb", () -> new InstantManaMobEffect(MobEffectCategory.HARMFUL, 0x820A60));
    public static final DeferredHolder<MobEffect, MobEffect> REPLENISHMENT_EFFECT = MOB_EFFECTS.register("replenishment", () -> new ManaMobEffect(MobEffectCategory.BENEFICIAL, 0x9E17BD));
    public static final DeferredHolder<MobEffect, MobEffect> LEAKING_MOB_EFFECT = MOB_EFFECTS.register("leaking", () -> new ManaMobEffect(MobEffectCategory.HARMFUL, 0x3EDE63));
    public static final DeferredHolder<MobEffect, MobEffect> MANA_BOOST_EFFECT = MOB_EFFECTS.register("mana_boost", () -> new SimpleEffect(MobEffectCategory.BENEFICIAL, 0x4E20B3).addAttributeModifier(MAX_MANA_ATTRIBUTE.get(), "65CAA54F-F98E-4AA0-99F1-B4AC438C6DB8", 0.5F, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final DeferredHolder<MobEffect, MobEffect> EXTRA_MANA_EFFECT = MOB_EFFECTS.register("extra_mana", () -> new ExtraManaMobEffect(MobEffectCategory.BENEFICIAL, 0x3E55E6));
    public static final DeferredHolder<MobEffect, MobEffect> SILENCE_EFFECT = MOB_EFFECTS.register("silence", () -> new SimpleEffect(MobEffectCategory.HARMFUL, 0x786634));
    public static final DeferredHolder<MobEffect, MobEffect> MAGIC_IMMUNE_EFFECT = MOB_EFFECTS.register("magic_immune", () -> new SimpleEffect(MobEffectCategory.BENEFICIAL, 0xFFC636));
    
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(BuiltInRegistries.POTION, MOD_ID);
    public static final DeferredHolder<Potion, Potion> INSTANT_MANA = POTIONS.register("instant_mana", () -> new Potion(new MobEffectInstance(INSTANT_MANA_EFFECT.get(), 1)));
    public static final DeferredHolder<Potion, Potion> STRONG_INSTANT_MANA = POTIONS.register("strong_instant_mana", () -> new Potion(new MobEffectInstance(INSTANT_MANA_EFFECT.get(), 1, 1)));
    public static final DeferredHolder<Potion, Potion> MANA_BOMB = POTIONS.register("mana_bomb", () -> new Potion(new MobEffectInstance(MANA_BOMB_EFFECT.get(), 1)));
    public static final DeferredHolder<Potion, Potion> STRONG_MANA_BOMB = POTIONS.register("strong_mana_bomb", () -> new Potion(new MobEffectInstance(MANA_BOMB_EFFECT.get(), 1, 1)));
    public static final DeferredHolder<Potion, Potion> REPLENISHMENT = POTIONS.register("replenishment", () -> new Potion(new MobEffectInstance(REPLENISHMENT_EFFECT.get(), 900)));
    public static final DeferredHolder<Potion, Potion> LONG_REPLENISHMENT = POTIONS.register("long_replenishment", () -> new Potion(new MobEffectInstance(REPLENISHMENT_EFFECT.get(), 1800)));
    public static final DeferredHolder<Potion, Potion> STRONG_REPLENISHMENT = POTIONS.register("strong_replenishment", () -> new Potion(new MobEffectInstance(REPLENISHMENT_EFFECT.get(), 450, 1)));
    public static final DeferredHolder<Potion, Potion> LEAKING = POTIONS.register("leaking", () -> new Potion(new MobEffectInstance(LEAKING_MOB_EFFECT.get(), 900)));
    public static final DeferredHolder<Potion, Potion> LONG_LEAKING = POTIONS.register("long_leaking", () -> new Potion(new MobEffectInstance(LEAKING_MOB_EFFECT.get(), 1800)));
    public static final DeferredHolder<Potion, Potion> STRONG_LEAKING = POTIONS.register("strong_leaking", () -> new Potion(new MobEffectInstance(LEAKING_MOB_EFFECT.get(), 432, 1)));
    
    private static final DeferredRegister<MenuType<?>> CONTAINER_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<SpellProgressionMenu>> SPELL_PROGRESSION_MENU = CONTAINER_TYPES.register("spell_progression", () -> new MenuType<>((IContainerFactory<SpellProgressionMenu>) SpellProgressionMenu::construct, FeatureFlags.DEFAULT_FLAGS));
    
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<SpellProjectile>> SPELL_PROJECTILE = ENTITY_TYPES.register("spell_projectile", () -> EntityType.Builder.<SpellProjectile>of(SpellProjectile::new, MobCategory.MISC).clientTrackingRange(20).updateInterval(10).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F).build("spell_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<HomingSpellProjectile>> HOMING_SPELL_PROJECTILE = ENTITY_TYPES.register("homing_spell_projectile", () -> EntityType.Builder.<HomingSpellProjectile>of(HomingSpellProjectile::new, MobCategory.MISC).clientTrackingRange(20).updateInterval(2).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F).build("homing_spell_projectile"));
    
    
    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, MOD_ID);
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<?, ?>> SPELL_ARGUMENT_TYPE = ARGUMENT_TYPES.register("spell", () -> ArgumentTypeInfos.registerByClass(SpellArgument.class, SingletonArgumentInfo.contextAware(SpellArgument::spell)));
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<?, ?>> SPELL_TREE_ARGUMENT_TYPE = ARGUMENT_TYPES.register("spell_tree", () -> ArgumentTypeInfos.registerByClass(SpellTreeArgument.class, SingletonArgumentInfo.contextAware(SpellTreeArgument::spellTree)));
    
    public static void register()
    {
        MOB_EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        POTIONS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ATTRIBUTES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENCHANTMENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINER_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ARGUMENT_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    
    public static void addPotionRecipes()
    {
        SpellsUtil.addPotionRecipes(Potions.AWKWARD, BuiltInRegisters.INSTANT_MANA.get(), BuiltInRegisters.STRONG_INSTANT_MANA.get(), null, Items.TUBE_CORAL, BuiltInRegisters.MANA_BOMB.get(), BuiltInRegisters.STRONG_MANA_BOMB.get(), null, Items.FERMENTED_SPIDER_EYE);
        SpellsUtil.addPotionRecipes(Potions.AWKWARD, BuiltInRegisters.REPLENISHMENT.get(), BuiltInRegisters.STRONG_REPLENISHMENT.get(), BuiltInRegisters.LONG_REPLENISHMENT.get(), Items.TUBE_CORAL_FAN, null, null, null, null);
        SpellsUtil.addPotionRecipes(Potions.AWKWARD, BuiltInRegisters.LEAKING.get(), BuiltInRegisters.STRONG_LEAKING.get(), BuiltInRegisters.LONG_LEAKING.get(), Items.DEAD_TUBE_CORAL_FAN, null, null, null, null);
    }
    
    private static void entityAttributeModification(EntityAttributeModificationEvent event)
    {
        event.add(EntityType.PLAYER, BuiltInRegisters.MAX_MANA_ATTRIBUTE.get());
        event.add(EntityType.PLAYER, BuiltInRegisters.MANA_REGENERATION_ATTRIBUTE.get());
    }
    
    private static void registerCommands(RegisterCommandsEvent event)
    {
        SpellCommand.register(event.getDispatcher(), event.getBuildContext());
    }
    
    private static void livingHurt(LivingHurtEvent event)
    {
        if(!event.getEntity().level().isClientSide && event.getSource().is(DamageTypes.MAGIC) && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) && event.getEntity().hasEffect(BuiltInRegisters.MAGIC_IMMUNE_EFFECT.get()))
        {
            event.setCanceled(true);
        }
    }
    
    private static void rightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        BlockPos pos = event.getPos();
        
        if(!event.getLevel().isClientSide && event.getEntity() instanceof ServerPlayer player && !player.hasContainerOpen() && SpellsUtil.isAltEnchantingTable(player.level().getBlockState(pos).getBlock()))
        {
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
            
            ContainerLevelAccess access = ContainerLevelAccess.create(player.level(), pos);
            
            SpellProgressionHolder.getSpellProgressionHolder(player).ifPresent(spellProgressionHolder ->
            {
                access.execute((level, blockPos) ->
                {
                    List<SpellTree> availableSpellTrees = ProgressionHelper.getStrippedSpellTrees(spellProgressionHolder, access);
                    HashMap<SpellNodeId, SpellStatus> progression = spellProgressionHolder.getProgression();
                    
                    Registry<Spell> registry = Spells.getRegistry(level);
                    
                    NetworkHooks.openScreen(player, new MenuProvider()
                    {
                        @Override
                        public Component getDisplayName()
                        {
                            return SpellProgressionMenu.TITLE;
                        }
                        
                        @Override
                        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player)
                        {
                            return new SpellProgressionMenu(id, inventory, access, availableSpellTrees, progression);
                        }
                    }, buf ->
                    {
                        SpellProgressionSyncMessage data = new SpellProgressionSyncMessage(blockPos, availableSpellTrees, progression, level);
                        SpellProgressionSyncMessage.encode(data, buf);
                    });
                });
            });
        }
    }
    
    public static void registerEvents()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(BuiltInRegisters::entityAttributeModification);
        NeoForge.EVENT_BUS.addListener(BuiltInRegisters::registerCommands);
        NeoForge.EVENT_BUS.addListener(BuiltInRegisters::livingHurt);
        NeoForge.EVENT_BUS.addListener(BuiltInRegisters::rightClickBlock);
    }
}
