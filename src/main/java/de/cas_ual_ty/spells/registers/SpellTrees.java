package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.requirement.*;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.icon.DefaultSpellIcon;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.util.SpellsCodecs;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Function;
import java.util.function.Supplier;

public class SpellTrees
{
    private static Supplier<IForgeRegistry<SpellTree>> REGISTRY;
    public static ResourceKey<Registry<SpellTree>> REGISTRY_KEY;
    
    public static Registry<SpellTree> getRegistry(LevelAccessor level)
    {
        return getRegistry(level.registryAccess());
    }
    
    public static Registry<SpellTree> getRegistry(RegistryAccess access)
    {
        return access.registryOrThrow(REGISTRY_KEY);
    }
    
    public static final String KEY_NETHER = "spell_tree." + SpellsAndShields.MOD_ID + ".nether";
    public static final String KEY_OCEAN = "spell_tree." + SpellsAndShields.MOD_ID + ".ocean";
    public static final String KEY_MINING = "spell_tree." + SpellsAndShields.MOD_ID + ".mining";
    public static final String KEY_MOVEMENT = "spell_tree." + SpellsAndShields.MOD_ID + ".movement";
    public static final String KEY_END = "spell_tree." + SpellsAndShields.MOD_ID + ".end";
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpellTrees::newRegistry);
        MinecraftForge.EVENT_BUS.addListener(SpellTrees::levelLoad);
    }
    
    private static void newRegistry(NewRegistryEvent event)
    {
        REGISTRY = event.create(new RegistryBuilder<SpellTree>().setMaxID(1024).dataPackRegistry(SpellsCodecs.SPELL_TREE_CONTENTS, SpellsCodecs.SPELL_TREE_SYNC).setName(new ResourceLocation(SpellsAndShields.MOD_ID, "spell_trees"))
                .onCreate((registry, stage) -> REGISTRY_KEY = registry.getRegistryKey())
        );
    }
    
    private static void levelLoad(LevelEvent.Load event)
    {
        if(event.getLevel().isClientSide())
        {
            return;
        }
        
        Registry<SpellTree> registry = getRegistry(event.getLevel());
        registry.forEach(spellTree -> spellTree.assignNodeIds(registry.getKey(spellTree)));
    }
    
    public static SpellTree fireTree(Function<ResourceLocation, Holder<Spell>> spellGetter)
    {
        return SpellTree.builder(Component.translatable(KEY_NETHER))
                .icon(DefaultSpellIcon.make(new ResourceLocation("textures/mob_effect/fire_resistance.png")))
                .add(spellGetter.apply(Spells.FIRE_BALL)).levelCost(15).learnRequirements(bookshelves(28)).hiddenRequirements(config())
                .add(spellGetter.apply(Spells.TOGGLE_LAVA_WALKER)).levelCost(20).learnRequirements(bookshelves(19))
                .add(spellGetter.apply(Spells.TOGGLE_FIRE_RESISTANCE)).levelCost(30).learnRequirements(bookshelves(30))
                .leaf()
                .add(spellGetter.apply(Spells.DRAIN_FLAME)).levelCost(20).learnRequirements(bookshelves(20))
                .leaf()
                .leaf()
                .add(spellGetter.apply(Spells.GHAST)).levelCost(10).learnRequirements(bookshelves(20), item(Items.GHAST_TEAR, 1, true))
                .add(spellGetter.apply(Spells.FLAMETHROWER)).levelCost(20).learnRequirements(bookshelves(24))
                .finish();
    }
    
    public static SpellTree waterTree(Function<ResourceLocation, Holder<Spell>> spellGetter)
    {
        return SpellTree.builder(Component.translatable(KEY_OCEAN))
                .icon(DefaultSpellIcon.make(new ResourceLocation("textures/mob_effect/dolphins_grace.png")))
                .add(spellGetter.apply(Spells.TOGGLE_WATER_BREATHING)).levelCost(10).hiddenRequirements(config())
                .add(spellGetter.apply(Spells.TOGGLE_REGENERATION)).levelCost(20).learnRequirements(bookshelves(20))
                .add(spellGetter.apply(Spells.GROWTH)).levelCost(20).learnRequirements(bookshelves(20))
                .leaf()
                .add(spellGetter.apply(Spells.DUMMY)).levelCost(20).learnRequirements(bookshelves(20))
                .leaf()
                .leaf()
                .add(spellGetter.apply(Spells.WATER_LEAP)).levelCost(5).learnRequirements(bookshelves(10))
                .add(spellGetter.apply(Spells.TOGGLE_DOLPHINS_GRACE)).levelCost(30).learnRequirements(bookshelves(30))
                .leaf()
                .add(spellGetter.apply(Spells.TOGGLE_FROST_WALKER)).levelCost(10).learnRequirements(bookshelves(14))
                .leaf()
                .leaf()
                .add(spellGetter.apply(Spells.WATER_WHIP)).levelCost(10).learnRequirements(bookshelves(10))
                .add(spellGetter.apply(Spells.POTION_SHOT)).levelCost(10).learnRequirements(bookshelves(10))
                .leaf()
                .leaf()
                .add(spellGetter.apply(Spells.LIGHTNING_STRIKE)).levelCost(25).learnRequirements(bookshelves(24), advancement("adventure/lightning_rod_with_villager_no_fire"))
                .leaf()
                .finish();
    }
    
    public static SpellTree earthTree(Function<ResourceLocation, Holder<Spell>> spellGetter)
    {
        return SpellTree.builder(Component.translatable(KEY_MINING))
                .icon(DefaultSpellIcon.make(new ResourceLocation("textures/mob_effect/haste.png")))
                .add(spellGetter.apply(Spells.BLAST_SMELT)).levelCost(5).learnRequirements(bookshelves(8)).hiddenRequirements(config())
                .add(spellGetter.apply(Spells.SILENCE_TARGET)).levelCost(25).learnRequirements(bookshelves(26))
                .add(spellGetter.apply(Spells.TOGGLE_MAGIC_IMMUNE)).levelCost(25).learnRequirements(bookshelves(26))
                .leaf()
                .leaf()
                .add(spellGetter.apply(Spells.INSTANT_MINE)).levelCost(15).learnRequirements(bookshelves(18))
                .add(spellGetter.apply(Spells.TOGGLE_HASTE)).levelCost(25).learnRequirements(bookshelves(24))
                .leaf()
                .leaf()
                .add(spellGetter.apply(Spells.SPIT_METAL)).levelCost(10).learnRequirements(bookshelves(12))
                .finish();
    }
    
    public static SpellTree airTree(Function<ResourceLocation, Holder<Spell>> spellGetter)
    {
        return SpellTree.builder(Component.translatable(KEY_MOVEMENT))
                .icon(DefaultSpellIcon.make(new ResourceLocation("textures/mob_effect/jump_boost.png")))
                .add(spellGetter.apply(Spells.TOGGLE_JUMP_BOOST)).levelCost(15).learnRequirements(bookshelves(12)).hiddenRequirements(config())
                .add(spellGetter.apply(Spells.LEAP)).levelCost(10).learnRequirements(bookshelves(14))
                .add(spellGetter.apply(Spells.TOGGLE_SPEED)).levelCost(20).learnRequirements(bookshelves(20))
                .leaf()
                .add(spellGetter.apply(Spells.JUMP)).levelCost(14).learnRequirements(bookshelves(14))
                .add(spellGetter.apply(Spells.POCKET_ROCKET)).levelCost(25).learnRequirements(bookshelves(20))
                .leaf()
                .leaf()
                .add(spellGetter.apply(Spells.MANA_SOLES)).levelCost(15).learnRequirements(bookshelves(12))
                .add(spellGetter.apply(Spells.TOGGLE_SLOW_FALLING)).levelCost(15).learnRequirements(bookshelves(16))
                .leaf()
                .leaf()
                .add(spellGetter.apply(Spells.BLOW_ARROW)).levelCost(10).learnRequirements(bookshelves(16))
                .add(spellGetter.apply(Spells.PRESSURIZE)).levelCost(20).learnRequirements(bookshelves(18))
                .leaf()
                .leaf()
                .finish();
    }
    
    public static SpellTree enderTree(Function<ResourceLocation, Holder<Spell>> spellGetter)
    {
        return SpellTree.builder(Component.translatable(KEY_END))
                .icon(DefaultSpellIcon.make(new ResourceLocation(SpellsAndShields.MOD_ID, "textures/spell/teleport.png")))
                .add(spellGetter.apply(Spells.RANDOM_TELEPORT)).levelCost(20).learnRequirements(bookshelves(28)).hiddenRequirements(advancement("end/root")).hiddenRequirements(config())
                .add(spellGetter.apply(Spells.FORCED_TELEPORT)).levelCost(30).learnRequirements(bookshelves(28))
                .leaf()
                .add(spellGetter.apply(Spells.TELEPORT)).levelCost(30).learnRequirements(bookshelves(28), advancement("end/respawn_dragon"))
                .add(spellGetter.apply(Spells.ENDER_ARMY)).levelCost(50).learnRequirements(bookshelves(30), item(Items.DRAGON_EGG, 1, false))
                .finish();
    }
    
    public static Requirement bookshelves(int bookshelves)
    {
        return new BookshelvesRequirement(RequirementTypes.BOOKSHELVES.get(), bookshelves);
    }
    
    public static Requirement advancement(String advancementRL)
    {
        return new AdvancementRequirement(RequirementTypes.ADVANCEMENT.get(), new ResourceLocation(advancementRL));
    }
    
    public static Requirement item(Item item, int count, boolean consume)
    {
        return new ItemRequirement(RequirementTypes.ITEM.get(), new ItemStack(item, count), consume);
    }
    
    public static Requirement config()
    {
        return new ConfigRequirement(RequirementTypes.CONFIG.get());
    }
}
