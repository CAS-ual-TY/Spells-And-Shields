package de.cas_ual_ty.spells.datagen;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.registers.BuiltInRegisters;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;

public class EnchantmentsGen
{
    private static final TagKey<Item> ENCHANTABLE_SWORD_OR_AXE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(SpellsAndShields.MOD_ID, "enchantable/sword_or_axe"));
    private static final TagKey<Item> ENCHANTABLE_SHIELD = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(SpellsAndShields.MOD_ID, "enchantable/shield"));
    private static final TagKey<Enchantment> MANA_ARMOR_EXCLUSIVE = TagKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(SpellsAndShields.MOD_ID, "exclusive/mana_armor"));
    private static final TagKey<DamageType> IS_MAGIC = TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(SpellsAndShields.MOD_ID, "is_magic"));

    public static void bootstrap(BootstrapContext<Enchantment> context)
    {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);
        HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);
        HolderSet.Named<Enchantment> armorExclusive = enchantments.getOrThrow(EnchantmentTags.ARMOR_EXCLUSIVE);
        HolderSet.Named<Enchantment> manaArmorExclusive = enchantments.getOrThrow(MANA_ARMOR_EXCLUSIVE);

        context.register(
            BuiltInRegisters.MAGIC_PROTECTION_KEY,
            Enchantment.enchantment(
                Enchantment.definition(
                    items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                    5,
                    4,
                    Enchantment.dynamicCost(4, 5),
                    Enchantment.dynamicCost(9, 5),
                    2,
                    EquipmentSlotGroup.ARMOR
                )
            )
            .exclusiveWith(armorExclusive)
            .withEffect(
                EnchantmentEffectComponents.DAMAGE_PROTECTION,
                new AddValue(LevelBasedValue.perLevel(2.0f)),
                DamageSourceCondition.hasDamageSource(
                    DamageSourcePredicate.Builder.damageType()
                        .tag(TagPredicate.is(IS_MAGIC))
                        .tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY))
                )
            )
            .build(BuiltInRegisters.MAGIC_PROTECTION_KEY.location())
        );

        context.register(
            BuiltInRegisters.MANA_BLADE_KEY,
            Enchantment.enchantment(
                Enchantment.definition(
                    items.getOrThrow(ENCHANTABLE_SWORD_OR_AXE),
                    5,
                    5,
                    Enchantment.dynamicCost(5, 8),
                    Enchantment.dynamicCost(25, 8),
                    2,
                    EquipmentSlotGroup.MAINHAND
                )
            )
            .build(BuiltInRegisters.MANA_BLADE_KEY.location())
        );

        context.register(
            BuiltInRegisters.MANA_SHIELD_KEY,
            Enchantment.enchantment(
                Enchantment.definition(
                    items.getOrThrow(ENCHANTABLE_SHIELD),
                    5,
                    3,
                    Enchantment.dynamicCost(5, 8),
                    Enchantment.dynamicCost(55, 8),
                    2,
                    EquipmentSlotGroup.OFFHAND
                )
            )
            .build(BuiltInRegisters.MANA_SHIELD_KEY.location())
        );

        context.register(
            BuiltInRegisters.MAX_MANA_KEY,
            Enchantment.enchantment(
                Enchantment.definition(
                    items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                    5,
                    2,
                    Enchantment.dynamicCost(20, 5),
                    Enchantment.dynamicCost(25, 5),
                    2,
                    EquipmentSlotGroup.ARMOR
                )
            )
            .exclusiveWith(manaArmorExclusive)
            .withEffect(
                EnchantmentEffectComponents.ATTRIBUTES,
                new EnchantmentAttributeEffect(
                    ResourceLocation.fromNamespaceAndPath(SpellsAndShields.MOD_ID, "max_mana"),
                    BuiltInRegisters.MAX_MANA_ATTRIBUTE,
                    LevelBasedValue.perLevel(4.0f, 2.0f),
                    AttributeModifier.Operation.ADD_VALUE
                )
            )
            .build(BuiltInRegisters.MAX_MANA_KEY.location())
        );

        context.register(
            BuiltInRegisters.MANA_REGEN_KEY,
            Enchantment.enchantment(
                Enchantment.definition(
                    items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                    5,
                    2,
                    Enchantment.dynamicCost(20, 5),
                    Enchantment.dynamicCost(25, 5),
                    2,
                    EquipmentSlotGroup.ARMOR
                )
            )
            .exclusiveWith(manaArmorExclusive)
            .withEffect(
                EnchantmentEffectComponents.ATTRIBUTES,
                new EnchantmentAttributeEffect(
                    ResourceLocation.fromNamespaceAndPath(SpellsAndShields.MOD_ID, "mana_regen"),
                    BuiltInRegisters.MANA_REGENERATION_ATTRIBUTE,
                    LevelBasedValue.perLevel(0.2f, 0.1f),
                    AttributeModifier.Operation.ADD_VALUE
                )
            )
            .build(BuiltInRegisters.MANA_REGEN_KEY.location())
        );
    }
}
