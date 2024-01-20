package de.cas_ual_ty.spells.datagen;

import com.google.common.collect.ImmutableList;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.registers.BuiltInRegisters;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.action.ai.SetMobTargetAction;
import de.cas_ual_ty.spells.spell.action.attribute.*;
import de.cas_ual_ty.spells.spell.action.control.*;
import de.cas_ual_ty.spells.spell.action.delayed.AddDelayedSpellAction;
import de.cas_ual_ty.spells.spell.action.delayed.CheckHasDelayedSpellAction;
import de.cas_ual_ty.spells.spell.action.delayed.RemoveDelayedSpellAction;
import de.cas_ual_ty.spells.spell.action.effect.*;
import de.cas_ual_ty.spells.spell.action.fx.ParticleEmitterAction;
import de.cas_ual_ty.spells.spell.action.fx.PlaySoundAction;
import de.cas_ual_ty.spells.spell.action.fx.SpawnParticlesAction;
import de.cas_ual_ty.spells.spell.action.item.*;
import de.cas_ual_ty.spells.spell.action.level.*;
import de.cas_ual_ty.spells.spell.action.mana.*;
import de.cas_ual_ty.spells.spell.action.target.*;
import de.cas_ual_ty.spells.spell.action.variable.PutVarAction;
import de.cas_ual_ty.spells.spell.compiler.Compiler;
import de.cas_ual_ty.spells.spell.icon.*;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.cas_ual_ty.spells.spell.context.BuiltinEvents.*;
import static de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups.*;
import static de.cas_ual_ty.spells.spell.context.BuiltinVariables.*;

public class SpellsGen
{
    public static final ResourceLocation PERMANENT_ICON_RL = new ResourceLocation(SpellsAndShields.MOD_ID, "textures/spell/permanent.png");
    public static final ResourceLocation TEMPORARY_ICON_RL = new ResourceLocation(SpellsAndShields.MOD_ID, "textures/spell/temporary.png");
    public static final ResourceLocation TOGGLE_ICON_RL = new ResourceLocation(SpellsAndShields.MOD_ID, "textures/spell/toggle.png");
    
    public static final String KEY_HAND_ITEM_REQUIREMENT_TITLE = "spell.generic.title.item_requirement.hand";
    public static final String KEY_HAND_ITEM_COST_TITLE = "spell.generic.title.item_cost.hand";
    public static final String KEY_MAINHAND_ITEM_COST_TITLE = "spell.generic.title.item_cost.mainhand";
    public static final String KEY_OFFHAND_ITEM_COST_TITLE = "spell.generic.title.item_cost.offhand";
    public static final String KEY_INVENTORY_ITEM_COST_TITLE = "spell.generic.title.item_cost.inventory";
    public static final String KEY_ITEM_COST = "spell.generic.item_cost";
    public static final String KEY_ITEM_COST_SINGLE = "spell.generic.item_cost.single";
    public static final String KEY_ITEM_COST_TEXT = "spell.generic.item_cost.text";
    
    protected String modId;
    protected final BootstapContext<Spell> context;
    
    public static final CtxVarType<Integer> INT = CtxVarTypes.INT.get();
    public static final CtxVarType<Double> DOUBLE = CtxVarTypes.DOUBLE.get();
    public static final CtxVarType<Vec3> VEC3 = CtxVarTypes.VEC3.get();
    public static final CtxVarType<Boolean> BOOLEAN = CtxVarTypes.BOOLEAN.get();
    public static final CtxVarType<CompoundTag> TAG = CtxVarTypes.TAG.get();
    public static final CtxVarType<String> STRING = CtxVarTypes.STRING.get();
    
    public final DynamicCtxVar<Integer> ZERO = INT.immediate(0);
    public final DynamicCtxVar<Integer> ONE = INT.immediate(1);
    public final DynamicCtxVar<Double> ZERO_D = DOUBLE.immediate(0D);
    public final DynamicCtxVar<Double> ONE_D = DOUBLE.immediate(1D);
    public final DynamicCtxVar<Boolean> TRUE = BOOLEAN.immediate(true);
    public final DynamicCtxVar<Boolean> FALSE = BOOLEAN.immediate(false);
    public final DynamicCtxVar<Vec3> ZERO_VEC3 = VEC3.immediate(Vec3.ZERO);
    public final DynamicCtxVar<CompoundTag> EMPTY_TAG = TAG.immediate(new CompoundTag());
    
    public SpellsGen(String modId, BootstapContext<Spell> context)
    {
        this.modId = modId;
        this.context = context;
        
        addSpells();
    }
    
    public void dummy(ResourceLocation rl)
    {
        dummy(rl, Spells.key(rl), Spells.descKey(rl));
    }
    
    public void dummy(ResourceLocation rl, String key, String descKey)
    {
        dummy(rl, key, descKey, DefaultSpellIcon.make(new ResourceLocation(modId, "textures/spell/" + rl.getPath() + ".png")));
    }
    
