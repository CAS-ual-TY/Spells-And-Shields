package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.requirement.AdvancementRequirement;
import de.cas_ual_ty.spells.requirement.BookshelvesRequirement;
import de.cas_ual_ty.spells.requirement.ItemRequirement;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.util.SpellsCodecs;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
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
        return level.registryAccess().registryOrThrow(REGISTRY_KEY);
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
        REGISTRY = event.create(new RegistryBuilder<SpellTree>().setMaxID(1024).dataPackRegistry(SpellsCodecs.SPELL_TREE_CONTENTS).setName(new ResourceLocation(SpellsAndShields.MOD_ID, "spell_trees"))
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
        return SpellTree.builder(Component.translatable(KEY_NETHER), spellGetter.apply(Spells.FIRE_BALL), 15, bookshelves(28))
                .icon(spellGetter.apply(Spells.DUMMY)) //TODO TEMPORARY_FIRE_RESISTANCE
                .add(spellGetter.apply(Spells.LAVA_WALKER), 20, bookshelves(19))
                .add(spellGetter.apply(Spells.DUMMY), 30, bookshelves(30)) //TODO TEMPORARY_FIRE_RESISTANCE
                .leaf()
                .add(spellGetter.apply(Spells.DRAIN_FLAME), 20, bookshelves(20))
                .leaf()
                .leaf()
                .add(spellGetter.apply(Spells.GHAST), 10, bookshelves(20), item(Items.GHAST_TEAR, 1, true))
                .add(spellGetter.apply(Spells.FLAMETHROWER), 20, bookshelves(24))
                .finish();
    }
    
    public static SpellTree waterTree(Function<ResourceLocation, Holder<Spell>> spellGetter)
    {
        return SpellTree.builder(Component.translatable(KEY_OCEAN), spellGetter.apply(Spells.DUMMY), 10) //TODO TEMPORARY_WATER_BREATHING
                .icon(spellGetter.apply(Spells.DUMMY)) //TODO TEMPORARY_DOLPHINS_GRACE
                .add(spellGetter.apply(Spells.DUMMY), 20, bookshelves(20)) //TODO TEMPORARY_REGENERATION
                .add(spellGetter.apply(Spells.GROWTH), 20, bookshelves(20))
                .leaf()
                .add(spellGetter.apply(Spells.AQUA_AFFINITY), 20, bookshelves(20))
                .leaf()
                .leaf()
                .add(spellGetter.apply(Spells.WATER_LEAP), 5, bookshelves(10))
                .add(spellGetter.apply(Spells.DUMMY), 30, bookshelves(30)) //TODO TEMPORARY_DOLPHINS_GRACE
                .leaf()
                .add(spellGetter.apply(Spells.FROST_WALKER), 10, bookshelves(14))
                .leaf()
                .leaf()
                .add(spellGetter.apply(Spells.WATER_WHIP), 10, bookshelves(10))
                .add(spellGetter.apply(Spells.POTION_SHOT), 10, bookshelves(10))
                .leaf()
                .leaf()
                .add(spellGetter.apply(Spells.LIGHTNING_STRIKE), 25, bookshelves(24), advancement("adventure/lightning_rod_with_villager_no_fire"))
                .leaf()
                .finish();
    }
    
    public static SpellTree earthTree(Function<ResourceLocation, Holder<Spell>> spellGetter)
    {
        return SpellTree.builder(Component.translatable(KEY_MINING), spellGetter.apply(Spells.BLAST_SMELT), 5, bookshelves(8))
                .icon(spellGetter.apply(Spells.DUMMY)) //TODO TEMPORARY_HASTE
                .add(spellGetter.apply(Spells.SILENCE_TARGET), 25, bookshelves(26))
                .add(spellGetter.apply(Spells.DUMMY), 25, bookshelves(26)) //TODO TEMPORARY_MAGIC_IMMUNE
                .leaf()
                .leaf()
                .add(spellGetter.apply(Spells.INSTANT_MINE), 15, bookshelves(18))
                .add(spellGetter.apply(Spells.DUMMY), 25, bookshelves(24)) //TODO TEMPORARY_HASTE
                .leaf()
                .leaf()
                .add(spellGetter.apply(Spells.SPIT_METAL), 10, bookshelves(12))
                .finish();
    }
    
    public static SpellTree airTree(Function<ResourceLocation, Holder<Spell>> spellGetter)
    {
        return SpellTree.builder(Component.translatable(KEY_MOVEMENT), spellGetter.apply(Spells.DUMMY), 15, bookshelves(12)) //TODO TEMPORARY_JUMP_BOOST
                .icon(spellGetter.apply(Spells.DUMMY)) //TODO TEMPORARY_JUMP_BOOST
                .add(spellGetter.apply(Spells.LEAP), 10, bookshelves(14))
                .add(spellGetter.apply(Spells.TOGGLE_SPEED), 20, bookshelves(20))
                .leaf()
                .add(spellGetter.apply(Spells.JUMP), 14, bookshelves(14))
                .leaf()
                .add(spellGetter.apply(Spells.MANA_SOLES), 15, bookshelves(12))
                .add(spellGetter.apply(Spells.DUMMY), 15, bookshelves(16)) //TODO TEMPORARY_SLOW_FALLING
                .leaf()
                .leaf()
                .add(spellGetter.apply(Spells.BLOW_ARROW), 10, bookshelves(16))
                .add(spellGetter.apply(Spells.PRESSURIZE), 20, bookshelves(18))
                .leaf()
                .leaf()
                .finish();
    }
    
    public static SpellTree enderTree(Function<ResourceLocation, Holder<Spell>> spellGetter)
    {
        return SpellTree.builder(Component.translatable(KEY_END), spellGetter.apply(Spells.RANDOM_TELEPORT), 20, bookshelves(28))
                .icon(spellGetter.apply(Spells.TELEPORT))
                .requirement(advancement("end/root"))
                .add(spellGetter.apply(Spells.FORCED_TELEPORT), 30, bookshelves(28))
                .leaf()
                .add(spellGetter.apply(Spells.TELEPORT), 30, bookshelves(28), advancement("end/respawn_dragon"))
                .add(spellGetter.apply(Spells.ENDER_ARMY), 50, bookshelves(30), item(Items.DRAGON_EGG, 1, false))
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
}
