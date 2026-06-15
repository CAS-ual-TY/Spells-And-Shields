package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.command.SpellArgument;
import de.cas_ual_ty.spells.command.SpellCommand;
import de.cas_ual_ty.spells.command.SpellTreeArgument;
import de.cas_ual_ty.spells.effect.ExtraManaMobEffect;
import de.cas_ual_ty.spells.effect.InstantManaMobEffect;
import de.cas_ual_ty.spells.effect.ManaMobEffect;
import de.cas_ual_ty.spells.effect.SimpleEffect;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
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

    public static final ResourceKey<Enchantment> MANA_BLADE_KEY = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(MOD_ID, "mana_blade"));
    public static final ResourceKey<Enchantment> MAGIC_PROTECTION_KEY = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_protection"));
    public static final ResourceKey<Enchantment> MANA_SHIELD_KEY = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(MOD_ID, "mana_shield"));
    public static final ResourceKey<Enchantment> MAX_MANA_KEY = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(MOD_ID, "max_mana"));
    public static final ResourceKey<Enchantment> MANA_REGEN_KEY = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(MOD_ID, "mana_regen"));

    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, MOD_ID);
    public static final DeferredHolder<MobEffect, MobEffect> INSTANT_MANA_EFFECT = MOB_EFFECTS.register("instant_mana", () -> new InstantManaMobEffect(MobEffectCategory.BENEFICIAL, 0x06B7BD));
    public static final DeferredHolder<MobEffect, MobEffect> MANA_BOMB_EFFECT = MOB_EFFECTS.register("mana_bomb", () -> new InstantManaMobEffect(MobEffectCategory.HARMFUL, 0x820A60));
    public static final DeferredHolder<MobEffect, MobEffect> REPLENISHMENT_EFFECT = MOB_EFFECTS.register("replenishment", () -> new ManaMobEffect(MobEffectCategory.BENEFICIAL, 0x9E17BD));
    public static final DeferredHolder<MobEffect, MobEffect> LEAKING_MOB_EFFECT = MOB_EFFECTS.register("leaking", () -> new ManaMobEffect(MobEffectCategory.HARMFUL, 0x3EDE63));
    public static final DeferredHolder<MobEffect, MobEffect> MANA_BOOST_EFFECT = MOB_EFFECTS.register("mana_boost", () -> new SimpleEffect(MobEffectCategory.BENEFICIAL, 0x4E20B3).addAttributeModifier(MAX_MANA_ATTRIBUTE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "mana_boost"), 0.5F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final DeferredHolder<MobEffect, MobEffect> EXTRA_MANA_EFFECT = MOB_EFFECTS.register("extra_mana", () -> new ExtraManaMobEffect(MobEffectCategory.BENEFICIAL, 0x3E55E6));
    public static final DeferredHolder<MobEffect, MobEffect> SILENCE_EFFECT = MOB_EFFECTS.register("silence", () -> new SimpleEffect(MobEffectCategory.HARMFUL, 0x786634));
    public static final DeferredHolder<MobEffect, MobEffect> MAGIC_IMMUNE_EFFECT = MOB_EFFECTS.register("magic_immune", () -> new SimpleEffect(MobEffectCategory.BENEFICIAL, 0xFFC636));

    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(BuiltInRegistries.POTION, MOD_ID);
    public static final DeferredHolder<Potion, Potion> INSTANT_MANA = POTIONS.register("instant_mana", () -> new Potion(new MobEffectInstance(INSTANT_MANA_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> STRONG_INSTANT_MANA = POTIONS.register("strong_instant_mana", () -> new Potion(new MobEffectInstance(INSTANT_MANA_EFFECT, 1, 1)));
    public static final DeferredHolder<Potion, Potion> MANA_BOMB = POTIONS.register("mana_bomb", () -> new Potion(new MobEffectInstance(MANA_BOMB_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> STRONG_MANA_BOMB = POTIONS.register("strong_mana_bomb", () -> new Potion(new MobEffectInstance(MANA_BOMB_EFFECT, 1, 1)));
    public static final DeferredHolder<Potion, Potion> REPLENISHMENT = POTIONS.register("replenishment", () -> new Potion(new MobEffectInstance(REPLENISHMENT_EFFECT, 900)));
    public static final DeferredHolder<Potion, Potion> LONG_REPLENISHMENT = POTIONS.register("long_replenishment", () -> new Potion(new MobEffectInstance(REPLENISHMENT_EFFECT, 1800)));
    public static final DeferredHolder<Potion, Potion> STRONG_REPLENISHMENT = POTIONS.register("strong_replenishment", () -> new Potion(new MobEffectInstance(REPLENISHMENT_EFFECT, 450, 1)));
    public static final DeferredHolder<Potion, Potion> LEAKING = POTIONS.register("leaking", () -> new Potion(new MobEffectInstance(LEAKING_MOB_EFFECT, 900)));
    public static final DeferredHolder<Potion, Potion> LONG_LEAKING = POTIONS.register("long_leaking", () -> new Potion(new MobEffectInstance(LEAKING_MOB_EFFECT, 1800)));
    public static final DeferredHolder<Potion, Potion> STRONG_LEAKING = POTIONS.register("strong_leaking", () -> new Potion(new MobEffectInstance(LEAKING_MOB_EFFECT, 432, 1)));

    private static final DeferredRegister<MenuType<?>> CONTAINER_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<SpellProgressionMenu>> SPELL_PROGRESSION_MENU = CONTAINER_TYPES.register("spell_progression", () -> IMenuTypeExtension.create(SpellProgressionMenu::construct));

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<SpellProjectile>> SPELL_PROJECTILE = ENTITY_TYPES.register("spell_projectile", () -> EntityType.Builder.<SpellProjectile>of(SpellProjectile::new, MobCategory.MISC).clientTrackingRange(20).updateInterval(10).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F).build("spell_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<HomingSpellProjectile>> HOMING_SPELL_PROJECTILE = ENTITY_TYPES.register("homing_spell_projectile", () -> EntityType.Builder.<HomingSpellProjectile>of(HomingSpellProjectile::new, MobCategory.MISC).clientTrackingRange(20).updateInterval(2).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F).build("homing_spell_projectile"));

    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, MOD_ID);
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<?, ?>> SPELL_ARGUMENT_TYPE = ARGUMENT_TYPES.register("spell", () -> ArgumentTypeInfos.registerByClass(SpellArgument.class, SingletonArgumentInfo.contextAware(SpellArgument::spell)));
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<?, ?>> SPELL_TREE_ARGUMENT_TYPE = ARGUMENT_TYPES.register("spell_tree", () -> ArgumentTypeInfos.registerByClass(SpellTreeArgument.class, SingletonArgumentInfo.contextAware(SpellTreeArgument::spellTree)));

    public static void register(IEventBus modEventBus)
    {
        MOB_EFFECTS.register(modEventBus);
        POTIONS.register(modEventBus);
        ATTRIBUTES.register(modEventBus);
        CONTAINER_TYPES.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        ARGUMENT_TYPES.register(modEventBus);
    }

    public static void addPotionRecipes(RegisterBrewingRecipesEvent event)
    {
        PotionBrewing.Builder builder = event.getBuilder();
        SpellsUtil.addPotionRecipes(Potions.AWKWARD, BuiltInRegisters.INSTANT_MANA, BuiltInRegisters.STRONG_INSTANT_MANA, null, Items.TUBE_CORAL, BuiltInRegisters.MANA_BOMB, BuiltInRegisters.STRONG_MANA_BOMB, null, Items.FERMENTED_SPIDER_EYE, builder);
        SpellsUtil.addPotionRecipes(Potions.AWKWARD, BuiltInRegisters.REPLENISHMENT, BuiltInRegisters.STRONG_REPLENISHMENT, BuiltInRegisters.LONG_REPLENISHMENT, Items.TUBE_CORAL_FAN, null, null, null, null, builder);
        SpellsUtil.addPotionRecipes(Potions.AWKWARD, BuiltInRegisters.LEAKING, BuiltInRegisters.STRONG_LEAKING, BuiltInRegisters.LONG_LEAKING, Items.DEAD_TUBE_CORAL_FAN, null, null, null, null, builder);
    }

    private static void entityAttributeModification(EntityAttributeModificationEvent event)
    {
        event.add(EntityType.PLAYER, BuiltInRegisters.MAX_MANA_ATTRIBUTE);
        event.add(EntityType.PLAYER, BuiltInRegisters.MANA_REGENERATION_ATTRIBUTE);
    }

    private static void registerCommands(RegisterCommandsEvent event)
    {
        SpellCommand.register(event.getDispatcher(), event.getBuildContext());
    }

    private static void livingHurt(LivingIncomingDamageEvent event)
    {
        if(event.getEntity().level().isClientSide)
        {
            return;
        }

        // Magic Immune effect
        if(event.getSource().is(DamageTypes.MAGIC) && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) && event.getEntity().hasEffect(BuiltInRegisters.MAGIC_IMMUNE_EFFECT))
        {
            event.setCanceled(true);
            return;
        }

        // Mana Blade — skip for indirect magic to prevent recursion from our own damage
        if(event.getSource().is(DamageTypes.INDIRECT_MAGIC))
        {
            return;
        }

        if(event.getEntity() instanceof LivingEntity victim && event.getSource().getEntity() instanceof LivingEntity attacker)
        {
            ItemStack weapon = attacker.getMainHandItem();

            int bladeLevel = event.getEntity().level().registryAccess()
                    .lookup(Registries.ENCHANTMENT)
                    .flatMap(reg -> reg.get(MANA_BLADE_KEY))
                    .map(h -> EnchantmentHelper.getItemEnchantmentLevel(h, weapon))
                    .orElse(0);

            if(bladeLevel > 0)
            {
                ManaHolder.getManaHolder(attacker).ifPresent(manaHolder ->
                {
                    if(manaHolder.getMana() > 2F)
                    {
                        float damage = Math.min(manaHolder.getMana(), (float) bladeLevel * 2F);
                        manaHolder.burn(5F);
                        victim.hurt(victim.level().damageSources().indirectMagic(attacker, null), damage);

                        int duration = 20 + attacker.getRandom().nextInt(10 * bladeLevel);
                        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 3));

                        Vec3 eyes = victim.getEyePosition();
                        for(int i = 0; i < 10; i++)
                        {
                            victim.level().addParticle(ParticleTypes.ENCHANTED_HIT,
                                    eyes.x + attacker.getRandom().nextGaussian() * 0.5,
                                    eyes.y + attacker.getRandom().nextGaussian() * 0.5,
                                    eyes.z + attacker.getRandom().nextGaussian() * 0.5,
                                    0, 0, 0);
                        }
                    }
                });
            }
        }
    }

    private static void rightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        BlockPos pos = event.getPos();

        if(!event.getLevel().isClientSide && event.getEntity() instanceof ServerPlayer player && !player.hasContainerOpen() && SpellsUtil.isAltEnchantingTable(player.level().getBlockState(pos).getBlock()))
        {
            event.setUseBlock(TriState.FALSE);
            event.setUseItem(TriState.FALSE);

            ContainerLevelAccess access = ContainerLevelAccess.create(player.level(), pos);

            SpellProgressionHolder.getSpellProgressionHolder(player).ifPresent(spellProgressionHolder ->
            {
                access.execute((level, blockPos) ->
                {
                    List<SpellTree> availableSpellTrees = ProgressionHelper.getStrippedSpellTrees(spellProgressionHolder, access);
                    HashMap<SpellNodeId, SpellStatus> progression = spellProgressionHolder.getProgression();

                    player.openMenu(new MenuProvider()
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
                    }, buf -> SpellProgressionSyncMessage.STREAM_CODEC.encode(buf, new SpellProgressionSyncMessage(blockPos, availableSpellTrees, progression)));
                });
            });
        }
    }

    public static void registerEvents(IEventBus modEventBus)
    {
        modEventBus.addListener(BuiltInRegisters::entityAttributeModification);
        NeoForge.EVENT_BUS.addListener(BuiltInRegisters::registerCommands);
        NeoForge.EVENT_BUS.addListener(BuiltInRegisters::livingHurt);
        NeoForge.EVENT_BUS.addListener(BuiltInRegisters::rightClickBlock);
    }
}