    public void dummy(ResourceLocation rl, String key, String descKey, SpellIcon icon)
    {
        addSpell(rl, new Spell(icon, Component.translatable(key), 0F)
                .addTooltip(Component.translatable(descKey))
                .addTooltip(Component.literal("In Development").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC))
        );
    }
    
    public void addSpell(String key, Spell spell)
    {
        addSpell(new ResourceLocation(modId, key), spell);
    }
    
    public void addSpell(ResourceLocation key, Spell spell)
    {
        context.register(ResourceKey.create(Spells.REGISTRY_KEY, key), spell);
    }
    
    public void addPermanentEffectSpell(ResourceLocation rl, String key, String descKey, MobEffect mobEffect, int duration, int amplifier)
    {
        MutableComponent component = mobEffect.getDisplayName().copy();
        if(amplifier > 0)
        {
            component = Component.translatable("potion.withAmplifier", component, Component.translatable("potion.potency." + amplifier));
        }
        ResourceLocation mobEffectRL = ForgeRegistries.MOB_EFFECTS.getKey(mobEffect);
        String uuidCode = " uuid_from_string('permanent' + '%s' + %s) ".formatted(mobEffectRL.getPath(), SPELL_SLOT);
        Spell spell = new Spell(LayeredSpellIcon.make(List.of(DefaultSpellIcon.make(new ResourceLocation(mobEffectRL.getNamespace(), "textures/mob_effect/" + mobEffectRL.getPath() + ".png")), DefaultSpellIcon.make(PERMANENT_ICON_RL))), Component.translatable(key, component), 0F)
                .addAction(CopyTargetsAction.make(ON_EQUIP, "player", OWNER))
                .addAction(CopyTargetsAction.make(ON_UNEQUIP, "player", OWNER))
                .addAction(CopyTargetsAction.make("apply", "player", HOLDER))
                .addAction(PutVarAction.makeString(ON_EQUIP, Compiler.compileString(uuidCode, STRING), "uuid"))
                .addAction(PutVarAction.makeString(ON_UNEQUIP, Compiler.compileString(uuidCode, STRING), "uuid"))
                .addAction(PutVarAction.moveString("apply", DELAY_UUID, "uuid"))
                .addAction(ActivateAction.make(ON_EQUIP, "apply"))
                .addAction(ActivateAction.make(ON_UNEQUIP, "remove"))
                .addAction(RemoveDelayedSpellAction.make("remove", "player", STRING.reference("uuid"), BOOLEAN.immediate(false)))
                .addAction(ActivateAction.make("apply", "renew"))
                .addAction(ApplyMobEffectAction.make("apply", "player", STRING.reference("mob_effect"), INT.reference("duration+1"), INT.reference("amplifier"), BOOLEAN.reference("ambient"), BOOLEAN.reference("visible"), BOOLEAN.reference("show_icon")))
                .addAction(AddDelayedSpellAction.make("renew", "player", "apply", INT.reference("duration"), STRING.reference("uuid"), EMPTY_TAG, eventHookMap()))
                .addParameter(STRING, "mob_effect", ForgeRegistries.MOB_EFFECTS.getKey(mobEffect).toString())
                .addParameter(INT, "duration", duration)
                .addParameter(INT, "amplifier", amplifier)
                .addParameter(BOOLEAN, "ambient", false)
                .addParameter(BOOLEAN, "visible", false)
                .addParameter(BOOLEAN, "show_icon", true)
                .addEventHook(ON_EQUIP)
                .addEventHook(ON_UNEQUIP)
                .addTooltip(Component.translatable(descKey, component.copy().withStyle(mobEffect.getCategory().getTooltipFormatting())));
        
        if(!mobEffect.getAttributeModifiers().isEmpty())
        {
            spell.addTooltip(Component.empty());
            spell.addTooltip(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));
            
            for(Map.Entry<Attribute, AttributeModifier> e : mobEffect.getAttributeModifiers().entrySet())
            {
                Attribute attribute = e.getKey();
                AttributeModifier.Operation op = e.getValue().getOperation();
                double value = e.getValue().getAmount();
                
                double d;
                if(op != AttributeModifier.Operation.MULTIPLY_BASE && op != AttributeModifier.Operation.MULTIPLY_TOTAL)
                {
                    d = value;
                }
                else
                {
                    d = value * 100;
                }
                
                if(value > 0)
                {
                    spell.addTooltip(Component.translatable("attribute.modifier.plus." + op.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d), component.copy()).withStyle(ChatFormatting.BLUE));
                }
                else if(value < 0)
                {
                    d *= -1D;
                    spell.addTooltip(Component.translatable("attribute.modifier.take." + op.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d), component.copy()).withStyle(ChatFormatting.RED));
                }
            }
        }
        
        addSpell(rl, spell);
    }
    
    public void addTemporaryEffectSpell(ResourceLocation rl, String key, String descKey, MobEffect mobEffect, float manaCost, int duration, int amplifier)
    {
        MutableComponent component = mobEffect.getDisplayName().copy();
        if(amplifier > 0)
        {
            component = Component.translatable("potion.withAmplifier", component, Component.translatable("potion.potency." + amplifier));
        }
        ResourceLocation mobEffectRL = ForgeRegistries.MOB_EFFECTS.getKey(mobEffect);
        String uuidCode = " uuid_from_string('toggle' + '%s' + %s) ".formatted(mobEffectRL.getPath(), SPELL_SLOT);
        Spell spell = new Spell(LayeredSpellIcon.make(List.of(DefaultSpellIcon.make(new ResourceLocation(mobEffectRL.getNamespace(), "textures/mob_effect/" + mobEffectRL.getPath() + ".png")), DefaultSpellIcon.make(TEMPORARY_ICON_RL))), Component.translatable(key, component), manaCost)
                .addAction(ManaCheckAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(ApplyMobEffectAction.make(ACTIVE, OWNER, STRING.reference("mob_effect"), INT.reference("duration+1"), INT.reference("amplifier"), BOOLEAN.reference("ambient"), BOOLEAN.reference("visible"), BOOLEAN.reference("show_icon")))
                .addAction(PlaySoundAction.make(ACTIVE, OWNER, SoundEvents.GENERIC_DRINK, ONE_D, ONE_D))
                .addAction(PlaySoundAction.make(ACTIVE, OWNER, SoundEvents.SPLASH_POTION_BREAK, ONE_D, ONE_D))
                .addParameter(STRING, "mob_effect", ForgeRegistries.MOB_EFFECTS.getKey(mobEffect).toString())
                .addParameter(INT, "duration", duration)
                .addParameter(INT, "amplifier", amplifier)
                .addParameter(BOOLEAN, "ambient", false)
                .addParameter(BOOLEAN, "visible", false)
                .addParameter(BOOLEAN, "show_icon", true)
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(descKey, component.copy().withStyle(mobEffect.getCategory().getTooltipFormatting())));
        
        if(!mobEffect.getAttributeModifiers().isEmpty())
        {
            spell.addTooltip(Component.empty());
            spell.addTooltip(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));
            
            for(Map.Entry<Attribute, AttributeModifier> e : mobEffect.getAttributeModifiers().entrySet())
            {
                Attribute attribute = e.getKey();
                AttributeModifier.Operation op = e.getValue().getOperation();
                double value = e.getValue().getAmount();
                
                double d;
                if(op != AttributeModifier.Operation.MULTIPLY_BASE && op != AttributeModifier.Operation.MULTIPLY_TOTAL)
                {
                    d = value;
                }
                else
                {
                    d = value * 100;
                }
                
                if(value > 0)
                {
                    spell.addTooltip(Component.translatable("attribute.modifier.plus." + op.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d), component.copy()).withStyle(ChatFormatting.BLUE));
                }
                else if(value < 0)
                {
                    d *= -1D;
                    spell.addTooltip(Component.translatable("attribute.modifier.take." + op.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d), component.copy()).withStyle(ChatFormatting.RED));
                }
            }
        }
        
        addSpell(rl, spell);
    }
    
    public void addToggleEffectSpell(ResourceLocation rl, String key, String descKey, MobEffect mobEffect, float manaCost, int duration, int amplifier)
    {
        MutableComponent component = mobEffect.getDisplayName().copy();
        if(amplifier > 0)
        {
            component = Component.translatable("potion.withAmplifier", component, Component.translatable("potion.potency." + amplifier));
        }
        ResourceLocation mobEffectRL = ForgeRegistries.MOB_EFFECTS.getKey(mobEffect);
        String uuidCode = " uuid_from_string('toggle' + '%s' + %s) ".formatted(mobEffectRL.getPath(), SPELL_SLOT);
        Spell spell = new Spell(LayeredSpellIcon.make(List.of(DefaultSpellIcon.make(new ResourceLocation(mobEffectRL.getNamespace(), "textures/mob_effect/" + mobEffectRL.getPath() + ".png")), DefaultSpellIcon.make(TOGGLE_ICON_RL))), Component.translatable(key, component), manaCost)
                .addAction(CopyTargetsAction.make(ACTIVE, "player", OWNER))
                .addAction(CopyTargetsAction.make(ON_UNEQUIP, "player", OWNER))
                .addAction(CopyTargetsAction.make("apply", "player", HOLDER))
                .addAction(PutVarAction.makeString(ACTIVE, Compiler.compileString(uuidCode, STRING), "uuid"))
                .addAction(PutVarAction.makeString(ON_UNEQUIP, Compiler.compileString(uuidCode, STRING), "uuid"))
                .addAction(PutVarAction.moveString("apply", DELAY_UUID, "uuid"))
                .addAction(ActivateAction.make(ACTIVE, "apply"))
                .addAction(ActivateAction.make(ACTIVE, "remove"))
                .addAction(ActivateAction.make(ON_UNEQUIP, "remove"))
                .addAction(CheckHasDelayedSpellAction.make("remove", "player", STRING.reference("uuid")))
                .addAction(DeactivateAction.make("remove", "apply"))
                .addAction(RemoveDelayedSpellAction.make("remove", "player", STRING.reference("uuid"), BOOLEAN.immediate(false)))
                .addAction(PlaySoundAction.make("remove", "player", SoundEvents.SPLASH_POTION_BREAK, ONE_D, ONE_D))
                .addAction(ManaCheckAction.make("apply", "player", Compiler.compileString(" (" + MANA_COST + " * duration) / 100 ", DOUBLE)))
                .addAction(ActivateAction.make("apply", "renew"))
                .addAction(ApplyMobEffectAction.make("apply", "player", STRING.reference("mob_effect"), INT.reference("duration+1"), INT.reference("amplifier"), BOOLEAN.reference("ambient"), BOOLEAN.reference("visible"), BOOLEAN.reference("show_icon")))
                .addAction(AddDelayedSpellAction.make("renew", "player", "apply", INT.reference("duration"), STRING.reference("uuid"), EMPTY_TAG, eventHookMap()))
                .addAction(ActivateAction.make("apply", "sound"))
                .addAction(ActivateAction.make("apply", "anti_sound"))
                .addAction(DeactivateAction.make(ACTIVE, "anti_sound"))
                .addAction(DeactivateAction.make("anti_sound", "sound"))
                .addAction(PlaySoundAction.make("sound", "player", SoundEvents.GENERIC_DRINK, ONE_D, ONE_D))
                .addParameter(STRING, "mob_effect", ForgeRegistries.MOB_EFFECTS.getKey(mobEffect).toString())
                .addParameter(INT, "duration", duration)
                .addParameter(INT, "amplifier", amplifier)
                .addParameter(BOOLEAN, "ambient", false)
                .addParameter(BOOLEAN, "visible", false)
                .addParameter(BOOLEAN, "show_icon", true)
                .addEventHook(ACTIVE)
                .addEventHook(ON_UNEQUIP)
                .addTooltip(Component.translatable(descKey, component.copy().withStyle(mobEffect.getCategory().getTooltipFormatting())));
        
        if(!mobEffect.getAttributeModifiers().isEmpty())
        {
            spell.addTooltip(Component.empty());
            spell.addTooltip(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));
            
            for(Map.Entry<Attribute, AttributeModifier> e : mobEffect.getAttributeModifiers().entrySet())
            {
                Attribute attribute = e.getKey();
                AttributeModifier.Operation op = e.getValue().getOperation();
                double value = e.getValue().getAmount();
                
                double d;
                if(op != AttributeModifier.Operation.MULTIPLY_BASE && op != AttributeModifier.Operation.MULTIPLY_TOTAL)
                {
                    d = value;
                }
                else
                {
                    d = value * 100;
                }
                
                if(value > 0)
                {
                    spell.addTooltip(Component.translatable("attribute.modifier.plus." + op.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d), component.copy()).withStyle(ChatFormatting.BLUE));
                }
                else if(value < 0)
                {
                    d *= -1D;
                    spell.addTooltip(Component.translatable("attribute.modifier.take." + op.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d), component.copy()).withStyle(ChatFormatting.RED));
                }
            }
        }
        
        addSpell(rl, spell);
    }
    
    public void addPermanentAttributeSpell(ResourceLocation rl, String key, String descKey, SpellIcon spellIcon, Attribute attribute, AttributeModifier.Operation op, double value)
    {
        MutableComponent component = Component.translatable(attribute.getDescriptionId());
        ResourceLocation attributeRL = ForgeRegistries.ATTRIBUTES.getKey(attribute);
        String opString = SpellsUtil.operationToString(op);
        
        String uuidCode = " uuid_from_string('attribute' + '%s' + %s + %s + %s) ".formatted(attributeRL.getPath(), SPELL_SLOT, "operation", "value");
        
        Spell spell = new Spell(LayeredSpellIcon.make(List.of(spellIcon, DefaultSpellIcon.make(PERMANENT_ICON_RL))), Component.translatable(key, component), 0F)
                .addAction(AddAttributeModifierAction.make(ON_EQUIP, OWNER, SpellsUtil.objectToString(attribute, ForgeRegistries.ATTRIBUTES), Compiler.compileString(uuidCode, STRING), STRING.immediate(attributeRL.getPath()), DOUBLE.reference("value"), STRING.reference("operation")))
                .addAction(RemoveAttributeModifierAction.make(ON_UNEQUIP, OWNER, SpellsUtil.objectToString(attribute, ForgeRegistries.ATTRIBUTES), Compiler.compileString(uuidCode, STRING)))
                .addParameter(DOUBLE, "value", value)
                .addParameter(STRING, "operation", opString)
                .addEventHook(ON_EQUIP)
                .addEventHook(ON_UNEQUIP)
                .addTooltip(Component.translatable(descKey, component.copy().withStyle(ChatFormatting.BLUE)));
        
        spell.addTooltip(Component.empty());
        spell.addTooltip(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));
        
        double d;
        if(op != AttributeModifier.Operation.MULTIPLY_BASE && op != AttributeModifier.Operation.MULTIPLY_TOTAL)
        {
            d = value;
        }
        else
        {
            d = value * 100;
        }
        
        if(value > 0)
        {
            spell.addTooltip(Component.translatable("attribute.modifier.plus." + op.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d), component.copy()).withStyle(ChatFormatting.BLUE));
        }
        else if(value < 0)
        {
            d *= -1D;
            spell.addTooltip(Component.translatable("attribute.modifier.take." + op.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d), component.copy()).withStyle(ChatFormatting.RED));
        }
        
        addSpell(rl, spell);
    }
    
    public void addPermanentWalkerSpell(ResourceLocation rl, String key, String descKey, String icon, FluidType from, BlockState to, boolean tick)
    {
        ResourceLocation fromRL = ForgeRegistries.FLUID_TYPES.get().getKey(from);
        ResourceLocation toRL = ForgeRegistries.BLOCKS.getKey(to.getBlock());
        String uuidCode = " uuid_from_string('permanent_walker' + '%s' + %s) ".formatted(rl.toString(), SPELL_SLOT);
        Spell spell = new Spell(LayeredSpellIcon.make(List.of(DefaultSpellIcon.make(new ResourceLocation(modId, "textures/spell/" + icon + ".png")), DefaultSpellIcon.make(PERMANENT_ICON_RL))), key, 0F)
                .addAction(CopyTargetsAction.make(ON_EQUIP, "player", OWNER))
                .addAction(CopyTargetsAction.make(ON_UNEQUIP, "player", OWNER))
                .addAction(CopyTargetsAction.make("apply", "player", HOLDER))
                .addAction(PutVarAction.makeString(ON_EQUIP, Compiler.compileString(uuidCode, STRING), "uuid"))
                .addAction(PutVarAction.makeString(ON_UNEQUIP, Compiler.compileString(uuidCode, STRING), "uuid"))
                .addAction(PutVarAction.moveString("apply", DELAY_UUID, "uuid"))
                .addAction(ActivateAction.make(ON_EQUIP, "apply"))
                .addAction(ActivateAction.make(ON_UNEQUIP, "remove"))
                .addAction(PutVarAction.makeInt(ON_EQUIP, ZERO, "time"))
                .addAction(PutVarAction.makeInt("apply", Compiler.compileString(" get_nbt_int(" + DELAY_TAG + ", 'time') ", INT), "time"))
                .addAction(RemoveDelayedSpellAction.make("remove", "player", STRING.reference("uuid"), BOOLEAN.immediate(false)))
                .addAction(DeactivateAction.make("remove", "apply"))
                .addAction(PlaySoundAction.make("remove", "player", SoundEvents.SPLASH_POTION_BREAK, ONE_D, ONE_D))
                .addAction(ActivateAction.make("apply", "renew"))
                
                .addAction(OffsetBlockAction.make("apply", "player", "above", ZERO_VEC3))
                .addAction(GetBlockAction.make("apply", "above", "", "", "is_air"))
                .addAction(BooleanActivationAction.make("apply", "apply", BOOLEAN.reference(" is_air "), BOOLEAN.immediate(false), BOOLEAN.immediate(true)))
                
                .addAction(OffsetBlockAction.make("apply", "player", "below", VEC3.immediate(new Vec3(0, -1, 0))))
                .addAction(CubeBlockTargetsAction.make("apply", "below", "blocks", Compiler.compileString(" vec3(-rect_radius, 0, -rect_radius) ", VEC3), Compiler.compileString(" vec3(rect_radius, 0, rect_radius) ", VEC3)))
                
                .addAction(LabelAction.make("apply", "loop"))
                .addAction(ClearTargetsAction.make("apply", "block"))
                .addAction(PickTargetAction.make("apply", "block", "blocks", true, false))
                
                .addAction(GetFluidAction.make("apply", "block", "fluid_id", "", "", "is_source"))
                .addAction(BooleanActivationAction.make("apply", "do_apply", BOOLEAN.reference(" is_source && fluid_id == '" + fromRL.toString() + "' "), BOOLEAN.immediate(true), BOOLEAN.immediate(true)))
                .addAction(SetBlockAction.make("do_apply", "block", STRING.reference("block_to"), TAG.reference("block_state_to")));
        
        if(tick)
        {
            spell.addAction(TickBlockAction.make("apply", "block", Compiler.compileString(" next_int(60) + 60 ", INT)));
        }
        
        spell.addAction(GetTargetGroupSizeAction.make("apply", "blocks", "size"))
                .addAction(BranchAction.make("apply", "loop", Compiler.compileString(" size > 0 ", BOOLEAN)))
                
                .addAction(ActivateAction.make("apply", "sound"))
                .addAction(ActivateAction.make("apply", "anti_sound"))
                .addAction(DeactivateAction.make(ON_EQUIP, "anti_sound"))
                .addAction(DeactivateAction.make("anti_sound", "sound"))
                .addAction(PlaySoundAction.make("sound", "player", SoundEvents.GENERIC_DRINK, ONE_D, ONE_D))
                .addAction(AddDelayedSpellAction.make("renew", "player", "apply", INT.reference("refresh_rate"), STRING.reference("uuid"), Compiler.compileString(" put_nbt_int(new_tag(), 'time', time + refresh_rate) ", TAG), eventHookMap()))
                .addParameter(INT, "refresh_rate", 2)
                .addParameter(STRING, "block_from", fromRL.toString())
                .addParameter(STRING, "block_to", toRL.toString())
                .addParameter(TAG, "block_state_to", SpellsUtil.stateToTag(to))
                .addParameter(INT, "rect_radius", 3)
                .addEventHook(ON_EQUIP)
                .addEventHook(ON_UNEQUIP)
                .addTooltip(Component.translatable(descKey));
        
        addSpell(rl, spell);
    }
    
    public void addTemporaryWalkerSpell(ResourceLocation rl, String key, String descKey, String icon, FluidType from, BlockState to, float manaCost, boolean tick, int duration)
    {
        ResourceLocation fromRL = ForgeRegistries.FLUID_TYPES.get().getKey(from);
        ResourceLocation toRL = ForgeRegistries.BLOCKS.getKey(to.getBlock());
        String uuidCode = " uuid_from_string('temporary_walker' + '%s' + %s) ".formatted(rl.toString(), SPELL_SLOT);
        Spell spell = new Spell(LayeredSpellIcon.make(List.of(DefaultSpellIcon.make(new ResourceLocation(modId, "textures/spell/" + icon + ".png")), DefaultSpellIcon.make(TEMPORARY_ICON_RL))), key, manaCost)
                .addAction(ManaCheckAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(CopyTargetsAction.make(ACTIVE, "player", OWNER))
                .addAction(CopyTargetsAction.make("apply", "player", HOLDER))
                .addAction(PutVarAction.makeString(ACTIVE, Compiler.compileString(uuidCode, STRING), "uuid"))
                .addAction(PutVarAction.moveString("apply", DELAY_UUID, "uuid"))
                .addAction(PutVarAction.makeInt(ACTIVE, ZERO, "time"))
                .addAction(PutVarAction.makeInt("apply", Compiler.compileString(" get_nbt_int(" + DELAY_TAG + ", 'time') ", INT), "time"))
                .addAction(ActivateAction.make(ACTIVE, "apply"))
                .addAction(ActivateAction.make("apply", "remove"))
                .addAction(BooleanActivationAction.make("apply", "renew", Compiler.compileString(" time < duration ", BOOLEAN), BOOLEAN.immediate(true), BOOLEAN.immediate(false)))
                .addAction(DeactivateAction.make("renew", "remove"))
                .addAction(PlaySoundAction.make("remove", "player", SoundEvents.SPLASH_POTION_BREAK, ONE_D, ONE_D))
                
                .addAction(OffsetBlockAction.make("apply", "player", "above", ZERO_VEC3))
                .addAction(GetBlockAction.make("apply", "above", "", "", "is_air"))
                .addAction(BooleanActivationAction.make("apply", "apply", BOOLEAN.reference(" is_air "), BOOLEAN.immediate(false), BOOLEAN.immediate(true)))
                
                .addAction(OffsetBlockAction.make("apply", "player", "below", VEC3.immediate(new Vec3(0, -1, 0))))
                .addAction(CubeBlockTargetsAction.make("apply", "below", "blocks", Compiler.compileString(" vec3(-rect_radius, 0, -rect_radius) ", VEC3), Compiler.compileString(" vec3(rect_radius, 0, rect_radius) ", VEC3)))
                
                .addAction(LabelAction.make("apply", "loop"))
                .addAction(ClearTargetsAction.make("apply", "block"))
                .addAction(PickTargetAction.make("apply", "block", "blocks", true, false))
                
                .addAction(GetFluidAction.make("apply", "block", "fluid_id", "", "", "is_source"))
                .addAction(BooleanActivationAction.make("apply", "do_apply", BOOLEAN.reference(" is_source && fluid_id == '" + fromRL.toString() + "' "), BOOLEAN.immediate(true), BOOLEAN.immediate(true)))
                .addAction(SetBlockAction.make("do_apply", "block", STRING.reference("block_to"), TAG.reference("block_state_to")));
        
        if(tick)
        {
            spell.addAction(TickBlockAction.make("apply", "block", Compiler.compileString(" next_int(60) + 60 ", INT)));
        }
        
        spell.addAction(GetTargetGroupSizeAction.make("apply", "blocks", "size"))
                .addAction(BranchAction.make("apply", "loop", Compiler.compileString(" size > 0 ", BOOLEAN)))
                
                .addAction(ActivateAction.make("apply", "sound"))
                .addAction(ActivateAction.make("apply", "anti_sound"))
                .addAction(DeactivateAction.make(ACTIVE, "anti_sound"))
                .addAction(DeactivateAction.make("anti_sound", "sound"))
                .addAction(PlaySoundAction.make("sound", "player", SoundEvents.GENERIC_DRINK, ONE_D, ONE_D))
                .addAction(AddDelayedSpellAction.make("renew", "player", "apply", INT.reference("refresh_rate"), STRING.reference("uuid"), Compiler.compileString(" put_nbt_int(new_tag(), 'time', time + refresh_rate) ", TAG), eventHookMap()))
                .addParameter(INT, "duration", duration)
                .addParameter(INT, "refresh_rate", 2)
                .addParameter(STRING, "block_from", fromRL.toString())
                .addParameter(STRING, "block_to", toRL.toString())
                .addParameter(TAG, "block_state_to", SpellsUtil.stateToTag(to))
                .addParameter(INT, "rect_radius", 3)
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(descKey));
        
        addSpell(rl, spell);
    }
    
    public void addToggleWalkerSpell(ResourceLocation rl, String key, String descKey, String icon, FluidType from, BlockState to, float manaCost, boolean tick)
    {
        ResourceLocation fromRL = ForgeRegistries.FLUID_TYPES.get().getKey(from);
        ResourceLocation toRL = ForgeRegistries.BLOCKS.getKey(to.getBlock());
        String uuidCode = " uuid_from_string('toggle_walker' + '%s' + %s) ".formatted(rl.toString(), SPELL_SLOT);
        Spell spell = new Spell(LayeredSpellIcon.make(List.of(DefaultSpellIcon.make(new ResourceLocation(modId, "textures/spell/" + icon + ".png")), DefaultSpellIcon.make(TOGGLE_ICON_RL))), key, manaCost)
                .addAction(CopyTargetsAction.make(ACTIVE, "player", OWNER))
                .addAction(CopyTargetsAction.make(ON_UNEQUIP, "player", OWNER))
                .addAction(CopyTargetsAction.make("apply", "player", HOLDER))
                .addAction(PutVarAction.makeString(ACTIVE, Compiler.compileString(uuidCode, STRING), "uuid"))
                .addAction(PutVarAction.makeString(ON_UNEQUIP, Compiler.compileString(uuidCode, STRING), "uuid"))
                .addAction(PutVarAction.moveString("apply", DELAY_UUID, "uuid"))
                .addAction(ActivateAction.make(ACTIVE, "apply"))
                .addAction(ActivateAction.make(ACTIVE, "remove"))
                .addAction(CheckHasDelayedSpellAction.make("remove", "player", STRING.reference("uuid")))
                .addAction(ActivateAction.make(ON_UNEQUIP, "remove"))
                .addAction(RemoveDelayedSpellAction.make("remove", "player", STRING.reference("uuid"), BOOLEAN.immediate(false)))
                .addAction(DeactivateAction.make("remove", "apply"))
                .addAction(PlaySoundAction.make("remove", "player", SoundEvents.SPLASH_POTION_BREAK, ONE_D, ONE_D))
                .addAction(ManaCheckAction.make("apply", "player", Compiler.compileString(" (" + MANA_COST + " * refresh_rate) / 100 ", DOUBLE)))
                .addAction(ActivateAction.make("apply", "renew"))
                
                .addAction(OffsetBlockAction.make("apply", "player", "above", ZERO_VEC3))
                .addAction(GetBlockAction.make("apply", "above", "", "", "is_air"))
                .addAction(BooleanActivationAction.make("apply", "apply", BOOLEAN.reference(" is_air "), BOOLEAN.immediate(false), BOOLEAN.immediate(true)))
                
                .addAction(OffsetBlockAction.make("apply", "player", "below", VEC3.immediate(new Vec3(0, -1, 0))))
                .addAction(CubeBlockTargetsAction.make("apply", "below", "blocks", Compiler.compileString(" vec3(-rect_radius, 0, -rect_radius) ", VEC3), Compiler.compileString(" vec3(rect_radius, 0, rect_radius) ", VEC3)))
                
                .addAction(LabelAction.make("apply", "loop"))
                .addAction(ClearTargetsAction.make("apply", "block"))
                .addAction(PickTargetAction.make("apply", "block", "blocks", true, false))
                
                .addAction(GetFluidAction.make("apply", "block", "fluid_id", "", "", "is_source"))
                .addAction(BooleanActivationAction.make("apply", "do_apply", BOOLEAN.reference(" is_source && fluid_id == '" + fromRL.toString() + "' "), BOOLEAN.immediate(true), BOOLEAN.immediate(true)))
                .addAction(SetBlockAction.make("do_apply", "block", STRING.reference("block_to"), TAG.reference("block_state_to")));
        
        if(tick)
        {
            spell.addAction(TickBlockAction.make("apply", "block", Compiler.compileString(" next_int(60) + 60 ", INT)));
        }
        
        spell.addAction(GetTargetGroupSizeAction.make("apply", "blocks", "size"))
                .addAction(BranchAction.make("apply", "loop", Compiler.compileString(" size > 0 ", BOOLEAN)))
                .addAction(ActivateAction.make("apply", "sound"))
                .addAction(ActivateAction.make("apply", "anti_sound"))
                .addAction(DeactivateAction.make(ACTIVE, "anti_sound"))
                .addAction(DeactivateAction.make("anti_sound", "sound"))
                .addAction(PlaySoundAction.make("sound", "player", SoundEvents.GENERIC_DRINK, ONE_D, ONE_D))
                .addAction(AddDelayedSpellAction.make("renew", "player", "apply", INT.reference("refresh_rate"), STRING.reference("uuid"), EMPTY_TAG, eventHookMap()))
                .addParameter(INT, "refresh_rate", 2)
                .addParameter(STRING, "block_from", fromRL.toString())
                .addParameter(STRING, "block_to", toRL.toString())
                .addParameter(TAG, "block_state_to", SpellsUtil.stateToTag(to))
                .addParameter(INT, "rect_radius", 3)
                .addEventHook(ACTIVE)
                .addEventHook(ON_UNEQUIP)
                .addTooltip(Component.translatable(descKey));
        
        addSpell(rl, spell);
    }
    
    protected void addSpells()
    {
        dummy(Spells.DUMMY);
        
        addSpell(Spells.LEAP, new Spell(modId, "leap", Spells.KEY_LEAP, 5F)
                .addParameter(DOUBLE, "speed", 2.5)
                .addAction(SimpleManaCheckAction.make(ACTIVE))
                .addAction(ResetFallDistanceAction.make(ACTIVE, OWNER))
                .addAction(GetEntityPositionDirectionMotionAction.make(ACTIVE, OWNER, "", "look", ""))
                .addAction(PutVarAction.makeVec3(ACTIVE, Compiler.compileString(" (normalize(look + vec3(0, -get_y(look), 0))) * speed ", VEC3), "direction"))
                .addAction(SetMotionAction.make(ACTIVE, OWNER, Compiler.compileString(" vec3(get_x(direction), max(0.5, get_y(look) + 0.5), get_z(direction)) ", VEC3)))
                .addAction(SpawnParticlesAction.make(ACTIVE, OWNER, ParticleTypes.POOF, INT.immediate(4), DOUBLE.immediate(0.1)))
                .addAction(PlaySoundAction.make(ACTIVE, OWNER, SoundEvents.ENDER_DRAGON_FLAP, ONE_D, ONE_D))
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_LEAP_DESC))
        );
        
        CompoundTag childTag = new CompoundTag();
        childTag.putInt("Age", -24000);
        CompoundTag animalsTag = new CompoundTag();
        animalsTag.putString(ForgeRegistries.ITEMS.getKey(Items.BEEF).toString(), ForgeRegistries.ENTITY_TYPES.getKey(EntityType.COW).toString());
        animalsTag.putString(ForgeRegistries.ITEMS.getKey(Items.CHICKEN).toString(), ForgeRegistries.ENTITY_TYPES.getKey(EntityType.CHICKEN).toString());
        animalsTag.putString(ForgeRegistries.ITEMS.getKey(Items.PORKCHOP).toString(), ForgeRegistries.ENTITY_TYPES.getKey(EntityType.PIG).toString());
        animalsTag.putString(ForgeRegistries.ITEMS.getKey(Items.MUTTON).toString(), ForgeRegistries.ENTITY_TYPES.getKey(EntityType.SHEEP).toString());
        CompoundTag amountsTag = new CompoundTag();
        amountsTag.putInt(ForgeRegistries.ITEMS.getKey(Items.BEEF).toString(), 8);
        amountsTag.putInt(ForgeRegistries.ITEMS.getKey(Items.CHICKEN).toString(), 8);
        amountsTag.putInt(ForgeRegistries.ITEMS.getKey(Items.PORKCHOP).toString(), 8);
        amountsTag.putInt(ForgeRegistries.ITEMS.getKey(Items.MUTTON).toString(), 8);
        addSpell(Spells.SUMMON_ANIMAL, new Spell(modId, "summon_animal", Spells.KEY_SUMMON_ANIMAL, 4F)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(MainhandItemTargetAction.make(ACTIVE, OWNER, "item"))
                .addAction(GetItemAttributesAction.make(ACTIVE, "item", "item_id", "amount", "", ""))
                .addAction(BooleanActivationAction.make(ACTIVE, "spawn", Compiler.compileString(" nbt_contains(animals, item_id) && nbt_contains(amounts, item_id) && amount >= (!item_costs() ? 1 : get_nbt_int(amounts, item_id)) ", BOOLEAN), TRUE, FALSE))
                .addAction(ActivateAction.make(ACTIVE, "offhand"))
                .addAction(DeactivateAction.make("spawn", "offhand"))
                .addAction(ClearTargetsAction.make("offhand", "item"))
                .addAction(OffhandItemTargetAction.make("offhand", OWNER, "item"))
                .addAction(BooleanActivationAction.make("offhand", "spawn", Compiler.compileString(" nbt_contains(animals, item_id) && nbt_contains(amounts, item_id) && amount >= get_nbt_int(amounts, item_id) ", BOOLEAN), TRUE, FALSE))
                .addAction(SpawnEntityAction.make("spawn", "baby", Compiler.compileString(" get_nbt_string(animals, item_id) ", STRING), OWNER, Compiler.compileString(" -direction ", VEC3), ZERO_VEC3, TAG.immediate(childTag)))
                .addAction(BurnManaAction.make("spawn", OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make("spawn", "consume", Compiler.compileString(" item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(ConsumeItemAction.make("consume", "item", Compiler.compileString(" get_nbt_int(amounts, item_id) ", INT)))
                .addAction(SpawnParticlesAction.make("spawn", OWNER, ParticleTypes.EXPLOSION, INT.immediate(3), DOUBLE.immediate(0.4)))
                .addAction(PlaySoundAction.make("spawn", OWNER, SoundEvents.CHICKEN_EGG, ONE_D, ONE_D))
                .addParameter(TAG, "animals", animalsTag)
                .addParameter(TAG, "amounts", amountsTag)
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_SUMMON_ANIMAL_DESC))
                .addTooltip(Component.empty())
                .addTooltip(itemCostTitle(KEY_HAND_ITEM_COST_TITLE))
                .addTooltip(itemCostComponent(new ItemStack(Items.BEEF, 8)))
                .addTooltip(itemCostComponent(new ItemStack(Items.CHICKEN, 8)))
                .addTooltip(itemCostComponent(new ItemStack(Items.PORKCHOP, 8)))
                .addTooltip(itemCostComponent(new ItemStack(Items.MUTTON, 8)))
        );
        
        addSpell(Spells.FIRE_BALL, new Spell(modId, "fire_ball", Spells.KEY_FIRE_BALL, 5F)
                .addParameter(DOUBLE, "speed", 2.5)
                .addParameter(INT, "fire_seconds", 2)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make(ACTIVE, "consume", Compiler.compileString(" !item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(PlayerHasItemsAction.make(ACTIVE, OWNER, SpellsUtil.objectToString(Items.BLAZE_POWDER, ForgeRegistries.ITEMS), ONE, null, TRUE, TRUE))
                .addAction(ActivateAction.make(ACTIVE, "consume"))
                .addAction(ActivateAction.make("consume", "shoot"))
                .addAction(BurnManaAction.make("shoot", OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make("consume", "consume", Compiler.compileString(" item_costs() ", BOOLEAN), FALSE, TRUE))
                .addAction(ConsumePlayerItemsAction.make("consume", OWNER, SpellsUtil.objectToString(Items.BLAZE_POWDER, ForgeRegistries.ITEMS), ONE, null, TRUE))
                .addAction(ShootAction.make("shoot", OWNER, DOUBLE.immediate(3D), ZERO_D, INT.immediate(200), "on_block_hit", "on_entity_hit", "on_timeout", "projectile"))
                .addAction(PlaySoundAction.make("shoot", OWNER, SoundEvents.BLAZE_SHOOT, ONE_D, ONE_D))
                .addAction(ParticleEmitterAction.make("shoot", "projectile", INT.immediate(200), INT.immediate(2), INT.immediate(3), DOUBLE.immediate(0.2D), TRUE, ZERO_VEC3, ParticleTypes.LARGE_SMOKE))
                .addAction(ParticleEmitterAction.make("shoot", "projectile", INT.immediate(200), INT.immediate(4), ONE, ZERO_D, TRUE, ZERO_VEC3, ParticleTypes.LAVA))
                .addAction(ParticleEmitterAction.make("shoot", "projectile", INT.immediate(200), INT.immediate(4), INT.immediate(2), DOUBLE.immediate(0.1D), TRUE, ZERO_VEC3, ParticleTypes.SMOKE))
                .addAction(ParticleEmitterAction.make("shoot", "projectile", INT.immediate(200), INT.immediate(4), INT.immediate(2), DOUBLE.immediate(0.1D), TRUE, ZERO_VEC3, ParticleTypes.FLAME))
                .addAction(BooleanActivationAction.make("on_entity_hit", "no_pvp", Compiler.compileString(" !pvp() ", BOOLEAN), TRUE, FALSE))
                .addAction(MovePlayerTargetsAction.make("no_pvp", ENTITY_HIT, ""))
                .addAction(SourcedDamageAction.make("on_entity_hit", ENTITY_HIT, DOUBLE.immediate(2D), PROJECTILE))
                .addAction(SetOnFireAction.make("on_entity_hit", ENTITY_HIT, INT.reference("fire_seconds")))
                .addAction(ActivateAction.make("on_entity_hit", "fx"))
                .addAction(ActivateAction.make("on_block_hit", "fx"))
                .addAction(ActivateAction.make("on_timeout", "fx"))
                .addAction(PlaySoundAction.make("fx", HIT_POSITION, SoundEvents.BLAZE_SHOOT, ONE_D, ONE_D))
                .addAction(SpawnParticlesAction.make("fx", HIT_POSITION, ParticleTypes.LARGE_SMOKE, INT.immediate(3), DOUBLE.immediate(0.2)))
                .addAction(SpawnParticlesAction.make("fx", HIT_POSITION, ParticleTypes.LAVA, ONE, DOUBLE.immediate(0.2)))
                .addAction(SpawnParticlesAction.make("fx", HIT_POSITION, ParticleTypes.SMOKE, INT.immediate(2), DOUBLE.immediate(0.1)))
                .addAction(SpawnParticlesAction.make("fx", HIT_POSITION, ParticleTypes.FLAME, INT.immediate(2), DOUBLE.immediate(0.1)))
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_FIRE_BALL_DESC))
                .addTooltip(Component.empty())
                .addTooltip(itemCostTitle(KEY_HAND_ITEM_COST_TITLE))
                .addTooltip(itemCostComponent(new ItemStack(Items.BLAZE_POWDER)))
        );
        
        CompoundTag blastRecipes = blastFurnaceRecipes();
        addSpell(Spells.BLAST_SMELT, new Spell(modId, "blast_smelt", Spells.KEY_BLAST_SMELT, 4F)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(MainhandItemTargetAction.make(ACTIVE, OWNER, "item"))
                .addAction(GetItemAttributesAction.make(ACTIVE, "item", "item_id", "amount", "", ""))
                .addAction(BooleanActivationAction.make(ACTIVE, "smelt", Compiler.compileString(" nbt_contains(recipes, item_id) ", BOOLEAN), TRUE, FALSE))
                .addAction(ActivateAction.make(ACTIVE, "offhand"))
                .addAction(DeactivateAction.make("smelt", "offhand"))
                .addAction(ClearTargetsAction.make("offhand", "item"))
                .addAction(OffhandItemTargetAction.make("offhand", OWNER, "item"))
                .addAction(BooleanActivationAction.make("offhand", "smelt", Compiler.compileString(" nbt_contains(recipes, item_id) ", BOOLEAN), TRUE, FALSE))
                .addAction(BurnManaAction.make("smelt", OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make("smelt", "consume", Compiler.compileString(" item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(ConsumeItemAction.make("consume", "item", ONE))
                .addAction(GiveItemAction.make("smelt", OWNER, ONE, ZERO, null, Compiler.compileString(" get_nbt_string(recipes, item_id) ", STRING)))
                .addParameter(TAG, "recipes", blastRecipes)
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_BLAST_SMELT_DESC))
                .addTooltip(Component.empty())
                .addTooltip(itemCostTitle(KEY_HAND_ITEM_COST_TITLE))
                .addTooltip(textItemCostComponent(Component.translatable(Spells.KEY_BLAST_SMELT_DESC_COST), 1))
        );
        
        addSpell(Spells.TRANSFER_MANA, new Spell(modId, "transfer_mana", Spells.KEY_TRANSFER_MANA, 4F)
                .addParameter(DOUBLE, "speed", 2.5)
                .addParameter(DOUBLE, "range", 25D)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(LookAtTargetAction.make(ACTIVE, OWNER, DOUBLE.reference("range"), 0.5F, ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, "looked_at_block", "looked_at_entity", "looked_at_nothing"))
                .addAction(BurnManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(HomeAction.make("looked_at_entity", OWNER, ENTITY_HIT, DOUBLE.immediate(3D), INT.immediate(200), "on_block_hit", "on_entity_hit", "on_timeout", ""))
                .addAction(PlaySoundAction.make("looked_at_entity", OWNER, SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, ONE_D, ONE_D))
                .addAction(ReplenishManaAction.make("on_entity_hit", ENTITY_HIT, DOUBLE.reference(MANA_COST)))
                .addAction(ActivateAction.make("on_entity_hit", "fx"))
                .addAction(ActivateAction.make("on_block_hit", "fx"))
                .addAction(ActivateAction.make("on_timeout", "fx"))
                .addAction(PlaySoundAction.make("fx", HIT_POSITION, SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, ONE_D, ONE_D))
                .addAction(SpawnParticlesAction.make("fx", HIT_POSITION, ParticleTypes.BUBBLE, INT.immediate(3), DOUBLE.immediate(0.2)))
                .addAction(SpawnParticlesAction.make("fx", HIT_POSITION, ParticleTypes.POOF, INT.immediate(2), DOUBLE.immediate(0.2)))
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_TRANSFER_MANA_DESC))
        );
        
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("crit", true);
        tag.putInt("pickup", 1);
        addSpell(Spells.BLOW_ARROW, new Spell(modId, "blow_arrow", Spells.KEY_BLOW_ARROW, 5F)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(PutVarAction.makeCompoundTag(ACTIVE, tag, "tag"))
                .addAction(GetEntityUUIDAction.make(ACTIVE, OWNER, "uuid"))
                .addAction(PutVarAction.makeCompoundTag(ACTIVE, Compiler.compileString(" put_nbt_uuid(tag, 'Owner', uuid) ", TAG), "tag"))
                .addAction(GetEntityPositionDirectionMotionAction.make(ACTIVE, OWNER, "", "direction", ""))
                .addAction(GetEntityEyePositionAction.make(ACTIVE, OWNER, "position"))
                .addAction(MainhandItemTargetAction.make(ACTIVE, OWNER, "item"))
                .addAction(ActivateAction.make(ACTIVE, "shoot"))
                .addAction(ActivateAction.make(ACTIVE, "potion"))
                .addAction(ActivateAction.make(ACTIVE, "spectral"))
                .addAction(ItemEqualsAction.make("shoot", "item", new ItemStack(Items.ARROW), TRUE, ONE, INT.immediate(-1)))
                .addAction(ItemEqualsAction.make("potion", "item", new ItemStack(Items.TIPPED_ARROW), TRUE, ONE, INT.immediate(-1)))
                .addAction(GetItemTagAction.make("potion", "item", "potion_tag"))
                .addAction(PutVarAction.makeCompoundTag("potion", Compiler.compileString(" put_nbt_string(tag, 'Potion', get_nbt_string(potion_tag, 'Potion')) ", TAG), "tag"))
                .addAction(ActivateAction.make("potion", "shoot"))
                .addAction(BurnManaAction.make("shoot", OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(SpawnEntityAction.make("shoot", "arrow", SpellsUtil.objectToString(EntityType.ARROW, ForgeRegistries.ENTITY_TYPES), "position", VEC3.reference("direction"), Compiler.compileString(" 3 * direction ", VEC3), TAG.reference("tag")))
                .addAction(PlaySoundAction.make("shoot", OWNER, SoundEvents.ARROW_SHOOT, ONE_D, ONE_D))
                .addAction(ItemEqualsAction.make("spectral", "item", new ItemStack(Items.SPECTRAL_ARROW), TRUE, ONE, INT.immediate(-1)))
                .addAction(HasManaAction.make("spectral", OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(SpawnEntityAction.make("spectral", "arrow", SpellsUtil.objectToString(EntityType.SPECTRAL_ARROW, ForgeRegistries.ENTITY_TYPES), "position", VEC3.reference("direction"), Compiler.compileString(" 3 * direction ", VEC3), TAG.reference("tag")))
                .addAction(PlaySoundAction.make("spectral", OWNER, SoundEvents.ARROW_SHOOT, ONE_D, ONE_D))
                .addAction(ActivateAction.make("shoot", "consume"))
                .addAction(ActivateAction.make("spectral", "consume"))
                .addAction(BooleanActivationAction.make("consume", "consume", Compiler.compileString(" item_costs() ", BOOLEAN), FALSE, TRUE))
                .addAction(ConsumeItemAction.make("consume", "item", ONE))
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_BLOW_ARROW_DESC))
                .addTooltip(Component.empty())
                .addTooltip(itemCostTitle(KEY_HAND_ITEM_COST_TITLE))
                .addTooltip(itemCostComponent(new ItemStack(Items.ARROW)))
                .addTooltip(itemCostComponent(Items.TIPPED_ARROW))
                .addTooltip(itemCostComponent(new ItemStack(Items.SPECTRAL_ARROW)))
        );
        
        addPermanentAttributeSpell(Spells.HEALTH_BOOST, Spells.KEY_HEALTH_BOOST, Spells.KEY_HEALTH_BOOST_DESC, DefaultSpellIcon.make(new ResourceLocation("textures/mob_effect/" + ForgeRegistries.MOB_EFFECTS.getKey(MobEffects.HEALTH_BOOST).getPath() + ".png")), Attributes.MAX_HEALTH, AttributeModifier.Operation.ADDITION, 4D);
        
        addPermanentAttributeSpell(Spells.MANA_BOOST, Spells.KEY_MANA_BOOST, Spells.KEY_MANA_BOOST_DESC, DefaultSpellIcon.make(new ResourceLocation(SpellsAndShields.MOD_ID, "textures/mob_effect/" + BuiltInRegisters.MANA_BOOST_EFFECT.getId().getPath() + ".png")), BuiltInRegisters.MAX_MANA_ATTRIBUTE.get(), AttributeModifier.Operation.ADDITION, 4D);
        
        addSpell(Spells.WATER_LEAP, new Spell(modId, "water_leap", Spells.KEY_WATER_LEAP, 5F)
                .addParameter(DOUBLE, "speed", 2.5)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(GetEntityEyePositionAction.make(ACTIVE, OWNER, "eye_pos"))
                .addAction(GetBlockAction.make(ACTIVE, OWNER, "feet_block", "", ""))
                .addAction(GetBlockAction.make(ACTIVE, "eye_pos", "eye_block", "", ""))
                .addAction(BooleanActivationAction.make(ACTIVE, ACTIVE, Compiler.compileString(" feet_block == '" + ForgeRegistries.BLOCKS.getKey(Blocks.WATER).toString() + "' && eye_block == '" + ForgeRegistries.BLOCKS.getKey(Blocks.WATER).toString() + "' ", BOOLEAN), FALSE, TRUE))
                .addAction(BurnManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(ResetFallDistanceAction.make(ACTIVE, OWNER))
                .addAction(GetEntityPositionDirectionMotionAction.make(ACTIVE, OWNER, "", "look", ""))
                .addAction(PutVarAction.makeVec3(ACTIVE, Compiler.compileString(" (normalize(look + vec3(0, -get_y(look), 0))) * speed ", VEC3), "direction"))
                .addAction(SetMotionAction.make(ACTIVE, OWNER, Compiler.compileString(" vec3(get_x(direction), max(0.5, get_y(look) + 0.5), get_z(direction)) ", VEC3)))
                .addAction(SpawnParticlesAction.make(ACTIVE, OWNER, ParticleTypes.POOF, INT.immediate(4), DOUBLE.immediate(0.1)))
                .addAction(PlaySoundAction.make(ACTIVE, OWNER, SoundEvents.ENDER_DRAGON_FLAP, ONE_D, ONE_D))
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_WATER_LEAP_DESC))
        );
        
        ResourceLocation resistanceRL = ForgeRegistries.MOB_EFFECTS.getKey(MobEffects.DAMAGE_RESISTANCE);
        ResourceLocation waterBreathingRL = ForgeRegistries.MOB_EFFECTS.getKey(MobEffects.WATER_BREATHING);
        addSpell(Spells.PERMANENT_AQUA_RESISTANCE, new Spell(LayeredSpellIcon.make(List.of(DefaultSpellIcon.make(new ResourceLocation(resistanceRL.getNamespace(), "textures/mob_effect/" + resistanceRL.getPath() + ".png")), DefaultSpellIcon.make(new ResourceLocation(waterBreathingRL.getNamespace(), "textures/mob_effect/" + waterBreathingRL.getPath() + ".png")))), Spells.KEY_PERMANENT_AQUA_RESISTANCE, 0F)
                .addAction(ConditionalDeactivationAction.make(LIVING_HURT_VICTIM, Compiler.compileString(" damage_type == 'mob' ", BOOLEAN)))
                .addAction(GetEntityEyePositionAction.make(LIVING_HURT_VICTIM, OWNER, "eye_pos"))
                .addAction(GetFluidAction.make(LIVING_HURT_VICTIM, "eye_pos", "fluid_type", "", "", ""))
                .addAction(ConditionalDeactivationAction.make(LIVING_HURT_VICTIM, Compiler.compileString(" fluid_type == '" + ForgeRegistries.FLUID_TYPES.get().getKey(Fluids.WATER.getFluidType()) + "' ", BOOLEAN)))
                .addAction(PutVarAction.makeDouble(LIVING_HURT_VICTIM, Compiler.compileString(" damage_amount * factor ", DOUBLE), "damage_amount"))
                .addParameter(DOUBLE, "factor", 0.75D)
                .addEventHook(LIVING_HURT_VICTIM)
                .addTooltip(Component.translatable(Spells.KEY_PERMANENT_AQUA_RESISTANCE_DESC))
        );
        
        addSpell(Spells.WATER_WHIP, new Spell(modId, "water_whip", Spells.KEY_WATER_WHIP, 5F)
                .addParameter(DOUBLE, "damage", 10.0)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(MainhandItemTargetAction.make(ACTIVE, OWNER, "item"))
                .addAction(ActivateAction.make(ACTIVE, "shoot"))
                .addAction(ItemEqualsAction.make("shoot", "item", new ItemStack(Items.WATER_BUCKET), TRUE, ONE, INT.immediate(-1)))
                .addAction(ActivateAction.make(ACTIVE, "offhand"))
                .addAction(DeactivateAction.make("shoot", "offhand"))
                .addAction(ClearTargetsAction.make("offhand", "item"))
                .addAction(OffhandItemTargetAction.make("offhand", OWNER, "item"))
                .addAction(ActivateAction.make("offhand", "shoot"))
                .addAction(ItemEqualsAction.make("shoot", "item", new ItemStack(Items.WATER_BUCKET), TRUE, ONE, INT.immediate(-1)))
                .addAction(GetItemAttributesAction.make("shoot", "item", "item", "amount", "damage", "item_tag"))
                .addAction(BooleanActivationAction.make("shoot", "consume", Compiler.compileString(" item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(OverrideItemAction.make("consume", "item", INT.reference("amount"), INT.reference("damage"), TAG.reference("item_tag"), SpellsUtil.objectToString(Items.BUCKET, ForgeRegistries.ITEMS)))
                .addAction(GetEntityUUIDAction.make("shoot", OWNER, "owner_uuid_return"))
                .addAction(PutVarAction.makeCompoundTag("shoot", Compiler.compileString(" put_nbt_uuid(new_tag(), 'owner_uuid_return', owner_uuid_return) ", TAG), "tag"))
                .addAction(BurnManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(ShootAction.make("shoot", OWNER, ONE_D, ZERO_D, INT.immediate(100), "on_block_hit", "on_entity_hit", "on_timeout", "projectile"))
                .addAction(ParticleEmitterAction.make("shoot", "projectile", INT.immediate(100), ONE, INT.immediate(5), DOUBLE.immediate(0.5), TRUE, ZERO_VEC3, ParticleTypes.FALLING_WATER))
                .addAction(ApplyEntityExtraTagAction.make("shoot", "projectile", TAG.reference("tag")))
                .addAction(PlaySoundAction.make("shoot", OWNER, SoundEvents.BUCKET_EMPTY, ONE_D, ONE_D))
                .addAction(BooleanActivationAction.make("on_entity_hit", "no_pvp", Compiler.compileString(" !pvp() ", BOOLEAN), TRUE, FALSE))
                .addAction(MovePlayerTargetsAction.make("no_pvp", ENTITY_HIT, ""))
                .addAction(SourcedDamageAction.make("on_entity_hit", ENTITY_HIT, DOUBLE.immediate(10D), PROJECTILE))
                .addAction(CopyTargetsAction.make("on_entity_hit", "position", HIT_POSITION))
                .addAction(ActivateAction.make("on_entity_hit", "return"))
                .addAction(CopyTargetsAction.make("on_block_hit", "position", HIT_POSITION))
                .addAction(ActivateAction.make("on_block_hit", "return"))
                .addAction(CopyTargetsAction.make("on_timeout", "position", PROJECTILE))
                .addAction(ActivateAction.make("on_timeout", "return"))
                .addAction(GetEntityExtraTagAction.make("return", PROJECTILE, "tag"))
                .addAction(EntityUUIDTargetAction.make("return", "return_target", Compiler.compileString(" get_nbt_uuid(tag, 'owner_uuid_return') ", STRING)))
                .addAction(HomeAction.make("return", "position", "return_target", ONE_D, INT.immediate(100), "dummy_block_hit", "on_entity_hit_return", "dummy_timeout", "projectile"))
                .addAction(ParticleEmitterAction.make("return", "projectile", INT.immediate(100), ONE, INT.immediate(5), DOUBLE.immediate(0.1), TRUE, ZERO_VEC3, ParticleTypes.FALLING_WATER))
                .addAction(ApplyEntityExtraTagAction.make("return", "projectile", TAG.reference("tag")))
                .addAction(GetEntityTypeAction.make("on_entity_hit_return", ENTITY_HIT, "", "", "is_player"))
                .addAction(GetEntityUUIDAction.make("on_entity_hit_return", ENTITY_HIT, "hit_uuid"))
                .addAction(GetEntityExtraTagAction.make("on_entity_hit_return", PROJECTILE, "tag"))
                .addAction(BooleanActivationAction.make("on_entity_hit_return", "refill", Compiler.compileString(" is_player && (hit_uuid == get_nbt_uuid(tag, 'owner_uuid_return')) ", BOOLEAN), TRUE, FALSE))
                .addAction(MainhandItemTargetAction.make("refill", ENTITY_HIT, "item"))
                .addAction(ActivateAction.make("refill", "do_refill"))
                .addAction(ItemEqualsAction.make("do_refill", "item", new ItemStack(Items.BUCKET), TRUE, ONE, INT.immediate(-1)))
                .addAction(ActivateAction.make("refill", "refill_offhand"))
                .addAction(DeactivateAction.make("do_refill", "refill_offhand"))
                .addAction(ClearTargetsAction.make("refill_offhand", "item"))
                .addAction(OffhandItemTargetAction.make("refill_offhand", ENTITY_HIT, "item"))
                .addAction(ActivateAction.make("refill_offhand", "do_refill"))
                .addAction(ItemEqualsAction.make("do_refill", "item", new ItemStack(Items.BUCKET), TRUE, ONE, INT.immediate(-1)))
                .addAction(GetItemAttributesAction.make("do_refill", "item", "item", "amount", "damage", "item_tag"))
                .addAction(OverrideItemAction.make("do_refill", "item", ONE, INT.reference("amount"), TAG.reference("item_tag"), SpellsUtil.objectToString(Items.WATER_BUCKET, ForgeRegistries.ITEMS)))
                .addAction(GiveItemAction.make("do_refill", "item", Compiler.compileString(" amount - 1 ", INT), INT.reference("amount"), TAG.reference("item_tag"), SpellsUtil.objectToString(Items.BUCKET, ForgeRegistries.ITEMS)))
                .addAction(PlaySoundAction.make("do_refill", OWNER, SoundEvents.BUCKET_FILL, ONE_D, ONE_D))
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_WATER_WHIP_DESC))
                .addTooltip(Component.empty())
                .addTooltip(itemCostTitle(KEY_HAND_ITEM_COST_TITLE))
                .addTooltip(itemCostComponent(new ItemStack(Items.WATER_BUCKET)))
        );
        
        addSpell(Spells.POTION_SHOT, new Spell(modId, "potion_shot", Spells.KEY_POTION_SHOT, 5F)
                .addParameter(DOUBLE, "damage", 10.0)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(MainhandItemTargetAction.make(ACTIVE, OWNER, "item"))
                .addAction(ActivateAction.make(ACTIVE, "shoot"))
                .addAction(ItemEqualsAction.make("shoot", "item", new ItemStack(Items.POTION), TRUE, ONE, INT.immediate(-1)))
                .addAction(ActivateAction.make(ACTIVE, "offhand"))
                .addAction(DeactivateAction.make("shoot", "offhand"))
                .addAction(ClearTargetsAction.make("offhand", "item"))
                .addAction(OffhandItemTargetAction.make("offhand", OWNER, "item"))
                .addAction(ActivateAction.make("offhand", "shoot"))
                .addAction(ItemEqualsAction.make("shoot", "item", new ItemStack(Items.POTION), TRUE, ONE, INT.immediate(-1)))
                .addAction(GetItemAttributesAction.make("shoot", "item", "item", "amount", "damage", "item_tag"))
                .addAction(BooleanActivationAction.make("shoot", "consume", Compiler.compileString(" item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(OverrideItemAction.make("consume", "item", INT.reference("amount"), INT.reference("damage"), TAG.reference("item_tag"), SpellsUtil.objectToString(Items.GLASS_BOTTLE, ForgeRegistries.ITEMS)))
                .addAction(PutVarAction.makeCompoundTag("shoot", Compiler.compileString(" put_nbt_string(new_tag(), 'Potion', get_nbt_string(item_tag, 'Potion')) ", TAG), "tag"))
                .addAction(BurnManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(ShootAction.make("shoot", OWNER, DOUBLE.immediate(2D), ZERO_D, INT.immediate(100), "", "on_entity_hit", "", "projectile"))
                .addAction(ApplyEntityExtraTagAction.make("shoot", "projectile", TAG.reference("tag")))
                .addAction(PlaySoundAction.make("shoot", OWNER, SoundEvents.BOTTLE_EMPTY, ONE_D, ONE_D))
                .addAction(GetEntityExtraTagAction.make("on_entity_hit", PROJECTILE, "tag"))
                .addAction(BooleanActivationAction.make("on_entity_hit", "no_pvp", Compiler.compileString(" !pvp() ", BOOLEAN), TRUE, FALSE))
                .addAction(MovePlayerTargetsAction.make("no_pvp", ENTITY_HIT, ""))
                .addAction(ApplyPotionEffectAction.make("on_entity_hit", ENTITY_HIT, Compiler.compileString(" get_nbt_string(tag, 'Potion') ", STRING)))
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_POTION_SHOT_DESC))
                .addTooltip(Component.empty())
                .addTooltip(itemCostTitle(KEY_HAND_ITEM_COST_TITLE))
                .addTooltip(itemCostComponent(Items.POTION))
        );
        
        addPermanentWalkerSpell(Spells.PERMANENT_FROST_WALKER, Spells.KEY_PERMANENT_FROST_WALKER, Spells.KEY_PERMANENT_FROST_WALKER_DESC, "frost_walker", Fluids.WATER.getFluidType(), Blocks.FROSTED_ICE.defaultBlockState(), true);
        addTemporaryWalkerSpell(Spells.TEMPORARY_FROST_WALKER, Spells.KEY_TEMPORARY_FROST_WALKER, Spells.KEY_TEMPORARY_FROST_WALKER_DESC, "frost_walker", Fluids.WATER.getFluidType(), Blocks.FROSTED_ICE.defaultBlockState(), 16F, true, 400);
        addToggleWalkerSpell(Spells.TOGGLE_FROST_WALKER, Spells.KEY_TOGGLE_FROST_WALKER, Spells.KEY_TOGGLE_FROST_WALKER_DESC, "frost_walker", Fluids.WATER.getFluidType(), Blocks.FROSTED_ICE.defaultBlockState(), 5F, true);
        
        addSpell(Spells.JUMP, new Spell(modId, "jump", Spells.KEY_JUMP, 5F)
                .addParameter(DOUBLE, "speed", 1.5)
                .addAction(SimpleManaCheckAction.make(ACTIVE))
                .addAction(ResetFallDistanceAction.make(ACTIVE, OWNER))
                .addAction(GetEntityPositionDirectionMotionAction.make(ACTIVE, OWNER, "", "", "motion"))
                .addAction(SetMotionAction.make(ACTIVE, OWNER, Compiler.compileString(" vec3(0, get_y(motion) + speed, 0) ", VEC3)))
                .addAction(SpawnParticlesAction.make(ACTIVE, OWNER, ParticleTypes.POOF, INT.immediate(4), DOUBLE.immediate(0.1)))
                .addAction(PlaySoundAction.make(ACTIVE, OWNER, SoundEvents.ENDER_DRAGON_FLAP, ONE_D, ONE_D))
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_JUMP_DESC))
        );
        
        addSpell(Spells.MANA_SOLES, new Spell(modId, "mana_soles", Spells.KEY_MANA_SOLES, 0F)
                .addAction(BooleanActivationAction.make(LIVING_HURT_VICTIM, "reduce", Compiler.compileString(" damage_type == '" + DamageTypes.FALL.location().getPath() + "' ", BOOLEAN), TRUE, TRUE))
                .addAction(GetManaAction.make("reduce", OWNER, "mana"))
                .addAction(PutVarAction.makeDouble("reduce", Compiler.compileString(" min(mana, damage_amount) ", DOUBLE), "reduce_amount"))
                .addAction(PutVarAction.makeBoolean("reduce", Compiler.compileString(" " + EVENT_IS_CANCELED.toString() + " || (reduce_amount >= damage_amount) ", BOOLEAN), EVENT_IS_CANCELED))
                .addAction(BurnManaAction.make("reduce", OWNER, DOUBLE.reference("reduce_amount")))
                .addAction(PutVarAction.makeDouble("reduce", Compiler.compileString(" damage_amount - reduce_amount ", DOUBLE), "damage_amount"))
                .addEventHook(LIVING_HURT_VICTIM)
                .addTooltip(Component.translatable(Spells.KEY_MANA_SOLES_DESC))
        );
        
        addSpell(Spells.FIRE_CHARGE, new Spell(ItemSpellIcon.make(new ItemStack(Items.FIRE_CHARGE)), Spells.KEY_FIRE_CHARGE, 5F)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make(ACTIVE, "consume", Compiler.compileString(" !item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(PlayerHasItemsAction.make(ACTIVE, OWNER, SpellsUtil.objectToString(Items.FIRE_CHARGE, ForgeRegistries.ITEMS), ONE, null, TRUE, TRUE))
                .addAction(ActivateAction.make(ACTIVE, "consume"))
                .addAction(ActivateAction.make("consume", "shoot"))
                .addAction(BurnManaAction.make("consume", OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make("consume", "consume", Compiler.compileString(" item_costs() ", BOOLEAN), FALSE, TRUE))
                .addAction(ConsumePlayerItemsAction.make("consume", OWNER, SpellsUtil.objectToString(Items.FIRE_CHARGE, ForgeRegistries.ITEMS), ONE, null, TRUE))
                .addAction(GetEntityUUIDAction.make("shoot", OWNER, "uuid"))
                .addAction(GetEntityPositionDirectionMotionAction.make("shoot", OWNER, "", "direction", ""))
                .addAction(PutVarAction.makeCompoundTag("shoot", Compiler.compileString(" put_nbt_uuid(new_tag(), 'Owner', uuid) ", TAG), "tag"))
                .addAction(PutVarAction.makeCompoundTag("shoot", Compiler.compileString(" put_nbt_vec3(tag, 'power', direction * 2.0 * 0.1) ", TAG), "tag"))
                .addAction(GetEntityEyePositionAction.make("shoot", OWNER, "position"))
                .addAction(SpawnEntityAction.make("shoot", "fire_charge", SpellsUtil.objectToString(EntityType.FIREBALL, ForgeRegistries.ENTITY_TYPES), "position", VEC3.reference("direction"), ZERO_VEC3, TAG.reference("tag")))
                .addAction(PlaySoundAction.make("shoot", OWNER, SoundEvents.BLAZE_SHOOT, ONE_D, ONE_D))
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_FIRE_CHARGE_DESC))
                .addTooltip(Component.empty())
                .addTooltip(itemCostTitle(KEY_HAND_ITEM_COST_TITLE))
                .addTooltip(itemCostComponent(new ItemStack(Items.FIRE_CHARGE)))
        );
        
        addSpell(Spells.PRESSURIZE, new Spell(modId, "pressurize", Spells.KEY_PRESSURIZE, 4F)
                .addAction(SimpleManaCheckAction.make(ACTIVE))
                .addAction(RangedEntityTargetsAction.make(ACTIVE, "targets", OWNER, DOUBLE.reference("range")))
                .addAction(BooleanActivationAction.make(ACTIVE, "no_pvp", Compiler.compileString(" !pvp() ", BOOLEAN), TRUE, FALSE))
                .addAction(MovePlayerTargetsAction.make("no_pvp", "targets", ""))
                .addAction(SourcedKnockbackAction.make(ACTIVE, "targets", DOUBLE.reference("knockback_strength"), OWNER))
                .addAction(PlaySoundAction.make(ACTIVE, OWNER, SoundEvents.PLAYER_BREATH, ONE_D, ONE_D))
                .addAction(SpawnParticlesAction.make(ACTIVE, "targets", ParticleTypes.POOF, INT.immediate(3), DOUBLE.immediate(0.5D)))
                .addParameter(DOUBLE, "range", 6D)
                .addParameter(DOUBLE, "knockback_strength", 3D)
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_PRESSURIZE_DESC))
        );
        
        addSpell(Spells.INSTANT_MINE, new Spell(modId, "instant_mine", Spells.KEY_INSTANT_MINE, 4F)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(LookAtTargetAction.make(ACTIVE, OWNER, DOUBLE.reference("range"), 0F, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, "on_block_hit", "", ""))
                .addAction(PlayerHarvestBlockAction.make("on_block_hit", OWNER, BLOCK_HIT, Direction.UP))
                .addAction(GetBlockAction.make("on_block_hit", BLOCK_HIT, "", "", "is_air"))
                .addAction(BooleanActivationAction.make("on_block_hit", "burn_mana", BOOLEAN.reference("is_air"), TRUE, FALSE))
                .addAction(BurnManaAction.make("burn_mana", OWNER, DOUBLE.reference(MANA_COST)))
                .addParameter(DOUBLE, "range", 4D)
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_INSTANT_MINE_DESC))
                .addTooltip(Component.empty())
                .addTooltip(itemCostTitle(KEY_HAND_ITEM_REQUIREMENT_TITLE))
                .addTooltip(textItemCostComponent(Component.translatable(Spells.KEY_INSTANT_MINE_DESC_REQUIREMENT), 1))
        );
        
        CompoundTag metalMap = new CompoundTag();
        metalMap.putDouble(ForgeRegistries.ITEMS.getKey(Items.IRON_NUGGET).toString(), Tiers.IRON.getAttackDamageBonus());
        metalMap.putDouble(ForgeRegistries.ITEMS.getKey(Items.GOLD_NUGGET).toString(), Tiers.GOLD.getAttackDamageBonus());
        addSpell(Spells.SPIT_METAL, new Spell(modId, "spit_metal", Spells.KEY_SPIT_METAL, 4F)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(MainhandItemTargetAction.make(ACTIVE, OWNER, "item"))
                .addAction(GetItemAttributesAction.make(ACTIVE, "item", "item_id", "amount", "", ""))
                .addAction(ActivateAction.make(ACTIVE, "offhand"))
                .addAction(BooleanActivationAction.make(ACTIVE, "shoot", Compiler.compileString(" nbt_contains(item_damage_map, item_id) ", BOOLEAN), TRUE, FALSE))
                .addAction(DeactivateAction.make("shoot", "offhand"))
                .addAction(ClearTargetsAction.make("offhand", "item"))
                .addAction(OffhandItemTargetAction.make("offhand", OWNER, "item"))
                .addAction(GetItemAttributesAction.make("offhand", "item", "item_id", "amount", "", ""))
                .addAction(BooleanActivationAction.make("offhand", "shoot", Compiler.compileString(" nbt_contains(item_damage_map, item_id) ", BOOLEAN), TRUE, FALSE))
                .addAction(BurnManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(ShootAction.make("shoot", OWNER, DOUBLE.immediate(2D), ZERO_D, INT.immediate(100), "", "on_entity_hit", "", "projectile"))
                .addAction(PutVarAction.makeDouble("shoot", Compiler.compileString(" base_damage + get_nbt_double(item_damage_map, item_id) ", DOUBLE), "damage"))
                .addAction(ApplyEntityExtraTagAction.make("shoot", "projectile", Compiler.compileString(" put_nbt_double(new_tag(), 'damage', damage) ", TAG)))
                .addAction(BooleanActivationAction.make("shoot", "consume", Compiler.compileString(" item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(ConsumeItemAction.make("consume", "item", ONE))
                .addAction(PlaySoundAction.make(ACTIVE, OWNER, SoundEvents.LLAMA_SPIT, ONE_D, ONE_D))
                .addAction(GetEntityExtraTagAction.make("on_entity_hit", PROJECTILE, "damage_tag"))
                .addAction(BooleanActivationAction.make("on_entity_hit", "no_pvp", Compiler.compileString(" !pvp() ", BOOLEAN), TRUE, FALSE))
                .addAction(MovePlayerTargetsAction.make("no_pvp", ENTITY_HIT, ""))
                .addAction(SourcedDamageAction.make("on_entity_hit", ENTITY_HIT, Compiler.compileString(" get_nbt_double(damage_tag, 'damage') ", DOUBLE), PROJECTILE))
                .addParameter(DOUBLE, "base_damage", 8D)
                .addParameter(TAG, "item_damage_map", metalMap)
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_SPIT_METAL_DESC))
                .addTooltip(Component.empty())
                .addTooltip(itemCostTitle(KEY_HAND_ITEM_COST_TITLE))
                .addTooltip(itemCostComponent(new ItemStack(Items.IRON_NUGGET)))
                .addTooltip(itemCostComponent(new ItemStack(Items.GOLD_NUGGET)))
        );
        
        addSpell(Spells.FLAMETHROWER, new Spell(modId, "flamethrower", Spells.KEY_FLAMETHROWER, 7F)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make(ACTIVE, "success", Compiler.compileString(" !item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(PlayerHasItemsAction.make(ACTIVE, OWNER, SpellsUtil.objectToString(Items.BLAZE_POWDER, ForgeRegistries.ITEMS), ONE, null, TRUE, TRUE))
                .addAction(ActivateAction.make(ACTIVE, "success"))
                .addAction(ActivateAction.make("success", "shoot"))
                .addAction(CopyTargetsAction.make("success", "player", OWNER))
                .addAction(BurnManaAction.make("success", OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make("success", "consume", Compiler.compileString(" item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(ConsumePlayerItemsAction.make("consume", OWNER, SpellsUtil.objectToString(Items.BLAZE_POWDER, ForgeRegistries.ITEMS), ONE, null, TRUE))
                .addAction(CopyTargetsAction.make("on_timeout", "player", HOLDER))
                .addAction(PutVarAction.makeInt("on_timeout", Compiler.compileString(" get_nbt_int(" + DELAY_TAG + ", 'repetitions') ", INT), "repetitions"))
                .addAction(ActivateAction.make("on_timeout", "shoot"))
                .addAction(LabelAction.make("shoot", "loop"))
                .addAction(BooleanActivationAction.make("shoot", "do_shoot", Compiler.compileString(" shots_per_repetition > 0 ", BOOLEAN), TRUE, TRUE))
                .addAction(ShootAction.make("do_shoot", "player", DOUBLE.immediate(2D), DOUBLE.reference("inaccuracy"), INT.immediate(20), "on_block_hit", "on_entity_hit", "", "projectile"))
                .addAction(ParticleEmitterAction.make("do_shoot", "projectile", INT.immediate(20), INT.immediate(4), ONE, ZERO_D, TRUE, ZERO_VEC3, ParticleTypes.LAVA))
                .addAction(ParticleEmitterAction.make("do_shoot", "projectile", INT.immediate(20), ONE, ONE, ZERO_D, TRUE, ZERO_VEC3, ParticleTypes.SMOKE))
                .addAction(PutVarAction.makeInt("do_shoot", Compiler.compileString(" shots_per_repetition - 1 ", INT), "shots_per_repetition"))
                .addAction(JumpAction.make("do_shoot", "loop"))
                .addAction(BooleanActivationAction.make("shoot", "repeat", Compiler.compileString(" repetitions > 1 ", BOOLEAN), TRUE, FALSE))
                .addAction(PlaySoundAction.make("shoot", "player", SoundEvents.BLAZE_SHOOT, ONE_D, ONE_D))
                .addAction(AddDelayedSpellAction.make("repeat", "player", "on_timeout", INT.reference("repetition_delay"), STRING.immediate(""), Compiler.compileString(" put_nbt_int(new_tag(), 'repetitions', repetitions - 1) ", TAG), eventHookMap()))
                .addAction(BooleanActivationAction.make("on_entity_hit", "no_pvp", Compiler.compileString(" !pvp() ", BOOLEAN), TRUE, FALSE))
                .addAction(MovePlayerTargetsAction.make("no_pvp", ENTITY_HIT, ""))
                .addAction(SetOnFireAction.make("on_entity_hit", ENTITY_HIT, INT.reference("fire_seconds")))
                .addParameter(INT, "fire_seconds", 10)
                .addParameter(INT, "shots_per_repetition", 3)
                .addParameter(INT, "repetitions", 5)
                .addParameter(INT, "repetition_delay", 4)
                .addParameter(DOUBLE, "inaccuracy", 15D)
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_FLAMETHROWER_DESC))
                .addTooltip(Component.empty())
                .addTooltip(itemCostTitle(KEY_HAND_ITEM_COST_TITLE))
                .addTooltip(itemCostComponent(new ItemStack(Items.BLAZE_POWDER)))
        );
        
        addPermanentWalkerSpell(Spells.PERMANENT_LAVA_WALKER, Spells.KEY_PERMANENT_LAVA_WALKER, Spells.KEY_PERMANENT_LAVA_WALKER_DESC, "lava_walker", Fluids.LAVA.getFluidType(), Blocks.OBSIDIAN.defaultBlockState(), false);
        addTemporaryWalkerSpell(Spells.TEMPORARY_LAVA_WALKER, Spells.KEY_TEMPORARY_LAVA_WALKER, Spells.KEY_TEMPORARY_LAVA_WALKER_DESC, "lava_walker", Fluids.LAVA.getFluidType(), Blocks.OBSIDIAN.defaultBlockState(), 16F, false, 400);
        addToggleWalkerSpell(Spells.TOGGLE_LAVA_WALKER, Spells.KEY_TOGGLE_LAVA_WALKER, Spells.KEY_TOGGLE_LAVA_WALKER_DESC, "lava_walker", Fluids.LAVA.getFluidType(), Blocks.OBSIDIAN.defaultBlockState(), 5F, false);
        
        addSpell(Spells.SILENCE_TARGET, new Spell(DefaultSpellIcon.make(new ResourceLocation(BuiltInRegisters.SILENCE_EFFECT.getId().getNamespace(), "textures/mob_effect/" + BuiltInRegisters.SILENCE_EFFECT.getId().getPath() + ".png")), Spells.KEY_SILENCE_TARGET, 5F)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(ActivateAction.make(ACTIVE, "bypass"))
                .addAction(PlayerHasItemsAction.make(ACTIVE, OWNER, SpellsUtil.objectToString(Items.AMETHYST_SHARD, ForgeRegistries.ITEMS), ONE, null, TRUE, TRUE))
                .addAction(BooleanActivationAction.make("bypass", ACTIVE, Compiler.compileString(" !item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(LookAtTargetAction.make(ACTIVE, OWNER, DOUBLE.reference("range"), 0.5F, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, "", "on_entity_hit", ""))
                
                .addAction(BooleanActivationAction.make("on_entity_hit", "no_pvp", Compiler.compileString(" !pvp() ", BOOLEAN), TRUE, FALSE))
                .addAction(MovePlayerTargetsAction.make("no_pvp", ENTITY_HIT, ""))
                .addAction(GetTargetGroupSizeAction.make("no_pvp", ENTITY_HIT, "target_size"))
                .addAction(BooleanActivationAction.make("no_pvp", "fail", Compiler.compileString(" target_size <= 0 ", BOOLEAN), TRUE, FALSE))
                .addAction(DeactivateAction.make("fail", "on_entity_hit"))
                
                .addAction(BurnManaAction.make("on_entity_hit", OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make("on_entity_hit", "consume", Compiler.compileString(" item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(ConsumePlayerItemsAction.make("consume", OWNER, SpellsUtil.objectToString(Items.AMETHYST_SHARD, ForgeRegistries.ITEMS), ONE, null, TRUE))
                .addAction(ApplyMobEffectAction.make("on_entity_hit", ENTITY_HIT, STRING.immediate(BuiltInRegisters.SILENCE_EFFECT.getId().toString()), INT.reference("silence_seconds"), ZERO, FALSE, TRUE, TRUE))
                .addAction(PlaySoundAction.make("on_entity_hit", OWNER, SoundEvents.AMETHYST_CLUSTER_HIT, ONE_D, ONE_D))
                .addAction(PlaySoundAction.make("on_entity_hit", ENTITY_HIT, SoundEvents.AMETHYST_CLUSTER_BREAK, ONE_D, ONE_D))
                .addAction(SpawnParticlesAction.make("on_entity_hit", HIT_POSITION, ParticleTypes.POOF, INT.immediate(3), DOUBLE.immediate(0.2)))
                .addEventHook(ACTIVE)
                .addParameter(DOUBLE, "range", 20D)
                .addParameter(INT, "silence_seconds", 15)
                .addTooltip(Component.translatable(Spells.KEY_SILENCE_TARGET_DESC))
        );
        
        addSpell(Spells.RANDOM_TELEPORT, new Spell(modId, "random_teleport", Spells.KEY_RANDOM_TELEPORT, 5F)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(ActivateAction.make(ACTIVE, "bypass"))
                .addAction(PlayerHasItemsAction.make(ACTIVE, OWNER, SpellsUtil.objectToString(Items.CHORUS_FRUIT, ForgeRegistries.ITEMS), ONE, null, TRUE, TRUE))
                .addAction(BooleanActivationAction.make("bypass", ACTIVE, Compiler.compileString(" !item_costs() ", BOOLEAN), TRUE, FALSE))
                
                .addAction(PutVarAction.makeInt(ACTIVE, INT.reference("max_attempts"), "attempts"))
                .addAction(LabelAction.make(ACTIVE, "loop"))
                
                .addAction(ClearTargetsAction.make(ACTIVE, "below"))
                .addAction(ClearTargetsAction.make(ACTIVE, "feet"))
                .addAction(ClearTargetsAction.make(ACTIVE, "head"))
                .addAction(PutVarAction.makeInt(ACTIVE, INT.reference("max_inner_attempts"), "inner_attempts"))
                .addAction(PutVarAction.makeDouble(ACTIVE, Compiler.compileString(" random_double() * 2 * range - range ", DOUBLE), "x"))
                .addAction(PutVarAction.makeDouble(ACTIVE, Compiler.compileString(" min(" + MAX_BLOCK_HEIGHT + ", max(" + MIN_BLOCK_HEIGHT + ", random_double() * 2 * range - range)) ", DOUBLE), "y"))
                .addAction(PutVarAction.makeDouble(ACTIVE, Compiler.compileString(" random_double() * 2 * range - range ", DOUBLE), "z"))
                .addAction(OffsetBlockAction.make(ACTIVE, OWNER, "below", Compiler.compileString(" vec3(x, y, z) ", VEC3)))
                .addAction(OffsetBlockAction.make(ACTIVE, "below", "feet", VEC3.immediate(new Vec3(0, 1, 0))))
                .addAction(OffsetBlockAction.make(ACTIVE, "feet", "head", VEC3.immediate(new Vec3(0, 1, 0))))
                .addAction(LabelAction.make(ACTIVE, "inner_loop"))
                
                .addAction(GetBlockAction.make(ACTIVE, "below", "", "", "below_is_air"))
                .addAction(GetBlockAction.make(ACTIVE, "feet", "", "", "feet_is_air"))
                .addAction(GetBlockAction.make(ACTIVE, "head", "", "", "head_is_air"))
                .addAction(BooleanActivationAction.make(ACTIVE, "success", Compiler.compileString(" !below_is_air && feet_is_air && head_is_air ", BOOLEAN), TRUE, TRUE))
                
                .addAction(DeactivateAction.make("success", ACTIVE))
                .addAction(ClearTargetsAction.make(ACTIVE, "below"))
                .addAction(PickTargetAction.make(ACTIVE, "below", "feet", true, false))
                .addAction(PickTargetAction.make(ACTIVE, "feet", "head", true, false))
                .addAction(OffsetBlockAction.make(ACTIVE, "feet", "head", VEC3.immediate(new Vec3(0, 1, 0))))
                .addAction(PutVarAction.makeInt(ACTIVE, Compiler.compileString(" inner_attempts - 1 ", INT), "inner_attempts"))
                .addAction(BranchAction.make(ACTIVE, "inner_loop", Compiler.compileString(" inner_attempts > 0 ", BOOLEAN)))
                
                .addAction(PutVarAction.makeInt(ACTIVE, Compiler.compileString(" attempts - 1 ", INT), "attempts"))
                .addAction(BranchAction.make(ACTIVE, "loop", Compiler.compileString(" attempts > 0 ", BOOLEAN)))
                .addAction(BurnManaAction.make("success", OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make("success", "consume", Compiler.compileString(" item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(ConsumePlayerItemsAction.make("consume", OWNER, SpellsUtil.objectToString(Items.CHORUS_FRUIT, ForgeRegistries.ITEMS), ONE, null, TRUE))
                .addAction(GetPositionAction.make("success", "feet", "feet_pos"))
                .addAction(PutVarAction.makeDouble("success", Compiler.compileString(" get_y(feet_pos) - floor(get_y(feet_pos))", DOUBLE), "feet_pos_floor"))
                .addAction(OffsetBlockAction.make("success", "feet", "teleport_position", Compiler.compileString("vec3(0, -feet_pos_floor, 0)", VEC3)))
                .addAction(PlaySoundAction.make("success", OWNER, SoundEvents.ENDERMAN_TELEPORT, ONE_D, ONE_D))
                .addAction(TeleportToAction.make("success", OWNER, "teleport_position"))
                .addAction(PlaySoundAction.make("success", OWNER, SoundEvents.ENDERMAN_TELEPORT, ONE_D, ONE_D))
                .addAction(PlaySoundAction.make(ACTIVE, OWNER, SoundEvents.ENDERMAN_SCREAM, ONE_D, ONE_D))
                
                .addParameter(INT, "max_attempts", 10)
                .addParameter(INT, "max_inner_attempts", 10)
                .addParameter(DOUBLE, "range", 32D)
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_RANDOM_TELEPORT_DESC))
                .addTooltip(Component.empty())
                .addTooltip(itemCostTitle(KEY_HAND_ITEM_COST_TITLE))
                .addTooltip(itemCostComponent(new ItemStack(Items.CHORUS_FRUIT)))
        );
        
        addSpell(Spells.FORCED_TELEPORT, new Spell(modId, "forced_teleport", Spells.KEY_FORCED_TELEPORT, 10F)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(ActivateAction.make(ACTIVE, "bypass"))
                .addAction(PlayerHasItemsAction.make(ACTIVE, OWNER, SpellsUtil.objectToString(Items.CHORUS_FRUIT, ForgeRegistries.ITEMS), ONE, null, TRUE, TRUE))
                .addAction(BooleanActivationAction.make("bypass", ACTIVE, Compiler.compileString(" !item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(LookAtTargetAction.make(ACTIVE, OWNER, DOUBLE.reference("target_range"), 0.5F, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, "fail", "on_entity_hit", "fail"))
                .addAction(CopyTargetsAction.make("on_entity_hit", "player", ENTITY_HIT))
                
                .addAction(BooleanActivationAction.make("on_entity_hit", "no_pvp", Compiler.compileString(" !pvp() ", BOOLEAN), TRUE, FALSE))
                .addAction(MovePlayerTargetsAction.make("no_pvp", ENTITY_HIT, ""))
                .addAction(GetTargetGroupSizeAction.make("no_pvp", ENTITY_HIT, "target_size"))
                .addAction(BooleanActivationAction.make("no_pvp", "fail", Compiler.compileString(" target_size <= 0 ", BOOLEAN), TRUE, FALSE))
                
                .addAction(DeactivateAction.make("fail", ACTIVE))
                .addAction(PutVarAction.makeInt(ACTIVE, INT.reference("max_attempts"), "attempts"))
                .addAction(LabelAction.make(ACTIVE, "loop"))
                
                .addAction(ClearTargetsAction.make(ACTIVE, "below"))
                .addAction(ClearTargetsAction.make(ACTIVE, "feet"))
                .addAction(ClearTargetsAction.make(ACTIVE, "head"))
                .addAction(PutVarAction.makeInt(ACTIVE, INT.reference("max_inner_attempts"), "inner_attempts"))
                .addAction(PutVarAction.makeDouble(ACTIVE, Compiler.compileString(" random_double() * 2 * teleport_range - teleport_range ", DOUBLE), "x"))
                .addAction(PutVarAction.makeDouble(ACTIVE, Compiler.compileString(" min(" + MAX_BLOCK_HEIGHT + ", max(" + MIN_BLOCK_HEIGHT + ", random_double() * 2 * teleport_range - teleport_range)) ", DOUBLE), "y"))
                .addAction(PutVarAction.makeDouble(ACTIVE, Compiler.compileString(" random_double() * 2 * teleport_range - teleport_range ", DOUBLE), "z"))
                .addAction(OffsetBlockAction.make(ACTIVE, "player", "below", Compiler.compileString(" vec3(x, y, z) ", VEC3)))
                .addAction(OffsetBlockAction.make(ACTIVE, "below", "feet", VEC3.immediate(new Vec3(0, 1, 0))))
                .addAction(OffsetBlockAction.make(ACTIVE, "feet", "head", VEC3.immediate(new Vec3(0, 1, 0))))
                .addAction(LabelAction.make(ACTIVE, "inner_loop"))
                
                .addAction(GetBlockAction.make(ACTIVE, "below", "", "", "below_is_air"))
                .addAction(GetBlockAction.make(ACTIVE, "feet", "", "", "feet_is_air"))
                .addAction(GetBlockAction.make(ACTIVE, "head", "", "", "head_is_air"))
                .addAction(BooleanActivationAction.make(ACTIVE, "success", Compiler.compileString(" !below_is_air && feet_is_air && head_is_air ", BOOLEAN), TRUE, TRUE))
                
                .addAction(DeactivateAction.make("success", ACTIVE))
                .addAction(ClearTargetsAction.make(ACTIVE, "below"))
                .addAction(PickTargetAction.make(ACTIVE, "below", "feet", true, false))
                .addAction(PickTargetAction.make(ACTIVE, "feet", "head", true, false))
                .addAction(OffsetBlockAction.make(ACTIVE, "feet", "head", VEC3.immediate(new Vec3(0, 1, 0))))
                .addAction(PutVarAction.makeInt(ACTIVE, Compiler.compileString(" inner_attempts - 1 ", INT), "inner_attempts"))
                .addAction(BranchAction.make(ACTIVE, "inner_loop", Compiler.compileString(" inner_attempts > 0 ", BOOLEAN)))
                
                .addAction(PutVarAction.makeInt(ACTIVE, Compiler.compileString(" attempts - 1 ", INT), "attempts"))
                .addAction(BranchAction.make(ACTIVE, "loop", Compiler.compileString(" attempts > 0 ", BOOLEAN)))
                .addAction(BurnManaAction.make("success", OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make("success", "consume", Compiler.compileString(" item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(ConsumePlayerItemsAction.make("consume", OWNER, SpellsUtil.objectToString(Items.CHORUS_FRUIT, ForgeRegistries.ITEMS), ONE, null, TRUE))
                .addAction(GetPositionAction.make("success", "feet", "feet_pos"))
                .addAction(PutVarAction.makeDouble("success", Compiler.compileString(" get_y(feet_pos) - floor(get_y(feet_pos))", DOUBLE), "feet_pos_floor"))
                .addAction(OffsetBlockAction.make("success", "feet", "teleport_position", Compiler.compileString("vec3(0, -feet_pos_floor, 0)", VEC3)))
                .addAction(PlaySoundAction.make("success", "player", SoundEvents.ENDERMAN_TELEPORT, ONE_D, ONE_D))
                .addAction(TeleportToAction.make("success", "player", "teleport_position"))
                .addAction(PlaySoundAction.make("success", "player", SoundEvents.ENDERMAN_TELEPORT, ONE_D, ONE_D))
                .addAction(PlaySoundAction.make("fail", OWNER, SoundEvents.ENDERMAN_SCREAM, ONE_D, ONE_D))
                
                .addParameter(INT, "max_attempts", 10)
                .addParameter(INT, "max_inner_attempts", 10)
                .addParameter(DOUBLE, "teleport_range", 32D)
                .addParameter(DOUBLE, "target_range", 32D)
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_FORCED_TELEPORT_DESC))
                .addTooltip(Component.empty())
                .addTooltip(itemCostTitle(KEY_HAND_ITEM_COST_TITLE))
                .addTooltip(itemCostComponent(new ItemStack(Items.CHORUS_FRUIT)))
        );
        
        addSpell(Spells.TELEPORT, new Spell(modId, "teleport", Spells.KEY_TELEPORT, 10F)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make(ACTIVE, "success", Compiler.compileString(" !item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(PlayerHasItemsAction.make(ACTIVE, OWNER, SpellsUtil.objectToString(Items.CHORUS_FRUIT, ForgeRegistries.ITEMS), ONE, null, TRUE, TRUE))
                .addAction(ActivateAction.make(ACTIVE, "success"))
                .addAction(LookAtTargetAction.make("success", OWNER, DOUBLE.reference("range"), 0.5F, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, "on_block_hit", "on_entity_hit", "on_miss"))
                .addAction(CopyTargetsAction.make("on_entity_hit", "teleport_position", ENTITY_HIT))
                .addAction(ActivateAction.make("on_entity_hit", "teleport"))
                .addAction(OffsetBlockAction.make("on_block_hit", BLOCK_HIT, "teleport_position", VEC3.immediate(new Vec3(0, 0.5, 0))))
                .addAction(ActivateAction.make("on_block_hit", "teleport"))
                .addAction(CopyTargetsAction.make("on_miss", "teleport_position", HIT_POSITION))
                .addAction(ActivateAction.make("on_miss", "teleport"))
                .addAction(BurnManaAction.make("teleport", OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make("teleport", "consume", Compiler.compileString(" item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(ConsumePlayerItemsAction.make("consume", OWNER, SpellsUtil.objectToString(Items.CHORUS_FRUIT, ForgeRegistries.ITEMS), ONE, null, TRUE))
                .addAction(PlaySoundAction.make("teleport", OWNER, SoundEvents.ENDERMAN_TELEPORT, ONE_D, ONE_D))
                .addAction(TeleportToAction.make("teleport", OWNER, "teleport_position"))
                .addAction(PlaySoundAction.make("teleport", OWNER, SoundEvents.ENDERMAN_TELEPORT, ONE_D, ONE_D))
                .addParameter(DOUBLE, "range", 32D)
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_TELEPORT_DESC))
                .addTooltip(Component.empty())
                .addTooltip(itemCostTitle(KEY_HAND_ITEM_COST_TITLE))
                .addTooltip(itemCostComponent(new ItemStack(Items.CHORUS_FRUIT)))
        );
        
        addSpell(Spells.LIGHTNING_STRIKE, new Spell(modId, "lightning_strike", Spells.KEY_LIGHTNING_STRIKE, 8F)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(ActivateAction.make(ACTIVE, "bypass"))
                .addAction(PlayerHasItemsAction.make(ACTIVE, OWNER, SpellsUtil.objectToString(Items.COPPER_INGOT, ForgeRegistries.ITEMS), ONE, null, TRUE, TRUE))
                .addAction(BooleanActivationAction.make("bypass", ACTIVE, Compiler.compileString(" !item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(LookAtTargetAction.make(ACTIVE, OWNER, DOUBLE.reference("range"), 0.5F, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, "on_block_hit", "on_entity_hit", ""))
                
                .addAction(BooleanActivationAction.make("on_entity_hit", "no_pvp", Compiler.compileString(" !pvp() ", BOOLEAN), TRUE, FALSE))
                .addAction(MovePlayerTargetsAction.make("no_pvp", ENTITY_HIT, ""))
                .addAction(GetTargetGroupSizeAction.make("no_pvp", ENTITY_HIT, "target_size"))
                .addAction(BooleanActivationAction.make("no_pvp", "fail", Compiler.compileString(" target_size <= 0 ", BOOLEAN), TRUE, FALSE))
                .addAction(DeactivateAction.make("fail", "on_entity_hit"))
                
                .addAction(CopyTargetsAction.make("on_block_hit", "position", BLOCK_HIT))
                .addAction(CopyTargetsAction.make("on_entity_hit", "position", ENTITY_HIT))
                .addAction(ActivateAction.make("on_block_hit", "on_hit"))
                .addAction(ActivateAction.make("on_entity_hit", "on_hit"))
                
                .addAction(BurnManaAction.make("on_hit", OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make("on_hit", "consume", Compiler.compileString(" item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(ConsumePlayerItemsAction.make("consume", OWNER, SpellsUtil.objectToString(Items.COPPER_INGOT, ForgeRegistries.ITEMS), ONE, null, TRUE))
                .addAction(SpawnEntityAction.make("on_hit", "", SpellsUtil.objectToString(EntityType.LIGHTNING_BOLT, ForgeRegistries.ENTITY_TYPES), "position", ZERO_VEC3, ZERO_VEC3, null))
                .addParameter(DOUBLE, "range", 200D)
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_LIGHTNING_STRIKE_DESC))
                .addTooltip(Component.empty())
                .addTooltip(itemCostTitle(KEY_HAND_ITEM_COST_TITLE))
                .addTooltip(itemCostComponent(new ItemStack(Items.COPPER_INGOT)))
        );
        
        addSpell(Spells.DRAIN_FLAME, new Spell(modId, "drain_flame", Spells.KEY_DRAIN_FLAME, 0F)
                .addAction(LookAtTargetAction.make(ACTIVE, OWNER, DOUBLE.reference("range"), 0F, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, "on_block_hit", "", ""))
                .addAction(CubeBlockTargetsAction.make("on_block_hit", BLOCK_HIT, "blocks", Compiler.compileString(" vec3(-radius, -radius, -radius) ", VEC3), Compiler.compileString(" vec3(radius, radius, radius) ", VEC3)))
                .addAction(LabelAction.make("on_block_hit", "loop"))
                .addAction(ClearTargetsAction.make("on_block_hit", "block_to_check"))
                .addAction(PickTargetAction.make("on_block_hit", "block_to_check", "blocks", true, true))
                .addAction(GetBlockAction.make("on_block_hit", "block_to_check", "block_type", "", ""))
                .addAction(BooleanActivationAction.make("on_block_hit", "success", Compiler.compileString(" block_type == '" + ForgeRegistries.BLOCKS.getKey(Blocks.FIRE).toString() + "' || block_type == '" + ForgeRegistries.BLOCKS.getKey(Blocks.SOUL_FIRE) + "' ", BOOLEAN), TRUE, FALSE))
                .addAction(DeactivateAction.make("success", "on_block_hit"))
                .addAction(JumpAction.make("on_block_hit", "loop"))
                .addAction(RemoveBlockAction.make("success", "block_to_check"))
                .addAction(PlaySoundAction.make("success", OWNER, SoundEvents.FIRE_EXTINGUISH, ONE_D, ONE_D))
                .addAction(PlaySoundAction.make("success", "block_to_check", SoundEvents.FIRE_EXTINGUISH, ONE_D, ONE_D))
                .addAction(HomeAction.make("success", "block_to_check", OWNER, ONE_D, INT.immediate(100), "", "owner_hit", "", ""))
                .addAction(ApplyMobEffectAction.make("owner_hit", ENTITY_HIT, SpellsUtil.objectToString(BuiltInRegisters.REPLENISHMENT_EFFECT.get(), ForgeRegistries.MOB_EFFECTS), INT.reference("replenishment_duration"), ZERO, FALSE, TRUE, TRUE))
                .addAction(PlaySoundAction.make("owner_hit", ENTITY_HIT, SoundEvents.FIRE_AMBIENT, ONE_D, ONE_D))
                .addParameter(DOUBLE, "range", 50D)
                .addParameter(INT, "replenishment_duration", 100)
                .addParameter(INT, "radius", 1)
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_DRAIN_FLAME_DESC))
        );
        
        addSpell(Spells.GROWTH, new Spell(modId, "growth", Spells.KEY_GROWTH, 4F)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make(ACTIVE, "success", Compiler.compileString(" !item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(PlayerHasItemsAction.make(ACTIVE, OWNER, SpellsUtil.objectToString(Items.BONE_MEAL, ForgeRegistries.ITEMS), ONE, null, TRUE, TRUE))
                .addAction(BurnManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(ActivateAction.make(ACTIVE, "success"))
                .addAction(BooleanActivationAction.make("success", "consume", Compiler.compileString(" item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(ConsumePlayerItemsAction.make("consume", OWNER, SpellsUtil.objectToString(Items.BONE_MEAL, ForgeRegistries.ITEMS), ONE, null, TRUE))
                .addAction(CubeBlockTargetsAction.make("success", OWNER, "blocks", Compiler.compileString(" vec3(-range, -1, -range) ", VEC3), Compiler.compileString(" vec3(range, 1, range) ", VEC3)))
                .addAction(UseItemOnBlockAction.make("success", OWNER, "blocks", new ItemStack(Items.BONE_MEAL), false, Direction.UP))
                .addAction(SpawnParticlesAction.make("success", "blocks", ParticleTypes.POOF, ONE, DOUBLE.immediate(0.25D)))
                .addAction(PlaySoundAction.make("success", OWNER, SoundEvents.BONE_MEAL_USE, ONE_D, ONE_D))
                .addParameter(INT, "range", 3)
                .addParameter(INT, "duration", 20)
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_GROWTH_DESC))
                .addTooltip(Component.empty())
                .addTooltip(itemCostTitle(KEY_HAND_ITEM_COST_TITLE))
                .addTooltip(itemCostComponent(new ItemStack(Items.BONE_MEAL)))
        );
        
        addSpell(Spells.GHAST, new Spell(AdvancedSpellIcon.make(new ResourceLocation("textures/entity/ghast/ghast_shooting.png"), 16, 16, 16, 16, 64, 32), Spells.KEY_GHAST, 4F)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make(ACTIVE, "success", Compiler.compileString(" !item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(PlayerHasItemsAction.make(ACTIVE, OWNER, SpellsUtil.objectToString(Items.FIRE_CHARGE, ForgeRegistries.ITEMS), ONE, null, TRUE, TRUE))
                .addAction(ActivateAction.make(ACTIVE, "success"))
                .addAction(BurnManaAction.make("success", OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make("success", "consume", Compiler.compileString(" item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(ConsumePlayerItemsAction.make("consume", OWNER, SpellsUtil.objectToString(Items.FIRE_CHARGE, ForgeRegistries.ITEMS), ONE, null, TRUE))
                .addAction(AddDelayedSpellAction.make("success", OWNER, "sound", INT.immediate(10), STRING.immediate(""), EMPTY_TAG, eventHookMap()))
                .addAction(AddDelayedSpellAction.make("success", OWNER, "shoot", INT.immediate(20), STRING.immediate(""), EMPTY_TAG, eventHookMap()))
                .addAction(PlaySoundAction.make("sound", HOLDER, SoundEvents.GHAST_WARN, ONE_D, ONE_D))
                .addAction(GetEntityUUIDAction.make("shoot", HOLDER, "uuid"))
                .addAction(GetEntityPositionDirectionMotionAction.make("shoot", HOLDER, "", "direction", ""))
                .addAction(PutVarAction.makeCompoundTag("shoot", Compiler.compileString(" put_nbt_uuid(new_tag(), 'Owner', uuid) ", TAG), "tag"))
                .addAction(PutVarAction.makeCompoundTag("shoot", Compiler.compileString(" put_nbt_vec3(tag, 'power', direction * 2.0 * 0.1) ", TAG), "tag"))
                .addAction(GetEntityEyePositionAction.make("shoot", HOLDER, "position"))
                .addAction(SpawnEntityAction.make("shoot", "fire_charge", SpellsUtil.objectToString(EntityType.FIREBALL, ForgeRegistries.ENTITY_TYPES), "position", VEC3.reference("direction"), ZERO_VEC3, TAG.reference("tag")))
                .addAction(PlaySoundAction.make("shoot", HOLDER, SoundEvents.GHAST_SHOOT, ONE_D, ONE_D))
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_GHAST_DESC))
                .addTooltip(Component.empty())
                .addTooltip(itemCostTitle(KEY_HAND_ITEM_COST_TITLE))
                .addTooltip(itemCostComponent(new ItemStack(Items.FIRE_CHARGE)))
        );
        
        addSpell(Spells.ENDER_ARMY, new Spell(modId, "ender_army", Spells.KEY_ENDER_ARMY, 20F)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make(ACTIVE, "success", Compiler.compileString(" !item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(PlayerHasItemsAction.make(ACTIVE, OWNER, SpellsUtil.objectToString(Items.DRAGON_HEAD, ForgeRegistries.ITEMS), ONE, null, TRUE, TRUE))
                .addAction(ActivateAction.make(ACTIVE, "success"))
                .addAction(LookAtTargetAction.make("success", OWNER, DOUBLE.reference("target_range"), 0.5F, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, "", "on_entity_hit", ""))
                
                .addAction(BooleanActivationAction.make("on_entity_hit", "no_pvp", Compiler.compileString(" !pvp() ", BOOLEAN), TRUE, FALSE))
                .addAction(MovePlayerTargetsAction.make("no_pvp", ENTITY_HIT, ""))
                .addAction(GetTargetGroupSizeAction.make("no_pvp", ENTITY_HIT, "target_size"))
                .addAction(BooleanActivationAction.make("no_pvp", "fail", Compiler.compileString(" target_size <= 0 ", BOOLEAN), TRUE, FALSE))
                .addAction(DeactivateAction.make("fail", "on_entity_hit"))
                
                .addAction(BurnManaAction.make("on_entity_hit", OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make("on_entity_hit", "consume", Compiler.compileString(" item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(ConsumePlayerItemsAction.make("consume", OWNER, SpellsUtil.objectToString(Items.DRAGON_HEAD, ForgeRegistries.ITEMS), ONE, null, TRUE))
                .addAction(RangedEntityTargetsAction.make("on_entity_hit", "targets", ENTITY_HIT, DOUBLE.reference("enderman_range")))
                .addAction(LabelAction.make("on_entity_hit", "loop"))
                .addAction(ClearTargetsAction.make("on_entity_hit", "to_check"))
                .addAction(PickTargetAction.make("on_entity_hit", "to_check", "targets", true, false))
                .addAction(GetEntityTypeAction.make("on_entity_hit", "to_check", "type", "", ""))
                .addAction(BooleanActivationAction.make("on_entity_hit", "move_entity", Compiler.compileString(" type == '" + ForgeRegistries.ENTITY_TYPES.getKey(EntityType.ENDERMAN).toString() + "' ", BOOLEAN), TRUE, FALSE))
                .addAction(CopyTargetsAction.make("move_entity", "endermen", "to_check"))
                .addAction(DeactivateAction.make("move_entity", "move_entity"))
                .addAction(GetTargetGroupSizeAction.make("on_entity_hit", "targets", "size"))
                .addAction(BranchAction.make("on_entity_hit", "loop", Compiler.compileString(" size > 0 ", BOOLEAN)))
                .addAction(SetMobTargetAction.make("on_entity_hit", ENTITY_HIT, "endermen"))
                .addAction(PlaySoundAction.make("on_entity_hit", OWNER, SoundEvents.ENDERMAN_SCREAM, ONE_D, ONE_D))
                .addAction(PlaySoundAction.make("on_entity_hit", ENTITY_HIT, SoundEvents.ENDERMAN_SCREAM, ONE_D, ONE_D))
                .addParameter(DOUBLE, "target_range", 50D)
                .addParameter(DOUBLE, "enderman_range", 40D)
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_ENDER_ARMY_DESC))
                .addTooltip(Component.empty())
                .addTooltip(itemCostTitle(KEY_HAND_ITEM_COST_TITLE))
                .addTooltip(itemCostComponent(new ItemStack(Items.DRAGON_HEAD)))
        );
        
        addSpell(Spells.EVOKER_FANGS, new Spell(LayeredSpellIcon.make(ImmutableList.of(
                        AdvancedSpellIcon.make(new ResourceLocation("textures/entity/illager/evoker.png"), 8, 8, 8, 10, 64, 64, 0, -2),
                        AdvancedSpellIcon.make(new ResourceLocation("textures/entity/illager/evoker.png"), 22, 26, 8, 4, 64, 64, 0, 5),
                        AdvancedSpellIcon.make(new ResourceLocation("textures/entity/illager/evoker.png"), 6, 44, 8, 4, 64, 64, 0, 5),
                        AdvancedSpellIcon.make(new ResourceLocation("textures/entity/illager/evoker.png"), 26, 2, 2, 4, 64, 64, 0, 2)
                )), Spells.KEY_EVOKER_FANGS, 6F)
                        .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                        .addAction(LookAtTargetAction.make(ACTIVE, OWNER, DOUBLE.reference("range"), 0.5F, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, "", "", ""))
                        .addAction(GetEntityUUIDAction.make(ACTIVE, OWNER, "owner_uuid"))
                        .addAction(PutVarAction.makeCompoundTag(ACTIVE, Compiler.compileString(" put_nbt_uuid(new_tag(), 'Owner', owner_uuid) ", TAG), "tag"))
                        
                        .addAction(GetPositionAction.make(ACTIVE, HIT_POSITION, "target_pos"))
                        .addAction(GetEntityEyePositionAction.make(ACTIVE, OWNER, "eye_position"))
                        .addAction(GetPositionAction.make(ACTIVE, "eye_position", "player_pos1"))
                        .addAction(GetPositionAction.make(ACTIVE, OWNER, "player_pos2"))
                        .addAction(GetEntityPositionDirectionMotionAction.make(ACTIVE, OWNER, "", "look", ""))
                        .addAction(PutVarAction.makeVec3(ACTIVE, Compiler.compileString(" get_y(look) >= 0 ? player_pos2 : player_pos1 ", VEC3), "player_pos"))
                        .addAction(PutVarAction.makeInt(ACTIVE, Compiler.compileString(" ceil(max(get_y(player_pos), get_y(target_pos))) + 1 ", INT), "max_y"))
                        .addAction(PutVarAction.makeInt(ACTIVE, Compiler.compileString(" floor(min(get_y(player_pos), get_y(target_pos))) - 2 ", INT), "min_y"))
                        .addAction(PutVarAction.makeVec3(ACTIVE, Compiler.compileString(" target_pos - player_pos ", VEC3), "vector"))
                        .addAction(PutVarAction.makeInt(ACTIVE, Compiler.compileString(" max(1, ceil(2 * sqrt(length(vector)))) ", INT), "fangs"))
                        .addAction(PutVarAction.makeInt(ACTIVE, 0, "fangs_spawned"))
                        
                        .addAction(PutVarAction.makeInt(ACTIVE, 1, "fang"))
                        .addAction(LabelAction.make(ACTIVE, "outer_loop"))
                        
                        .addAction(ClearTargetsAction.make(ACTIVE, "position"))
                        .addAction(PutVarAction.makeVec3(ACTIVE, Compiler.compileString(" (vector * fang) / fangs ", VEC3), "offset"))
                        
                        .addAction(ClearTargetsAction.make(ACTIVE, "position"))
                        .addAction(OffsetBlockAction.make(ACTIVE, "eye_position", "position", VEC3.reference("offset")))
                        .addAction(GetPositionAction.make(ACTIVE, "position", "position_var"))
                        .addAction(PutVarAction.makeDouble(ACTIVE, Compiler.compileString(" get_x(position_var) ", DOUBLE), "x"))
                        .addAction(PutVarAction.makeDouble(ACTIVE, Compiler.compileString(" get_z(position_var) ", DOUBLE), "z"))
                        .addAction(PutVarAction.moveInt(ACTIVE, "max_y", "y"))
                        
                        .addAction(ActivateAction.make(ACTIVE, "repeat"))
                        .addAction(DeactivateAction.make(ACTIVE, "success"))
                        .addAction(LabelAction.make(ACTIVE, "inner_loop"))
                        
                        .addAction(ClearTargetsAction.make(ACTIVE, "block"))
                        .addAction(PositionToTargetAction.make(ACTIVE, "block", Compiler.compileString(" vec3(x, y, z) ", VEC3)))
                        .addAction(GetBlockAttributesAction.make(ACTIVE, "block", "", "", "has_collider"))
                        .addAction(BooleanActivationAction.make(ACTIVE, "success", BOOLEAN.reference("has_collider"), TRUE, FALSE))
                        
                        .addAction(ClearTargetsAction.make("success", "above"))
                        .addAction(PositionToTargetAction.make("success", "above", Compiler.compileString(" vec3(x, y + 1, z) ", VEC3)))
                        .addAction(PutVarAction.makeCompoundTag("success", Compiler.compileString(" put_nbt_int(tag, 'Warmup', fang) ", TAG), "delayed_tag"))
                        .addAction(SpawnEntityAction.make("success", "", STRING.immediate(ForgeRegistries.ENTITY_TYPES.getKey(EntityType.EVOKER_FANGS).toString()), "above", VEC3.reference("look"), ZERO_VEC3, TAG.reference("delayed_tag")))
                        .addAction(PutVarAction.makeInt("success", Compiler.compileString(" fangs_spawned + 1 ", INT), "fangs_spawned"))
                        
                        .addAction(DeactivateAction.make("success", "repeat"))
                        .addAction(PutVarAction.makeInt("repeat", Compiler.compileString(" y - 1 ", INT), "y"))
                        .addAction(BranchAction.make("repeat", "inner_loop", Compiler.compileString(" y >= min_y ", BOOLEAN)))
                        
                        .addAction(PutVarAction.makeInt(ACTIVE, Compiler.compileString(" fang + 1 ", INT), "fang"))
                        .addAction(BranchAction.make(ACTIVE, "outer_loop", Compiler.compileString(" fang <= fangs ", BOOLEAN)))
                        .addAction(BooleanActivationAction.make(ACTIVE, ACTIVE, Compiler.compileString(" fangs_spawned > 0 ", BOOLEAN), FALSE, TRUE))
                        .addAction(BurnManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                        
                        .addParameter(DOUBLE, "range", 20D)
                        .addEventHook(ACTIVE)
                        .addTooltip(Component.translatable(Spells.KEY_EVOKER_FANGS_DESC))
        );
        
        addSpell(Spells.POCKET_ROCKET, new Spell(ItemSpellIcon.make(new ItemStack(Items.FIREWORK_ROCKET)), Spells.KEY_POCKET_ROCKET, 8F)
                .addAction(HasManaAction.make(ACTIVE, OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make(ACTIVE, "success", Compiler.compileString(" !item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(PlayerHasItemsAction.make(ACTIVE, OWNER, SpellsUtil.objectToString(Items.GUNPOWDER, ForgeRegistries.ITEMS), ONE, null, TRUE, TRUE))
                .addAction(ActivateAction.make(ACTIVE, "success"))
                .addAction(BurnManaAction.make("success", OWNER, DOUBLE.reference(MANA_COST)))
                .addAction(BooleanActivationAction.make("success", "consume", Compiler.compileString(" item_costs() ", BOOLEAN), TRUE, FALSE))
                .addAction(ConsumePlayerItemsAction.make("consume", OWNER, SpellsUtil.objectToString(Items.GUNPOWDER, ForgeRegistries.ITEMS), ONE, null, TRUE))
                .addAction(LabelAction.make("success", "loop"))
                .addAction(PutVarAction.makeInt("success", Compiler.compileString(" repetitions - 1 ", INT), "repetitions"))
                .addAction(AddDelayedSpellAction.make("success", OWNER, "fire", Compiler.compileString(" repetitions * time_delay_ticks ", INT), STRING.immediate(""), EMPTY_TAG, eventHookMap()))
                .addAction(BranchAction.make("success", "loop", Compiler.compileString(" repetitions > 1 ", BOOLEAN)))
                .addAction(CopyTargetsAction.make("success", "player", OWNER))
                .addAction(CopyTargetsAction.make("fire", "player", HOLDER))
                .addAction(ActivateAction.make("success", "fire"))
                .addAction(UseItemAction.make("fire", "player", new ItemStack(Items.FIREWORK_ROCKET), false))
                .addParameter(INT, "repetitions", 4)
                .addParameter(INT, "time_delay_ticks", 30)
                .addEventHook(ACTIVE)
                .addTooltip(Component.translatable(Spells.KEY_POCKET_ROCKET_DESC))
                .addTooltip(itemCostComponent(Items.GUNPOWDER))
        );
        
        addPermanentEffectSpell(Spells.PERMANENT_REPLENISHMENT, Spells.KEY_PERMANENT_REPLENISHMENT, Spells.KEY_PERMANENT_REPLENISHMENT_DESC, BuiltInRegisters.REPLENISHMENT_EFFECT.get(), 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_REPLENISHMENT, Spells.KEY_TEMPORARY_REPLENISHMENT, Spells.KEY_TEMPORARY_REPLENISHMENT_DESC, BuiltInRegisters.REPLENISHMENT_EFFECT.get(), 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_REPLENISHMENT, Spells.KEY_TOGGLE_REPLENISHMENT, Spells.KEY_TOGGLE_REPLENISHMENT_DESC, BuiltInRegisters.REPLENISHMENT_EFFECT.get(), 4F, 50, 0);
        
        addPermanentEffectSpell(Spells.PERMANENT_MAGIC_IMMUNE, Spells.KEY_PERMANENT_MAGIC_IMMUNE, Spells.KEY_PERMANENT_MAGIC_IMMUNE_DESC, BuiltInRegisters.MAGIC_IMMUNE_EFFECT.get(), 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_MAGIC_IMMUNE, Spells.KEY_TEMPORARY_MAGIC_IMMUNE, Spells.KEY_TEMPORARY_MAGIC_IMMUNE_DESC, BuiltInRegisters.MAGIC_IMMUNE_EFFECT.get(), 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_MAGIC_IMMUNE, Spells.KEY_TOGGLE_MAGIC_IMMUNE, Spells.KEY_TOGGLE_MAGIC_IMMUNE_DESC, BuiltInRegisters.MAGIC_IMMUNE_EFFECT.get(), 4F, 50, 0);
        
        addPermanentEffectSpell(Spells.PERMANENT_SPEED, Spells.KEY_PERMANENT_SPEED, Spells.KEY_PERMANENT_SPEED_DESC, MobEffects.MOVEMENT_SPEED, 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_SPEED, Spells.KEY_TEMPORARY_SPEED, Spells.KEY_TEMPORARY_SPEED_DESC, MobEffects.MOVEMENT_SPEED, 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_SPEED, Spells.KEY_TOGGLE_SPEED, Spells.KEY_TOGGLE_SPEED_DESC, MobEffects.MOVEMENT_SPEED, 4F, 50, 0);
        
        addPermanentEffectSpell(Spells.PERMANENT_JUMP_BOOST, Spells.KEY_PERMANENT_JUMP_BOOST, Spells.KEY_PERMANENT_JUMP_BOOST_DESC, MobEffects.JUMP, 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_JUMP_BOOST, Spells.KEY_TEMPORARY_JUMP_BOOST, Spells.KEY_TEMPORARY_JUMP_BOOST_DESC, MobEffects.JUMP, 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_JUMP_BOOST, Spells.KEY_TOGGLE_JUMP_BOOST, Spells.KEY_TOGGLE_JUMP_BOOST_DESC, MobEffects.JUMP, 4F, 50, 0);
        
        addPermanentEffectSpell(Spells.PERMANENT_DOLPHINS_GRACE, Spells.KEY_PERMANENT_DOLPHINS_GRACE, Spells.KEY_PERMANENT_DOLPHINS_GRACE_DESC, MobEffects.DOLPHINS_GRACE, 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_DOLPHINS_GRACE, Spells.KEY_TEMPORARY_DOLPHINS_GRACE, Spells.KEY_TEMPORARY_DOLPHINS_GRACE_DESC, MobEffects.DOLPHINS_GRACE, 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_DOLPHINS_GRACE, Spells.KEY_TOGGLE_DOLPHINS_GRACE, Spells.KEY_TOGGLE_DOLPHINS_GRACE_DESC, MobEffects.DOLPHINS_GRACE, 4F, 50, 0);
        
        addPermanentEffectSpell(Spells.PERMANENT_WATER_BREATHING, Spells.KEY_PERMANENT_WATER_BREATHING, Spells.KEY_PERMANENT_WATER_BREATHING_DESC, MobEffects.WATER_BREATHING, 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_WATER_BREATHING, Spells.KEY_TEMPORARY_WATER_BREATHING, Spells.KEY_TEMPORARY_WATER_BREATHING_DESC, MobEffects.WATER_BREATHING, 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_WATER_BREATHING, Spells.KEY_TOGGLE_WATER_BREATHING, Spells.KEY_TOGGLE_WATER_BREATHING_DESC, MobEffects.WATER_BREATHING, 4F, 50, 0);
        
        addPermanentEffectSpell(Spells.PERMANENT_SLOW_FALLING, Spells.KEY_PERMANENT_SLOW_FALLING, Spells.KEY_PERMANENT_SLOW_FALLING_DESC, MobEffects.SLOW_FALLING, 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_SLOW_FALLING, Spells.KEY_TEMPORARY_SLOW_FALLING, Spells.KEY_TEMPORARY_SLOW_FALLING_DESC, MobEffects.SLOW_FALLING, 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_SLOW_FALLING, Spells.KEY_TOGGLE_SLOW_FALLING, Spells.KEY_TOGGLE_SLOW_FALLING_DESC, MobEffects.SLOW_FALLING, 4F, 50, 0);
        
        addPermanentEffectSpell(Spells.PERMANENT_HASTE, Spells.KEY_PERMANENT_HASTE, Spells.KEY_PERMANENT_HASTE_DESC, MobEffects.DIG_SPEED, 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_HASTE, Spells.KEY_TEMPORARY_HASTE, Spells.KEY_TEMPORARY_HASTE_DESC, MobEffects.DIG_SPEED, 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_HASTE, Spells.KEY_TOGGLE_HASTE, Spells.KEY_TOGGLE_HASTE_DESC, MobEffects.DIG_SPEED, 4F, 50, 0);
        
        addPermanentEffectSpell(Spells.PERMANENT_REGENERATION, Spells.KEY_PERMANENT_REGENERATION, Spells.KEY_PERMANENT_REGENERATION_DESC, MobEffects.REGENERATION, 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_REGENERATION, Spells.KEY_TEMPORARY_REGENERATION, Spells.KEY_TEMPORARY_REGENERATION_DESC, MobEffects.REGENERATION, 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_REGENERATION, Spells.KEY_TOGGLE_REGENERATION, Spells.KEY_TOGGLE_REGENERATION_DESC, MobEffects.REGENERATION, 4F, 50, 0);
        
        addPermanentEffectSpell(Spells.PERMANENT_FIRE_RESISTANCE, Spells.KEY_PERMANENT_FIRE_RESISTANCE, Spells.KEY_PERMANENT_FIRE_RESISTANCE_DESC, MobEffects.FIRE_RESISTANCE, 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_FIRE_RESISTANCE, Spells.KEY_TEMPORARY_FIRE_RESISTANCE, Spells.KEY_TEMPORARY_FIRE_RESISTANCE_DESC, MobEffects.FIRE_RESISTANCE, 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_FIRE_RESISTANCE, Spells.KEY_TOGGLE_FIRE_RESISTANCE, Spells.KEY_TOGGLE_FIRE_RESISTANCE_DESC, MobEffects.FIRE_RESISTANCE, 4F, 50, 0);
        
        addPermanentEffectSpell(Spells.PERMANENT_NIGHT_VISION, Spells.KEY_PERMANENT_NIGHT_VISION, Spells.KEY_PERMANENT_NIGHT_VISION_DESC, MobEffects.NIGHT_VISION, 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_NIGHT_VISION, Spells.KEY_TEMPORARY_NIGHT_VISION, Spells.KEY_TEMPORARY_NIGHT_VISION_DESC, MobEffects.NIGHT_VISION, 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_NIGHT_VISION, Spells.KEY_TOGGLE_NIGHT_VISION, Spells.KEY_TOGGLE_NIGHT_VISION_DESC, MobEffects.NIGHT_VISION, 4F, 50, 0);
        
        addPermanentEffectSpell(Spells.PERMANENT_STRENGTH, Spells.KEY_PERMANENT_STRENGTH, Spells.KEY_PERMANENT_STRENGTH_DESC, MobEffects.DAMAGE_BOOST, 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_STRENGTH, Spells.KEY_TEMPORARY_STRENGTH, Spells.KEY_TEMPORARY_STRENGTH_DESC, MobEffects.DAMAGE_BOOST, 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_STRENGTH, Spells.KEY_TOGGLE_STRENGTH, Spells.KEY_TOGGLE_STRENGTH_DESC, MobEffects.DAMAGE_BOOST, 4F, 50, 0);
        
        addPermanentEffectSpell(Spells.PERMANENT_RESISTANCE, Spells.KEY_PERMANENT_RESISTANCE, Spells.KEY_PERMANENT_RESISTANCE_DESC, MobEffects.DAMAGE_RESISTANCE, 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_RESISTANCE, Spells.KEY_TEMPORARY_RESISTANCE, Spells.KEY_TEMPORARY_RESISTANCE_DESC, MobEffects.DAMAGE_RESISTANCE, 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_RESISTANCE, Spells.KEY_TOGGLE_RESISTANCE, Spells.KEY_TOGGLE_RESISTANCE_DESC, MobEffects.DAMAGE_RESISTANCE, 4F, 50, 0);
        
        addPermanentEffectSpell(Spells.PERMANENT_INVISIBILITY, Spells.KEY_PERMANENT_INVISIBILITY, Spells.KEY_PERMANENT_INVISIBILITY_DESC, MobEffects.INVISIBILITY, 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_INVISIBILITY, Spells.KEY_TEMPORARY_INVISIBILITY, Spells.KEY_TEMPORARY_INVISIBILITY_DESC, MobEffects.INVISIBILITY, 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_INVISIBILITY, Spells.KEY_TOGGLE_INVISIBILITY, Spells.KEY_TOGGLE_INVISIBILITY_DESC, MobEffects.INVISIBILITY, 4F, 50, 0);
        
        addPermanentEffectSpell(Spells.PERMANENT_GLOWING, Spells.KEY_PERMANENT_GLOWING, Spells.KEY_PERMANENT_GLOWING_DESC, MobEffects.GLOWING, 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_GLOWING, Spells.KEY_TEMPORARY_GLOWING, Spells.KEY_TEMPORARY_GLOWING_DESC, MobEffects.GLOWING, 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_GLOWING, Spells.KEY_TOGGLE_GLOWING, Spells.KEY_TOGGLE_GLOWING_DESC, MobEffects.GLOWING, 4F, 50, 0);
        
        addPermanentEffectSpell(Spells.PERMANENT_LUCK, Spells.KEY_PERMANENT_LUCK, Spells.KEY_PERMANENT_LUCK_DESC, MobEffects.LUCK, 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_LUCK, Spells.KEY_TEMPORARY_LUCK, Spells.KEY_TEMPORARY_LUCK_DESC, MobEffects.LUCK, 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_LUCK, Spells.KEY_TOGGLE_LUCK, Spells.KEY_TOGGLE_LUCK_DESC, MobEffects.LUCK, 4F, 50, 0);
        
        addPermanentEffectSpell(Spells.PERMANENT_CONDUIT_POWER, Spells.KEY_PERMANENT_CONDUIT_POWER, Spells.KEY_PERMANENT_CONDUIT_POWER_DESC, MobEffects.CONDUIT_POWER, 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_CONDUIT_POWER, Spells.KEY_TEMPORARY_CONDUIT_POWER, Spells.KEY_TEMPORARY_CONDUIT_POWER_DESC, MobEffects.CONDUIT_POWER, 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_CONDUIT_POWER, Spells.KEY_TOGGLE_CONDUIT_POWER, Spells.KEY_TOGGLE_CONDUIT_POWER_DESC, MobEffects.CONDUIT_POWER, 4F, 50, 0);
    }
    
    public String getName()
    {
        return "Spells & Shields Spells Files";
    }
    
    public static Component itemCostTitle(String key)
    {
        return Component.translatable(key).withStyle(ChatFormatting.BLUE);
    }
    
    public static Component itemCostComponent(Item item)
    {
        return itemCostComponent(item, 1);
    }
    
    public static Component itemCostComponent(Item item, int count)
    {
        if(count == 1)
        {
            return Component.translatable(KEY_ITEM_COST_SINGLE, Component.translatable(item.getDescriptionId())).withStyle(ChatFormatting.YELLOW);
        }
        else
        {
            return Component.translatable(KEY_ITEM_COST, count, Component.translatable(item.getDescriptionId())).withStyle(ChatFormatting.YELLOW);
        }
    }
    
    public static Component itemCostComponent(ItemStack itemStack)
    {
        if(itemStack.getCount() == 1)
        {
            return Component.translatable(KEY_ITEM_COST_SINGLE, itemStack.getHoverName()).withStyle(ChatFormatting.YELLOW);
        }
        else
        {
            return Component.translatable(KEY_ITEM_COST, itemStack.getCount(), itemStack.getHoverName()).withStyle(ChatFormatting.YELLOW);
        }
    }
    
    public static Component textItemCostComponent(Component translatable, int count)
    {
        return Component.translatable(KEY_ITEM_COST_TEXT, translatable).withStyle(ChatFormatting.YELLOW);
    }
    
    public static Map<String, String> eventHookMap(String... keyValuePairs)
    {
        assert keyValuePairs.length % 2 == 0;
        Map<String, String> map = new HashMap<>();
        for(int i = 0; i < keyValuePairs.length; i += 2)
        {
            map.put(keyValuePairs[0], keyValuePairs[1]);
        }
        return map;
    }
    
    // yes this is stupid but I do not know how to access these without a level
    // so I print this as code to console and then copy paste it into actual code
    public static void printBlastingRecipes(Level level, RegistryAccess access)
    {
        System.out.println("ABCDEFG=".repeat(50));
        level.getRecipeManager().getAllRecipesFor(RecipeType.BLASTING).forEach(r ->
        {
            String out = ForgeRegistries.ITEMS.getKey(r.getResultItem(access).getItem()).toString();
            r.getIngredients().forEach(i ->
            {
                Arrays.stream(i.getItems()).map(it -> ForgeRegistries.ITEMS.getKey(it.getItem()).toString()).forEach(item ->
                {
                    System.out.printf("blastRecipes.putString(\"%s\", \"%s\");\n", item, out);
                });
            });
        });
    }
    
    public static CompoundTag blastFurnaceRecipes()
    {
        CompoundTag blastRecipes = new CompoundTag();
        blastRecipes.putString("minecraft:golden_pickaxe", "minecraft:gold_nugget");
        blastRecipes.putString("minecraft:golden_shovel", "minecraft:gold_nugget");
        blastRecipes.putString("minecraft:golden_axe", "minecraft:gold_nugget");
        blastRecipes.putString("minecraft:golden_hoe", "minecraft:gold_nugget");
        blastRecipes.putString("minecraft:golden_sword", "minecraft:gold_nugget");
        blastRecipes.putString("minecraft:golden_helmet", "minecraft:gold_nugget");
        blastRecipes.putString("minecraft:golden_chestplate", "minecraft:gold_nugget");
        blastRecipes.putString("minecraft:golden_leggings", "minecraft:gold_nugget");
        blastRecipes.putString("minecraft:golden_boots", "minecraft:gold_nugget");
        blastRecipes.putString("minecraft:golden_horse_armor", "minecraft:gold_nugget");
        blastRecipes.putString("minecraft:deepslate_copper_ore", "minecraft:copper_ingot");
        blastRecipes.putString("minecraft:coal_ore", "minecraft:coal");
        blastRecipes.putString("minecraft:diamond_ore", "minecraft:diamond");
        blastRecipes.putString("minecraft:redstone_ore", "minecraft:redstone");
        blastRecipes.putString("minecraft:deepslate_lapis_ore", "minecraft:lapis_lazuli");
        blastRecipes.putString("minecraft:deepslate_diamond_ore", "minecraft:diamond");
        blastRecipes.putString("minecraft:deepslate_redstone_ore", "minecraft:redstone");
        blastRecipes.putString("minecraft:ancient_debris", "minecraft:netherite_scrap");
        blastRecipes.putString("minecraft:deepslate_iron_ore", "minecraft:iron_ingot");
        blastRecipes.putString("minecraft:copper_ore", "minecraft:copper_ingot");
        blastRecipes.putString("minecraft:deepslate_emerald_ore", "minecraft:emerald");
        blastRecipes.putString("minecraft:raw_gold", "minecraft:gold_ingot");
        blastRecipes.putString("minecraft:iron_pickaxe", "minecraft:iron_nugget");
        blastRecipes.putString("minecraft:iron_shovel", "minecraft:iron_nugget");
        blastRecipes.putString("minecraft:iron_axe", "minecraft:iron_nugget");
        blastRecipes.putString("minecraft:iron_hoe", "minecraft:iron_nugget");
        blastRecipes.putString("minecraft:iron_sword", "minecraft:iron_nugget");
        blastRecipes.putString("minecraft:iron_helmet", "minecraft:iron_nugget");
        blastRecipes.putString("minecraft:iron_chestplate", "minecraft:iron_nugget");
        blastRecipes.putString("minecraft:iron_leggings", "minecraft:iron_nugget");
        blastRecipes.putString("minecraft:iron_boots", "minecraft:iron_nugget");
        blastRecipes.putString("minecraft:iron_horse_armor", "minecraft:iron_nugget");
        blastRecipes.putString("minecraft:chainmail_helmet", "minecraft:iron_nugget");
        blastRecipes.putString("minecraft:chainmail_chestplate", "minecraft:iron_nugget");
        blastRecipes.putString("minecraft:chainmail_leggings", "minecraft:iron_nugget");
        blastRecipes.putString("minecraft:chainmail_boots", "minecraft:iron_nugget");
        blastRecipes.putString("minecraft:gold_ore", "minecraft:gold_ingot");
        blastRecipes.putString("minecraft:nether_quartz_ore", "minecraft:quartz");
        blastRecipes.putString("minecraft:iron_ore", "minecraft:iron_ingot");
        blastRecipes.putString("minecraft:nether_gold_ore", "minecraft:gold_ingot");
        blastRecipes.putString("minecraft:deepslate_gold_ore", "minecraft:gold_ingot");
        blastRecipes.putString("minecraft:emerald_ore", "minecraft:emerald");
        blastRecipes.putString("minecraft:raw_iron", "minecraft:iron_ingot");
        blastRecipes.putString("minecraft:lapis_ore", "minecraft:lapis_lazuli");
        blastRecipes.putString("minecraft:raw_copper", "minecraft:copper_ingot");
        blastRecipes.putString("minecraft:deepslate_coal_ore", "minecraft:coal");
        return blastRecipes;
    }
}
