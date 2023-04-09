package de.cas_ual_ty.spells.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.registers.BuiltinRegistries;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.action.ai.SetMobTargetAction;
import de.cas_ual_ty.spells.spell.action.attribute.*;
import de.cas_ual_ty.spells.spell.action.control.*;
import de.cas_ual_ty.spells.spell.action.delayed.AddDelayedSpellAction;
import de.cas_ual_ty.spells.spell.action.delayed.CheckHasDelayedSpellAction;
import de.cas_ual_ty.spells.spell.action.delayed.RemoveDelayedSpellAction;
import de.cas_ual_ty.spells.spell.action.effect.*;
import de.cas_ual_ty.spells.spell.action.fx.PlaySoundAction;
import de.cas_ual_ty.spells.spell.action.fx.SpawnParticlesAction;
import de.cas_ual_ty.spells.spell.action.item.*;
import de.cas_ual_ty.spells.spell.action.level.*;
import de.cas_ual_ty.spells.spell.action.mana.*;
import de.cas_ual_ty.spells.spell.action.target.*;
import de.cas_ual_ty.spells.spell.action.variable.PutVarAction;
import de.cas_ual_ty.spells.spell.compiler.Compiler;
import de.cas_ual_ty.spells.spell.icon.*;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.cas_ual_ty.spells.registers.CtxVarTypes.*;
import static de.cas_ual_ty.spells.spell.context.BuiltinActivations.*;
import static de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups.*;
import static de.cas_ual_ty.spells.spell.context.BuiltinVariables.*;

public class SpellsGen implements DataProvider
{
    public static final ResourceLocation PERMANENT_ICON_RL = new ResourceLocation(SpellsAndShields.MOD_ID, "textures/spell/permanent.png");
    public static final ResourceLocation TEMPORARY_ICON_RL = new ResourceLocation(SpellsAndShields.MOD_ID, "textures/spell/temporary.png");
    public static final ResourceLocation TOGGLE_ICON_RL = new ResourceLocation(SpellsAndShields.MOD_ID, "textures/spell/toggle.png");
    
    protected Map<ResourceLocation, Spell> spells;
    
    protected DataGenerator gen;
    protected String modId;
    protected ExistingFileHelper exFileHelper;
    protected RegistryAccess registryAccess;
    protected RegistryOps<JsonElement> registryOps;
    
    public SpellsGen(DataGenerator gen, String modId, ExistingFileHelper exFileHelper)
    {
        this.gen = gen;
        this.modId = modId;
        this.exFileHelper = exFileHelper;
        this.registryAccess = RegistryAccess.builtinCopy();
        this.registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        
        spells = new HashMap<>();
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
        spells.put(key, spell);
    }
    
    public void addPermanentEffectSpell(ResourceLocation rl, String key, String descKey, MobEffect mobEffect, int duration, int amplifier)
    {
        MutableComponent component = mobEffect.getDisplayName().copy();
        if(amplifier > 0)
        {
            component = Component.translatable("potion.withAmplifier", component, Component.translatable("potion.potency." + amplifier));
        }
        ResourceLocation mobEffectRL = ForgeRegistries.MOB_EFFECTS.getKey(mobEffect);
        String uuidCode = " uuid_from_string('permanent' + '%s' + %s) ".formatted(mobEffectRL.getPath(), SPELL_SLOT.name);
        Spell spell = new Spell(LayeredSpellIcon.make(List.of(DefaultSpellIcon.make(new ResourceLocation(mobEffectRL.getNamespace(), "textures/mob_effect/" + mobEffectRL.getPath() + ".png")), DefaultSpellIcon.make(PERMANENT_ICON_RL))), Component.translatable(key, component), 0F)
                .addAction(CopyTargetsAction.make(ON_EQUIP.activation, "player", OWNER.targetGroup))
                .addAction(CopyTargetsAction.make(ON_UNEQUIP.activation, "player", OWNER.targetGroup))
                .addAction(CopyTargetsAction.make("apply", "player", HOLDER.targetGroup))
                .addAction(PutVarAction.makeString(ON_EQUIP.activation, Compiler.compileString(uuidCode, STRING.get()), "uuid"))
                .addAction(PutVarAction.makeString(ON_UNEQUIP.activation, Compiler.compileString(uuidCode, STRING.get()), "uuid"))
                .addAction(PutVarAction.makeStringMoveVar("apply", DELAY_UUID.name, "uuid"))
                .addAction(ActivateAction.make(ON_EQUIP.activation, "apply"))
                .addAction(ActivateAction.make(ON_UNEQUIP.activation, "remove"))
                .addAction(RemoveDelayedSpellAction.make("remove", "player", STRING.get().reference("uuid"), BOOLEAN.get().immediate(false)))
                .addAction(ActivateAction.make("apply", "renew"))
                .addAction(ApplyMobEffectAction.make("apply", "player", STRING.get().reference("mob_effect"), INT.get().reference("duration+1"), INT.get().reference("amplifier"), BOOLEAN.get().reference("ambient"), BOOLEAN.get().reference("visible"), BOOLEAN.get().reference("show_icon")))
                .addAction(AddDelayedSpellAction.make("renew", "player", "apply", INT.get().reference("duration"), STRING.get().reference("uuid"), COMPOUND_TAG.get().immediate(new CompoundTag())))
                .addParameter(STRING.get(), "mob_effect", ForgeRegistries.MOB_EFFECTS.getKey(mobEffect).toString())
                .addParameter(INT.get(), "duration", duration)
                .addParameter(INT.get(), "amplifier", amplifier)
                .addParameter(BOOLEAN.get(), "ambient", false)
                .addParameter(BOOLEAN.get(), "visible", false)
                .addParameter(BOOLEAN.get(), "show_icon", true)
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
        String uuidCode = " uuid_from_string('toggle' + '%s' + %s) ".formatted(mobEffectRL.getPath(), SPELL_SLOT.name);
        Spell spell = new Spell(LayeredSpellIcon.make(List.of(DefaultSpellIcon.make(new ResourceLocation(mobEffectRL.getNamespace(), "textures/mob_effect/" + mobEffectRL.getPath() + ".png")), DefaultSpellIcon.make(TEMPORARY_ICON_RL))), Component.translatable(key, component), manaCost)
                .addAction(ManaCheckAction.make(ACTIVE.activation, OWNER.targetGroup, DOUBLE.get().reference(MANA_COST.name)))
                .addAction(ApplyMobEffectAction.make(ACTIVE.activation, OWNER.targetGroup, STRING.get().reference("mob_effect"), INT.get().reference("duration+1"), INT.get().reference("amplifier"), BOOLEAN.get().reference("ambient"), BOOLEAN.get().reference("visible"), BOOLEAN.get().reference("show_icon")))
                .addAction(PlaySoundAction.make(ACTIVE.activation, OWNER.targetGroup, SoundEvents.GENERIC_DRINK, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(PlaySoundAction.make(ACTIVE.activation, OWNER.targetGroup, SoundEvents.SPLASH_POTION_BREAK, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addParameter(STRING.get(), "mob_effect", ForgeRegistries.MOB_EFFECTS.getKey(mobEffect).toString())
                .addParameter(INT.get(), "duration", duration)
                .addParameter(INT.get(), "amplifier", amplifier)
                .addParameter(BOOLEAN.get(), "ambient", false)
                .addParameter(BOOLEAN.get(), "visible", false)
                .addParameter(BOOLEAN.get(), "show_icon", true)
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
        String uuidCode = " uuid_from_string('toggle' + '%s' + %s) ".formatted(mobEffectRL.getPath(), SPELL_SLOT.name);
        Spell spell = new Spell(LayeredSpellIcon.make(List.of(DefaultSpellIcon.make(new ResourceLocation(mobEffectRL.getNamespace(), "textures/mob_effect/" + mobEffectRL.getPath() + ".png")), DefaultSpellIcon.make(TOGGLE_ICON_RL))), Component.translatable(key, component), manaCost)
                .addAction(CopyTargetsAction.make(ACTIVE.activation, "player", OWNER.targetGroup))
                .addAction(CopyTargetsAction.make(ON_UNEQUIP.activation, "player", OWNER.targetGroup))
                .addAction(CopyTargetsAction.make("apply", "player", HOLDER.targetGroup))
                .addAction(PutVarAction.makeString(ACTIVE.activation, Compiler.compileString(uuidCode, STRING.get()), "uuid"))
                .addAction(PutVarAction.makeString(ON_UNEQUIP.activation, Compiler.compileString(uuidCode, STRING.get()), "uuid"))
                .addAction(PutVarAction.makeStringMoveVar("apply", DELAY_UUID.name, "uuid"))
                .addAction(ActivateAction.make(ACTIVE.activation, "apply"))
                .addAction(ActivateAction.make(ACTIVE.activation, "remove"))
                .addAction(ActivateAction.make(ON_UNEQUIP.activation, "remove"))
                .addAction(CheckHasDelayedSpellAction.make("remove", "player", STRING.get().reference("uuid")))
                .addAction(DeactivateAction.make("remove", "apply"))
                .addAction(RemoveDelayedSpellAction.make("remove", "player", STRING.get().reference("uuid"), BOOLEAN.get().immediate(false)))
                .addAction(PlaySoundAction.make("remove", "player", SoundEvents.SPLASH_POTION_BREAK, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(ManaCheckAction.make("apply", "player", Compiler.compileString(" (" + MANA_COST.name + " * duration) / 100 ", DOUBLE.get())))
                .addAction(ActivateAction.make("apply", "renew"))
                .addAction(ApplyMobEffectAction.make("apply", "player", STRING.get().reference("mob_effect"), INT.get().reference("duration+1"), INT.get().reference("amplifier"), BOOLEAN.get().reference("ambient"), BOOLEAN.get().reference("visible"), BOOLEAN.get().reference("show_icon")))
                .addAction(AddDelayedSpellAction.make("renew", "player", "apply", INT.get().reference("duration"), STRING.get().reference("uuid"), COMPOUND_TAG.get().immediate(new CompoundTag())))
                .addAction(ActivateAction.make("apply", "sound"))
                .addAction(ActivateAction.make("apply", "anti_sound"))
                .addAction(DeactivateAction.make(ACTIVE.activation, "anti_sound"))
                .addAction(DeactivateAction.make("anti_sound", "sound"))
                .addAction(PlaySoundAction.make("sound", "player", SoundEvents.GENERIC_DRINK, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addParameter(STRING.get(), "mob_effect", ForgeRegistries.MOB_EFFECTS.getKey(mobEffect).toString())
                .addParameter(INT.get(), "duration", duration)
                .addParameter(INT.get(), "amplifier", amplifier)
                .addParameter(BOOLEAN.get(), "ambient", false)
                .addParameter(BOOLEAN.get(), "visible", false)
                .addParameter(BOOLEAN.get(), "show_icon", true)
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
        
        String uuidCode = " uuid_from_string('attribute' + '%s' + %s + %s + %s) ".formatted(attributeRL.getPath(), SPELL_SLOT.name, "operation", "value");
        
        Spell spell = new Spell(LayeredSpellIcon.make(List.of(spellIcon, DefaultSpellIcon.make(PERMANENT_ICON_RL))), Component.translatable(key, component), 0F)
                .addAction(AddAttributeModifierAction.make(ON_EQUIP.activation, OWNER.targetGroup, SpellsUtil.objectToString(attribute, ForgeRegistries.ATTRIBUTES), Compiler.compileString(uuidCode, STRING.get()), STRING.get().immediate(attributeRL.getPath()), DOUBLE.get().immediate(value), STRING.get().immediate(opString)))
                .addAction(RemoveAttributeModifierAction.make(ON_UNEQUIP.activation, OWNER.targetGroup, SpellsUtil.objectToString(attribute, ForgeRegistries.ATTRIBUTES), Compiler.compileString(uuidCode, STRING.get())))
                .addParameter(DOUBLE.get(), "value", value)
                .addParameter(STRING.get(), "operation", opString)
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
    
    public void addToggleWalkerSpell(ResourceLocation rl, String key, String descKey, String icon, BlockState from, BlockState to, float manaCost, boolean tick)
    {
        ResourceLocation fromRL = ForgeRegistries.BLOCKS.getKey(from.getBlock());
        ResourceLocation toRL = ForgeRegistries.BLOCKS.getKey(to.getBlock());
        String uuidCode = " uuid_from_string('toggle_walker' + '%s' + %s) ".formatted(rl.toString(), SPELL_SLOT.name);
        Spell spell = new Spell(LayeredSpellIcon.make(List.of(DefaultSpellIcon.make(new ResourceLocation(modId, "textures/spell/" + icon + ".png")), DefaultSpellIcon.make(TOGGLE_ICON_RL))), key, manaCost)
                .addAction(CopyTargetsAction.make(ACTIVE.activation, "player", OWNER.targetGroup))
                .addAction(CopyTargetsAction.make(ON_UNEQUIP.activation, "player", OWNER.targetGroup))
                .addAction(CopyTargetsAction.make("apply", "player", HOLDER.targetGroup))
                .addAction(PutVarAction.makeString(ACTIVE.activation, Compiler.compileString(uuidCode, STRING.get()), "uuid"))
                .addAction(PutVarAction.makeString(ON_UNEQUIP.activation, Compiler.compileString(uuidCode, STRING.get()), "uuid"))
                .addAction(PutVarAction.makeStringMoveVar("apply", DELAY_UUID.name, "uuid"))
                .addAction(ActivateAction.make(ACTIVE.activation, "apply"))
                .addAction(ActivateAction.make(ACTIVE.activation, "remove"))
                .addAction(CheckHasDelayedSpellAction.make("remove", "player", STRING.get().reference("uuid")))
                .addAction(ActivateAction.make(ON_UNEQUIP.activation, "remove"))
                .addAction(RemoveDelayedSpellAction.make("remove", "player", STRING.get().reference("uuid"), BOOLEAN.get().immediate(false)))
                .addAction(DeactivateAction.make("remove", "apply"))
                .addAction(PlaySoundAction.make("remove", "player", SoundEvents.SPLASH_POTION_BREAK, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(ManaCheckAction.make("apply", "player", Compiler.compileString(" (" + MANA_COST.name + " * refresh_rate) / 100 ", DOUBLE.get())))
                .addAction(ActivateAction.make("apply", "renew"))
                
                .addAction(OffsetBlockAction.make("apply", "player", "above", VEC3.get().immediate(Vec3.ZERO)))
                .addAction(GetBlockAction.make("apply", "above", "", "", "is_air"))
                .addAction(BooleanActivationAction.make("apply", "apply", BOOLEAN.get().reference(" is_air "), BOOLEAN.get().immediate(false), BOOLEAN.get().immediate(true)))
                
                .addAction(OffsetBlockAction.make("apply", "player", "below", VEC3.get().immediate(new Vec3(0, -1, 0))))
                .addAction(CubeBlockTargetsAction.make("apply", "below", "blocks", Compiler.compileString(" vec3(-rect_radius, 0, -rect_radius) ", VEC3.get()), Compiler.compileString(" vec3(rect_radius, 0, rect_radius) ", VEC3.get())))
                
                .addAction(LabelAction.make("apply", "loop"))
                .addAction(ClearTargetsAction.make("apply", "block"))
                .addAction(PickTargetAction.make("apply", "block", "blocks", true, false))
                
                .addAction(GetBlockAction.make("apply", "block", "block_id", "", ""))
                .addAction(BooleanActivationAction.make("apply", "do_apply", BOOLEAN.get().reference(" block_id == '" + fromRL.toString() + "' "), BOOLEAN.get().immediate(true), BOOLEAN.get().immediate(true)))
                .addAction(SetBlockAction.make("do_apply", "block", STRING.get().reference("block_to"), COMPOUND_TAG.get().reference("block_state_to")));
        
        if(tick)
        {
            spell.addAction(TickBlockAction.make("apply", "block", Compiler.compileString(" next_int(60) + 60 ", INT.get())));
        }
        
        spell.addAction(GetTargetGroupSizeAction.make("apply", "blocks", "size"))
                .addAction(BranchAction.make("apply", "loop", Compiler.compileString(" size > 0 ", BOOLEAN.get())))
                .addAction(ActivateAction.make("apply", "sound"))
                .addAction(ActivateAction.make("apply", "anti_sound"))
                .addAction(DeactivateAction.make(ACTIVE.activation, "anti_sound"))
                .addAction(DeactivateAction.make("anti_sound", "sound"))
                .addAction(PlaySoundAction.make("sound", "player", SoundEvents.GENERIC_DRINK, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(AddDelayedSpellAction.make("renew", "player", "apply", INT.get().reference("refresh_rate"), STRING.get().reference("uuid"), COMPOUND_TAG.get().immediate(new CompoundTag())))
                .addParameter(INT.get(), "refresh_rate", 2)
                .addParameter(STRING.get(), "block_from", fromRL.toString())
                .addParameter(STRING.get(), "block_to", toRL.toString())
                .addParameter(COMPOUND_TAG.get(), "block_state_to", SpellsUtil.stateToTag(to))
                .addParameter(INT.get(), "rect_radius", 3)
                .addTooltip(Component.translatable(descKey));
        
        addSpell(rl, spell);
    }
    
    protected void addSpells()
    {
        dummy(Spells.DUMMY);
        
        addSpell(Spells.LEAP, new Spell(modId, "leap", Spells.KEY_LEAP, 5F)
                .addParameter(DOUBLE.get(), "speed", 2.5)
                .addAction(SimpleManaCheckAction.make(ACTIVE.activation))
                .addAction(ResetFallDistanceAction.make(ACTIVE.activation, OWNER.targetGroup))
                .addAction(GetEntityPositionDirectionMotionAction.make(ACTIVE.activation, OWNER.targetGroup, "", "look", ""))
                .addAction(PutVarAction.makeVec3(ACTIVE.activation, " (normalize(look + vec3(0, -get_y(look), 0))) * speed ", "direction"))
                .addAction(SetMotionAction.make(ACTIVE.activation, OWNER.targetGroup, Compiler.compileString(" vec3(get_x(direction), max(0.5, get_y(look) + 0.5), get_z(direction)) ", VEC3.get())))
                .addAction(SpawnParticlesAction.make(ACTIVE.activation, OWNER.targetGroup, ParticleTypes.POOF, INT.get().immediate(4), DOUBLE.get().immediate(0.1)))
                .addAction(PlaySoundAction.make(ACTIVE.activation, OWNER.targetGroup, SoundEvents.ENDER_DRAGON_FLAP, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addTooltip(Component.translatable(Spells.KEY_LEAP_DESC))
        );
        
        CompoundTag childTag = new CompoundTag();
        childTag.putInt("Age", -24000);
        addSpell(Spells.SUMMON_ANIMAL, new Spell(modId, "summon_animal", Spells.KEY_SUMMON_ANIMAL, 4F)
                .addAction(GetEntityPositionDirectionMotionAction.make(ACTIVE.activation, OWNER.targetGroup, "", "direction", ""))
                .addAction(SimpleManaCheckAction.make(ACTIVE.activation))
                .addAction(SpawnParticlesAction.make(ACTIVE.activation, OWNER.targetGroup, ParticleTypes.EXPLOSION, INT.get().immediate(3), DOUBLE.get().immediate(0.4)))
                .addAction(ActivateAction.make(ACTIVE.activation, "cow"))
                .addAction(ItemCheckAction.make("cow", OWNER.targetGroup, BOOLEAN.get().immediate(true), new ItemStack(Items.BEEF, 8)))
                .addAction(SpawnEntityAction.make("cow", "baby", SpellsUtil.objectToString(EntityType.COW, ForgeRegistries.ENTITY_TYPES), OWNER.targetGroup, Compiler.compileString(" -direction ", VEC3.get()), VEC3.get().immediate(Vec3.ZERO), COMPOUND_TAG.get().immediate(childTag)))
                .addAction(ActivateAction.make(ACTIVE.activation, "chicken"))
                .addAction(ItemCheckAction.make("chicken", OWNER.targetGroup, BOOLEAN.get().immediate(true), new ItemStack(Items.CHICKEN, 8)))
                .addAction(SpawnEntityAction.make("chicken", "baby", SpellsUtil.objectToString(EntityType.CHICKEN, ForgeRegistries.ENTITY_TYPES), OWNER.targetGroup, Compiler.compileString(" -direction ", VEC3.get()), VEC3.get().immediate(Vec3.ZERO), COMPOUND_TAG.get().immediate(childTag)))
                .addAction(ActivateAction.make(ACTIVE.activation, "pig"))
                .addAction(ItemCheckAction.make("pig", OWNER.targetGroup, BOOLEAN.get().immediate(true), new ItemStack(Items.PORKCHOP, 8)))
                .addAction(SpawnEntityAction.make("pig", "baby", SpellsUtil.objectToString(EntityType.PIG, ForgeRegistries.ENTITY_TYPES), OWNER.targetGroup, Compiler.compileString(" -direction ", VEC3.get()), VEC3.get().immediate(Vec3.ZERO), COMPOUND_TAG.get().immediate(childTag)))
                .addAction(ActivateAction.make(ACTIVE.activation, "sheep"))
                .addAction(ItemCheckAction.make("sheep", OWNER.targetGroup, BOOLEAN.get().immediate(true), new ItemStack(Items.MUTTON, 8)))
                .addAction(SpawnEntityAction.make("sheep", "baby", SpellsUtil.objectToString(EntityType.SHEEP, ForgeRegistries.ENTITY_TYPES), OWNER.targetGroup, Compiler.compileString(" -direction ", VEC3.get()), VEC3.get().immediate(Vec3.ZERO), COMPOUND_TAG.get().immediate(childTag)))
                .addTooltip(Component.translatable(Spells.KEY_SUMMON_ANIMAL_DESC))
        );
        
        addSpell(Spells.FIRE_BALL, new Spell(modId, "fire_ball", Spells.KEY_FIRE_BALL, 5F)
                .addParameter(DOUBLE.get(), "speed", 2.5)
                .addParameter(INT.get(), "fire_seconds", 2)
                .addAction(ItemCheckAction.make(ACTIVE.activation, OWNER.targetGroup, BOOLEAN.get().immediate(true), new ItemStack(Items.BLAZE_POWDER)))
                .addAction(SimpleManaCheckAction.make(ACTIVE.activation))
                .addAction(ShootAction.make(ACTIVE.activation, OWNER.targetGroup, DOUBLE.get().immediate(3D), DOUBLE.get().immediate(0D), INT.get().immediate(200), "on_block_hit", "on_entity_hit", "on_timeout", ""))
                .addAction(PlaySoundAction.make(ACTIVE.activation, OWNER.targetGroup, SoundEvents.BLAZE_SHOOT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(SourcedDamageAction.make("on_entity_hit", ENTITY_HIT.targetGroup, DOUBLE.get().immediate(2D), PROJECTILE.targetGroup))
                .addAction(SetOnFireAction.make("on_entity_hit", ENTITY_HIT.targetGroup, INT.get().reference("fire_seconds")))
                .addAction(ActivateAction.make("on_entity_hit", "fx"))
                .addAction(ActivateAction.make("on_block_hit", "fx"))
                .addAction(ActivateAction.make("on_timeout", "fx"))
                .addAction(PlaySoundAction.make("fx", HIT_POSITION.targetGroup, SoundEvents.BLAZE_SHOOT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(SpawnParticlesAction.make("fx", HIT_POSITION.targetGroup, ParticleTypes.LARGE_SMOKE, INT.get().immediate(3), DOUBLE.get().immediate(0.2)))
                .addAction(SpawnParticlesAction.make("fx", HIT_POSITION.targetGroup, ParticleTypes.LAVA, INT.get().immediate(1), DOUBLE.get().immediate(0.2)))
                .addAction(SpawnParticlesAction.make("fx", HIT_POSITION.targetGroup, ParticleTypes.SMOKE, INT.get().immediate(2), DOUBLE.get().immediate(0.1)))
                .addAction(SpawnParticlesAction.make("fx", HIT_POSITION.targetGroup, ParticleTypes.FLAME, INT.get().immediate(2), DOUBLE.get().immediate(0.1)))
                .addTooltip(Component.translatable(Spells.KEY_FIRE_BALL_DESC))
        );
        
        //TODO Blast Smelt
        dummy(Spells.BLAST_SMELT);
        
        addSpell(Spells.TRANSFER_MANA, new Spell(modId, "transfer_mana", Spells.KEY_TRANSFER_MANA, 4F)
                .addParameter(DOUBLE.get(), "speed", 2.5)
                .addParameter(DOUBLE.get(), "range", 25D)
                .addAction(LookAtTargetAction.make(ACTIVE.activation, OWNER.targetGroup, DOUBLE.get().reference("range"), 0.5F, ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, "looked_at_block", "looked_at_entity", "looked_at_nothing"))
                .addAction(SimpleManaCheckAction.make("looked_at_entity"))
                .addAction(HomeAction.make("looked_at_entity", OWNER.targetGroup, ENTITY_HIT.targetGroup, DOUBLE.get().immediate(3D), INT.get().immediate(200), "on_block_hit", "on_entity_hit", "on_timeout", ""))
                .addAction(PlaySoundAction.make("looked_at_entity", OWNER.targetGroup, SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(ReplenishManaAction.make("on_entity_hit", ENTITY_HIT.targetGroup, DOUBLE.get().reference(MANA_COST.name)))
                .addAction(ActivateAction.make("on_entity_hit", "fx"))
                .addAction(ActivateAction.make("on_block_hit", "fx"))
                .addAction(ActivateAction.make("on_timeout", "fx"))
                .addAction(PlaySoundAction.make("fx", HIT_POSITION.targetGroup, SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(SpawnParticlesAction.make("fx", HIT_POSITION.targetGroup, ParticleTypes.BUBBLE, INT.get().immediate(3), DOUBLE.get().immediate(0.2)))
                .addAction(SpawnParticlesAction.make("fx", HIT_POSITION.targetGroup, ParticleTypes.POOF, INT.get().immediate(2), DOUBLE.get().immediate(0.2)))
                .addTooltip(Component.translatable(Spells.KEY_TRANSFER_MANA_DESC))
        );
        
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("crit", true);
        tag.putInt("pickup", 1);
        addSpell(Spells.BLOW_ARROW, new Spell(modId, "blow_arrow", Spells.KEY_BLOW_ARROW, 5F)
                .addAction(SimpleManaCheckAction.make(ACTIVE.activation))
                .addAction(PutVarAction.makeCompoundTag(ACTIVE.activation, tag, "tag"))
                .addAction(GetEntityUUIDAction.make(ACTIVE.activation, OWNER.targetGroup, "uuid"))
                .addAction(PutVarAction.makeCompoundTag(ACTIVE.activation, Compiler.compileString(" put_nbt_uuid(tag, 'Owner', uuid) ", COMPOUND_TAG.get()), "tag"))
                .addAction(GetEntityPositionDirectionMotionAction.make(ACTIVE.activation, OWNER.targetGroup, "", "direction", ""))
                .addAction(GetEntityEyePositionAction.make(ACTIVE.activation, OWNER.targetGroup, "position"))
                .addAction(MainhandItemTargetAction.make(ACTIVE.activation, OWNER.targetGroup, "item"))
                .addAction(ActivateAction.make(ACTIVE.activation, "shoot"))
                .addAction(ActivateAction.make(ACTIVE.activation, "potion"))
                .addAction(ActivateAction.make(ACTIVE.activation, "spectral"))
                .addAction(ItemEqualsAction.make("shoot", "item", new ItemStack(Items.ARROW), BOOLEAN.get().immediate(true), INT.get().immediate(1), INT.get().immediate(-1)))
                .addAction(ItemEqualsAction.make("potion", "item", new ItemStack(Items.TIPPED_ARROW), BOOLEAN.get().immediate(true), INT.get().immediate(1), INT.get().immediate(-1)))
                .addAction(GetItemTagAction.make("potion", "item", "potion_tag"))
                .addAction(PutVarAction.makeCompoundTag("potion", Compiler.compileString(" put_nbt_string(tag, 'Potion', get_nbt_string(potion_tag, 'Potion')) ", COMPOUND_TAG.get()), "tag"))
                .addAction(ActivateAction.make("potion", "shoot"))
                .addAction(SpawnEntityAction.make("shoot", "arrow", SpellsUtil.objectToString(EntityType.ARROW, ForgeRegistries.ENTITY_TYPES), "position", VEC3.get().reference("direction"), Compiler.compileString(" 3 * direction ", VEC3.get()), COMPOUND_TAG.get().reference("tag")))
                .addAction(PlaySoundAction.make("shoot", OWNER.targetGroup, SoundEvents.ARROW_SHOOT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(ConsumeItemAction.make("shoot", "item", INT.get().immediate(1)))
                .addAction(ItemEqualsAction.make("spectral", "item", new ItemStack(Items.SPECTRAL_ARROW), BOOLEAN.get().immediate(true), INT.get().immediate(1), INT.get().immediate(-1)))
                .addAction(SpawnEntityAction.make("spectral", "arrow", SpellsUtil.objectToString(EntityType.SPECTRAL_ARROW, ForgeRegistries.ENTITY_TYPES), "position", VEC3.get().reference("direction"), Compiler.compileString(" 3 * direction ", VEC3.get()), COMPOUND_TAG.get().reference("tag")))
                .addAction(PlaySoundAction.make("spectral", OWNER.targetGroup, SoundEvents.ARROW_SHOOT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(ConsumeItemAction.make("spectral", "item", INT.get().immediate(1)))
                .addTooltip(Component.translatable(Spells.KEY_BLOW_ARROW_DESC))
        );
        
        addPermanentAttributeSpell(Spells.HEALTH_BOOST, Spells.KEY_HEALTH_BOOST, Spells.KEY_HEALTH_BOOST_DESC, DefaultSpellIcon.make(new ResourceLocation("textures/mob_effect/" + ForgeRegistries.MOB_EFFECTS.getKey(MobEffects.HEALTH_BOOST).getPath() + ".png")), Attributes.MAX_HEALTH, AttributeModifier.Operation.ADDITION, 4D);
        
        addPermanentAttributeSpell(Spells.MANA_BOOST, Spells.KEY_MANA_BOOST, Spells.KEY_MANA_BOOST_DESC, DefaultSpellIcon.make(new ResourceLocation(SpellsAndShields.MOD_ID, "textures/mob_effect/" + BuiltinRegistries.MANA_BOOST_EFFECT.getId().getPath() + ".png")), BuiltinRegistries.MAX_MANA_ATTRIBUTE.get(), AttributeModifier.Operation.ADDITION, 4D);
        
        addSpell(Spells.WATER_LEAP, new Spell(modId, "water_leap", Spells.KEY_WATER_LEAP, 5F)
                .addParameter(DOUBLE.get(), "speed", 2.5)
                .addAction(HasManaAction.make(ACTIVE.activation, OWNER.targetGroup, DOUBLE.get().reference(MANA_COST.name)))
                .addAction(GetEntityEyePositionAction.make(ACTIVE.activation, OWNER.targetGroup, "eye_pos"))
                .addAction(GetBlockAction.make(ACTIVE.activation, OWNER.targetGroup, "feet_block", "", ""))
                .addAction(GetBlockAction.make(ACTIVE.activation, "eye_pos", "eye_block", "", ""))
                .addAction(BooleanActivationAction.make(ACTIVE.activation, ACTIVE.activation, Compiler.compileString(" feet_block == '" + ForgeRegistries.BLOCKS.getKey(Blocks.WATER).toString() + "' && eye_block == '" + ForgeRegistries.BLOCKS.getKey(Blocks.WATER).toString() + "' ", BOOLEAN.get()), BOOLEAN.get().immediate(false), BOOLEAN.get().immediate(true)))
                .addAction(BurnManaAction.make(ACTIVE.activation, OWNER.targetGroup, DOUBLE.get().reference(MANA_COST.name)))
                .addAction(ResetFallDistanceAction.make(ACTIVE.activation, OWNER.targetGroup))
                .addAction(GetEntityPositionDirectionMotionAction.make(ACTIVE.activation, OWNER.targetGroup, "", "look", ""))
                .addAction(PutVarAction.makeVec3(ACTIVE.activation, " (normalize(look + vec3(0, -get_y(look), 0))) * speed ", "direction"))
                .addAction(SetMotionAction.make(ACTIVE.activation, OWNER.targetGroup, Compiler.compileString(" vec3(get_x(direction), max(0.5, get_y(look) + 0.5), get_z(direction)) ", VEC3.get())))
                .addAction(SpawnParticlesAction.make(ACTIVE.activation, OWNER.targetGroup, ParticleTypes.POOF, INT.get().immediate(4), DOUBLE.get().immediate(0.1)))
                .addAction(PlaySoundAction.make(ACTIVE.activation, OWNER.targetGroup, SoundEvents.ENDER_DRAGON_FLAP, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addTooltip(Component.translatable(Spells.KEY_WATER_LEAP_DESC))
        );
        
        dummy(Spells.AQUA_AFFINITY, Spells.KEY_AQUA_AFFINITY, Spells.KEY_AQUA_AFFINITY_DESC, ItemSpellIcon.make(new ItemStack(Items.ENCHANTED_BOOK)));
        
        //TODO fx, test
        addSpell(Spells.WATER_WHIP, new Spell(modId, "water_whip", Spells.KEY_WATER_WHIP, 5F)
                .addParameter(DOUBLE.get(), "damage", 10.0)
                .addAction(SimpleManaCheckAction.make(ACTIVE.activation))
                .addAction(MainhandItemTargetAction.make(ACTIVE.activation, OWNER.targetGroup, "item"))
                .addAction(ActivateAction.make(ACTIVE.activation, "shoot"))
                .addAction(ItemEqualsAction.make("shoot", "item", new ItemStack(Items.WATER_BUCKET), BOOLEAN.get().immediate(true), INT.get().immediate(1), INT.get().immediate(-1)))
                .addAction(ActivateAction.make(ACTIVE.activation, "offhand"))
                .addAction(DeactivateAction.make("shoot", "offhand"))
                .addAction(ClearTargetsAction.make("offhand", "item"))
                .addAction(OffhandItemTargetAction.make("offhand", OWNER.targetGroup, "item"))
                .addAction(ActivateAction.make("offhand", "shoot"))
                .addAction(ItemEqualsAction.make("shoot", "item", new ItemStack(Items.WATER_BUCKET), BOOLEAN.get().immediate(true), INT.get().immediate(1), INT.get().immediate(-1)))
                .addAction(GetItemAttributesAction.make("shoot", "item", "item", "amount", "damage", "item_tag"))
                .addAction(OverrideItemAction.make("shoot", "item", INT.get().reference("amount"), INT.get().reference("damage"), COMPOUND_TAG.get().reference("item_tag"), SpellsUtil.objectToString(Items.BUCKET, ForgeRegistries.ITEMS)))
                .addAction(GetEntityUUIDAction.make("shoot", OWNER.targetGroup, "owner_uuid_return"))
                .addAction(PutVarAction.makeCompoundTag("shoot", Compiler.compileString(" put_nbt_uuid(tag(), 'owner_uuid_return', owner_uuid_return) ", COMPOUND_TAG.get()), "tag"))
                .addAction(ShootAction.make("shoot", OWNER.targetGroup, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(0D), INT.get().immediate(100), "on_block_hit", "on_entity_hit", "on_timeout", "projectile"))
                .addAction(ApplyEntityExtraTagAction.make("shoot", "projectile", COMPOUND_TAG.get().reference("tag")))
                .addAction(PlaySoundAction.make("shoot", OWNER.targetGroup, SoundEvents.BUCKET_EMPTY, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(DamageAction.make("on_entity_hit", ENTITY_HIT.targetGroup, DOUBLE.get().immediate(10D)))
                .addAction(CopyTargetsAction.make("on_entity_hit", "position", HIT_POSITION.targetGroup))
                .addAction(ActivateAction.make("on_entity_hit", "return"))
                .addAction(CopyTargetsAction.make("on_block_hit", "position", HIT_POSITION.targetGroup))
                .addAction(ActivateAction.make("on_block_hit", "return"))
                .addAction(CopyTargetsAction.make("on_timeout", "position", PROJECTILE.targetGroup))
                .addAction(ActivateAction.make("on_timeout", "return"))
                .addAction(GetEntityExtraTagAction.make("return", PROJECTILE.targetGroup, "tag"))
                .addAction(EntityUUIDTargetAction.make("return", "return_target", Compiler.compileString(" get_nbt_uuid(tag, 'owner_uuid_return') ", STRING.get())))
                .addAction(HomeAction.make("return", "position", "return_target", DOUBLE.get().immediate(1D), INT.get().immediate(100), "dummy_block_hit", "on_entity_hit_return", "dummy_timeout", "projectile"))
                .addAction(ApplyEntityExtraTagAction.make("return", "projectile", COMPOUND_TAG.get().reference("tag")))
                .addAction(GetEntityTypeAction.make("on_entity_hit_return", ENTITY_HIT.targetGroup, "", "", "is_player"))
                .addAction(GetEntityUUIDAction.make("on_entity_hit_return", ENTITY_HIT.targetGroup, "hit_uuid"))
                .addAction(GetEntityExtraTagAction.make("on_entity_hit_return", PROJECTILE.targetGroup, "tag"))
                .addAction(BooleanActivationAction.make("on_entity_hit_return", "refill", Compiler.compileString(" is_player && (hit_uuid == get_nbt_uuid(tag, 'owner_uuid_return')) ", BOOLEAN.get()), BOOLEAN.get().immediate(true), BOOLEAN.get().immediate(false)))
                .addAction(MainhandItemTargetAction.make("refill", ENTITY_HIT.targetGroup, "item"))
                .addAction(ActivateAction.make("refill", "do_refill"))
                .addAction(ItemEqualsAction.make("do_refill", "item", new ItemStack(Items.BUCKET), BOOLEAN.get().immediate(true), INT.get().immediate(1), INT.get().immediate(-1)))
                .addAction(ActivateAction.make("refill", "refill_offhand"))
                .addAction(DeactivateAction.make("do_refill", "refill_offhand"))
                .addAction(ClearTargetsAction.make("refill_offhand", "item"))
                .addAction(OffhandItemTargetAction.make("refill_offhand", ENTITY_HIT.targetGroup, "item"))
                .addAction(ActivateAction.make("refill_offhand", "do_refill"))
                .addAction(ItemEqualsAction.make("do_refill", "item", new ItemStack(Items.BUCKET), BOOLEAN.get().immediate(true), INT.get().immediate(1), INT.get().immediate(-1)))
                .addAction(GetItemAttributesAction.make("do_refill", "item", "item", "amount", "damage", "item_tag"))
                .addAction(OverrideItemAction.make("do_refill", "item", INT.get().immediate(1), INT.get().reference("amount"), COMPOUND_TAG.get().reference("item_tag"), SpellsUtil.objectToString(Items.WATER_BUCKET, ForgeRegistries.ITEMS)))
                .addAction(GiveItemAction.make("do_refill", "item", Compiler.compileString(" amount - 1 ", INT.get()), INT.get().reference("amount"), COMPOUND_TAG.get().reference("item_tag"), SpellsUtil.objectToString(Items.BUCKET, ForgeRegistries.ITEMS)))
                .addAction(PlaySoundAction.make("do_refill", OWNER.targetGroup, SoundEvents.BUCKET_FILL, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addTooltip(Component.translatable(Spells.KEY_WATER_WHIP_DESC))
        );
        
        //TODO fx, test
        addSpell(Spells.POTION_SHOT, new Spell(modId, "potion_shot", Spells.KEY_POTION_SHOT, 5F)
                .addParameter(DOUBLE.get(), "damage", 10.0)
                .addAction(SimpleManaCheckAction.make(ACTIVE.activation))
                .addAction(MainhandItemTargetAction.make(ACTIVE.activation, OWNER.targetGroup, "item"))
                .addAction(ActivateAction.make(ACTIVE.activation, "shoot"))
                .addAction(ItemEqualsAction.make("shoot", "item", new ItemStack(Items.POTION), BOOLEAN.get().immediate(true), INT.get().immediate(1), INT.get().immediate(-1)))
                .addAction(ActivateAction.make(ACTIVE.activation, "offhand"))
                .addAction(DeactivateAction.make("shoot", "offhand"))
                .addAction(ClearTargetsAction.make("offhand", "item"))
                .addAction(OffhandItemTargetAction.make("offhand", OWNER.targetGroup, "item"))
                .addAction(ActivateAction.make("offhand", "shoot"))
                .addAction(ItemEqualsAction.make("shoot", "item", new ItemStack(Items.POTION), BOOLEAN.get().immediate(true), INT.get().immediate(1), INT.get().immediate(-1)))
                .addAction(GetItemAttributesAction.make("shoot", "item", "item", "amount", "damage", "item_tag"))
                .addAction(OverrideItemAction.make("shoot", "item", INT.get().reference("amount"), INT.get().reference("damage"), COMPOUND_TAG.get().reference("item_tag"), SpellsUtil.objectToString(Items.GLASS_BOTTLE, ForgeRegistries.ITEMS)))
                .addAction(PutVarAction.makeCompoundTag("shoot", Compiler.compileString(" put_nbt_string(tag(), 'Potion', get_nbt_string(item_tag, 'Potion')) ", COMPOUND_TAG.get()), "tag"))
                .addAction(ShootAction.make("shoot", OWNER.targetGroup, DOUBLE.get().immediate(2D), DOUBLE.get().immediate(0D), INT.get().immediate(100), "", "on_entity_hit", "", "projectile"))
                .addAction(ApplyEntityExtraTagAction.make("shoot", "projectile", COMPOUND_TAG.get().reference("tag")))
                .addAction(PlaySoundAction.make("shoot", OWNER.targetGroup, SoundEvents.BOTTLE_EMPTY, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(GetEntityExtraTagAction.make("on_entity_hit", PROJECTILE.targetGroup, "tag"))
                .addAction(ApplyPotionEffectAction.make("on_entity_hit", ENTITY_HIT.targetGroup, Compiler.compileString(" get_nbt_string(tag, 'Potion') ", STRING.get())))
                .addTooltip(Component.translatable(Spells.KEY_POTION_SHOT_DESC))
        );
        
        addToggleWalkerSpell(Spells.FROST_WALKER, Spells.KEY_FROST_WALKER, Spells.KEY_FROST_WALKER_DESC, "frost_walker", Blocks.WATER.defaultBlockState(), Blocks.FROSTED_ICE.defaultBlockState(), 5F, true);
        
        addSpell(Spells.JUMP, new Spell(modId, "jump", Spells.KEY_JUMP, 5F)
                .addParameter(DOUBLE.get(), "speed", 1.5)
                .addAction(SimpleManaCheckAction.make(ACTIVE.activation))
                .addAction(ResetFallDistanceAction.make(ACTIVE.activation, OWNER.targetGroup))
                .addAction(GetEntityPositionDirectionMotionAction.make(ACTIVE.activation, OWNER.targetGroup, "", "", "motion"))
                .addAction(SetMotionAction.make(ACTIVE.activation, OWNER.targetGroup, Compiler.compileString(" vec3(0, get_y(motion) + speed, 0) ", VEC3.get())))
                .addAction(SpawnParticlesAction.make(ACTIVE.activation, OWNER.targetGroup, ParticleTypes.POOF, INT.get().immediate(4), DOUBLE.get().immediate(0.1)))
                .addAction(PlaySoundAction.make(ACTIVE.activation, OWNER.targetGroup, SoundEvents.ENDER_DRAGON_FLAP, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addTooltip(Component.translatable(Spells.KEY_JUMP_DESC))
        );
        
        dummy(Spells.MANA_SOLES);
        
        addSpell(Spells.FIRE_CHARGE, new Spell(ItemSpellIcon.make(new ItemStack(Items.FIRE_CHARGE)), Spells.KEY_FIRE_CHARGE, 5F)
                .addAction(SimpleManaCheckAction.make(ACTIVE.activation))
                .addAction(ItemCheckAction.make(ACTIVE.activation, OWNER.targetGroup, BOOLEAN.get().immediate(true), new ItemStack(Items.FIRE_CHARGE)))
                .addAction(PutVarAction.makeCompoundTag(ACTIVE.activation, tag, "tag"))
                .addAction(GetEntityUUIDAction.make(ACTIVE.activation, OWNER.targetGroup, "uuid"))
                .addAction(GetEntityPositionDirectionMotionAction.make(ACTIVE.activation, OWNER.targetGroup, "", "direction", ""))
                .addAction(PutVarAction.makeCompoundTag(ACTIVE.activation, Compiler.compileString(" put_nbt_uuid(tag, 'Owner', uuid) ", COMPOUND_TAG.get()), "tag"))
                .addAction(PutVarAction.makeCompoundTag(ACTIVE.activation, Compiler.compileString(" put_nbt_vec3(tag, 'power', direction * 2.0 * 0.1) ", COMPOUND_TAG.get()), "tag"))
                .addAction(GetEntityEyePositionAction.make(ACTIVE.activation, OWNER.targetGroup, "position"))
                .addAction(SpawnEntityAction.make(ACTIVE.activation, "fire_charge", SpellsUtil.objectToString(EntityType.FIREBALL, ForgeRegistries.ENTITY_TYPES), "position", VEC3.get().reference("direction"), VEC3.get().immediate(Vec3.ZERO), COMPOUND_TAG.get().reference("tag")))
                .addAction(PlaySoundAction.make(ACTIVE.activation, OWNER.targetGroup, SoundEvents.BLAZE_SHOOT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addTooltip(Component.translatable(Spells.KEY_FIRE_CHARGE_DESC))
        );
        
        addSpell(Spells.PRESSURIZE, new Spell(modId, "pressurize", Spells.KEY_PRESSURIZE, 4F)
                .addAction(SimpleManaCheckAction.make(ACTIVE.activation))
                .addAction(RangedEntityTargetsAction.make(ACTIVE.activation, "targets", OWNER.targetGroup, DOUBLE.get().reference("range")))
                .addAction(SourcedKnockbackAction.make(ACTIVE.activation, "targets", DOUBLE.get().reference("knockback_strength"), OWNER.targetGroup))
                .addAction(PlaySoundAction.make(ACTIVE.activation, OWNER.targetGroup, SoundEvents.PLAYER_BREATH, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(SpawnParticlesAction.make(ACTIVE.activation, "targets", ParticleTypes.POOF, INT.get().immediate(3), DOUBLE.get().immediate(0.5D)))
                .addParameter(DOUBLE.get(), "range", 6D)
                .addParameter(DOUBLE.get(), "knockback_strength", 3D)
                .addTooltip(Component.translatable(Spells.KEY_PRESSURIZE_DESC))
        );
        
        dummy(Spells.INSTANT_MINE);
        
        //TODO fx
        CompoundTag metalMap = new CompoundTag();
        metalMap.putDouble(ForgeRegistries.ITEMS.getKey(Items.IRON_NUGGET).toString(), Tiers.IRON.getAttackDamageBonus());
        metalMap.putDouble(ForgeRegistries.ITEMS.getKey(Items.GOLD_NUGGET).toString(), Tiers.GOLD.getAttackDamageBonus());
        addSpell(Spells.SPIT_METAL, new Spell(modId, "spit_metal", Spells.KEY_SPIT_METAL, 4F)
                .addAction(SimpleManaCheckAction.make(ACTIVE.activation))
                .addAction(MainhandItemTargetAction.make(ACTIVE.activation, OWNER.targetGroup, "item"))
                .addAction(GetItemAttributesAction.make(ACTIVE.activation, "item", "item_id", "amount", "", ""))
                .addAction(ActivateAction.make(ACTIVE.activation, "offhand"))
                .addAction(BooleanActivationAction.make(ACTIVE.activation, "shoot", Compiler.compileString(" nbt_contains(item_damage_map, item_id) ", BOOLEAN.get()), BOOLEAN.get().immediate(true), BOOLEAN.get().immediate(false)))
                .addAction(DeactivateAction.make("shoot", "offhand"))
                .addAction(ClearTargetsAction.make("offhand", "item"))
                .addAction(OffhandItemTargetAction.make("offhand", OWNER.targetGroup, "item"))
                .addAction(GetItemAttributesAction.make("offhand", "item", "item_id", "amount", "", ""))
                .addAction(BooleanActivationAction.make("offhand", "shoot", Compiler.compileString(" nbt_contains(item_damage_map, item_id) ", BOOLEAN.get()), BOOLEAN.get().immediate(true), BOOLEAN.get().immediate(false)))
                .addAction(ShootAction.make("shoot", OWNER.targetGroup, DOUBLE.get().immediate(2D), DOUBLE.get().immediate(0D), INT.get().immediate(100), "", "on_entity_hit", "", "projectile"))
                .addAction(PutVarAction.makeDouble("shoot", Compiler.compileString(" base_damage + get_nbt_double(item_damage_map, item_id) ", DOUBLE.get()), "damage"))
                .addAction(ApplyEntityExtraTagAction.make("shoot", "projectile", Compiler.compileString(" put_nbt_double(tag(), 'damage', damage) ", COMPOUND_TAG.get())))
                .addAction(ConsumeItemAction.make("shoot", "item", INT.get().immediate(1)))
                .addAction(PlaySoundAction.make(ACTIVE.activation, OWNER.targetGroup, SoundEvents.LLAMA_SPIT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(GetEntityExtraTagAction.make("on_entity_hit", PROJECTILE.targetGroup, "damage_tag"))
                .addAction(SourcedDamageAction.make("on_entity_hit", ENTITY_HIT.targetGroup, Compiler.compileString(" get_nbt_double(damage_tag, 'damage') ", DOUBLE.get()), PROJECTILE.targetGroup))
                .addParameter(DOUBLE.get(), "base_damage", 8D)
                .addParameter(COMPOUND_TAG.get(), "item_damage_map", metalMap)
                .addTooltip(Component.translatable(Spells.KEY_SPIT_METAL_DESC))
        );
        
        // TODO fx
        addSpell(Spells.FLAMETHROWER, new Spell(modId, "flamethrower", Spells.KEY_FLAMETHROWER, 7F)
                .addAction(SimpleManaCheckAction.make(ACTIVE.activation))
                .addAction(ItemCheckAction.make(ACTIVE.activation, OWNER.targetGroup, BOOLEAN.get().immediate(true), new ItemStack(Items.BLAZE_POWDER)))
                .addAction(CopyTargetsAction.make(ACTIVE.activation, "player", OWNER.targetGroup))
                .addAction(ActivateAction.make(ACTIVE.activation, "shoot"))
                .addAction(CopyTargetsAction.make("on_timeout", "player", HOLDER.targetGroup))
                .addAction(PutVarAction.makeInt("on_timeout", Compiler.compileString(" get_nbt_int(" + DELAY_TAG.name + ", 'repetitions') ", INT.get()), "repetitions"))
                .addAction(ActivateAction.make("on_timeout", "shoot"))
                .addAction(LabelAction.make("shoot", "loop"))
                .addAction(BooleanActivationAction.make("shoot", "do_shoot", Compiler.compileString(" shots_per_repetition > 0 ", BOOLEAN.get()), BOOLEAN.get().immediate(true), BOOLEAN.get().immediate(true)))
                .addAction(ShootAction.make("do_shoot", "player", DOUBLE.get().immediate(2D), DOUBLE.get().reference("inaccuracy"), INT.get().immediate(20), "on_block_hit", "on_entity_hit", "", ""))
                .addAction(PutVarAction.makeInt("do_shoot", Compiler.compileString(" shots_per_repetition - 1 ", INT.get()), "shots_per_repetition"))
                .addAction(JumpAction.make("do_shoot", "loop"))
                .addAction(BooleanActivationAction.make("shoot", "repeat", Compiler.compileString(" repetitions > 1 ", BOOLEAN.get()), BOOLEAN.get().immediate(true), BOOLEAN.get().immediate(false)))
                .addAction(PlaySoundAction.make("shoot", "player", SoundEvents.BLAZE_SHOOT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(AddDelayedSpellAction.make("repeat", "player", "on_timeout", INT.get().reference("repetition_delay"), STRING.get().immediate(""), Compiler.compileString(" put_nbt_int(tag(), 'repetitions', repetitions - 1) ", COMPOUND_TAG.get())))
                .addAction(SetOnFireAction.make("on_entity_hit", ENTITY_HIT.targetGroup, INT.get().reference("fire_seconds")))
                .addParameter(INT.get(), "fire_seconds", 10)
                .addParameter(INT.get(), "shots_per_repetition", 3)
                .addParameter(INT.get(), "repetitions", 5)
                .addParameter(INT.get(), "repetition_delay", 4)
                .addParameter(DOUBLE.get(), "inaccuracy", 15D)
                .addTooltip(Component.translatable(Spells.KEY_FLAMETHROWER_DESC))
        );
        
        addToggleWalkerSpell(Spells.LAVA_WALKER, Spells.KEY_LAVA_WALKER, Spells.KEY_LAVA_WALKER_DESC, "lava_walker", Blocks.LAVA.defaultBlockState(), Blocks.OBSIDIAN.defaultBlockState(), 5F, true);
        
        addSpell(Spells.SILENCE_TARGET, new Spell(DefaultSpellIcon.make(new ResourceLocation(BuiltinRegistries.SILENCE_EFFECT.getId().getNamespace(), "textures/mob_effect/" + BuiltinRegistries.SILENCE_EFFECT.getId().getPath() + ".png")), Spells.KEY_SILENCE_TARGET, 5F)
                .addAction(SimpleManaCheckAction.make(ACTIVE.activation))
                .addAction(ItemCheckAction.make(ACTIVE.activation, OWNER.targetGroup, BOOLEAN.get().immediate(true), new ItemStack(Items.AMETHYST_SHARD)))
                .addAction(LookAtTargetAction.make(ACTIVE.activation, OWNER.targetGroup, DOUBLE.get().reference("range"), 0.5F, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, "", "on_entity_hit", ""))
                .addAction(ApplyMobEffectAction.make("on_entity_hit", ENTITY_HIT.targetGroup, STRING.get().immediate(BuiltinRegistries.SILENCE_EFFECT.getId().toString()), INT.get().reference("silence_seconds"), INT.get().immediate(0), BOOLEAN.get().immediate(false), BOOLEAN.get().immediate(true), BOOLEAN.get().immediate(true)))
                .addAction(PlaySoundAction.make("on_entity_hit", OWNER.targetGroup, SoundEvents.AMETHYST_CLUSTER_HIT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(PlaySoundAction.make("on_entity_hit", ENTITY_HIT.targetGroup, SoundEvents.AMETHYST_CLUSTER_BREAK, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(SpawnParticlesAction.make("on_entity_hit", HIT_POSITION.targetGroup, ParticleTypes.POOF, INT.get().immediate(3), DOUBLE.get().immediate(0.2)))
                .addParameter(DOUBLE.get(), "range", 20D)
                .addParameter(INT.get(), "silence_seconds", 15)
        );
        
        addSpell(Spells.RANDOM_TELEPORT, new Spell(modId, "random_teleport", Spells.KEY_RANDOM_TELEPORT, 5F)
                .addAction(HasManaAction.make(ACTIVE.activation, OWNER.targetGroup, DOUBLE.get().reference(MANA_COST.name)))
                .addAction(PlayerHasItemsAction.make(ACTIVE.activation, OWNER.targetGroup, SpellsUtil.objectToString(Items.CHORUS_FRUIT, ForgeRegistries.ITEMS), INT.get().immediate(1), COMPOUND_TAG.get().immediate(new CompoundTag()), BOOLEAN.get().immediate(true)))
                
                .addAction(PutVarAction.makeInt(ACTIVE.activation, INT.get().reference("max_attempts"), "attempts"))
                .addAction(LabelAction.make(ACTIVE.activation, "loop"))
                
                .addAction(ClearTargetsAction.make(ACTIVE.activation, "below"))
                .addAction(ClearTargetsAction.make(ACTIVE.activation, "feet"))
                .addAction(ClearTargetsAction.make(ACTIVE.activation, "head"))
                .addAction(PutVarAction.makeInt(ACTIVE.activation, INT.get().reference("max_inner_attempts"), "inner_attempts"))
                .addAction(PutVarAction.makeDouble(ACTIVE.activation, Compiler.compileString(" random_double() * 2 * range - range ", DOUBLE.get()), "x"))
                .addAction(PutVarAction.makeDouble(ACTIVE.activation, Compiler.compileString(" min(" + MAX_BLOCK_HEIGHT.name + ", max(" + MIN_BLOCK_HEIGHT.name + ", random_double() * 2 * range - range)) ", DOUBLE.get()), "y"))
                .addAction(PutVarAction.makeDouble(ACTIVE.activation, Compiler.compileString(" random_double() * 2 * range - range ", DOUBLE.get()), "z"))
                .addAction(OffsetBlockAction.make(ACTIVE.activation, OWNER.targetGroup, "below", Compiler.compileString(" vec3(x, y, z) ", VEC3.get())))
                .addAction(OffsetBlockAction.make(ACTIVE.activation, "below", "feet", VEC3.get().immediate(new Vec3(0, 1, 0))))
                .addAction(OffsetBlockAction.make(ACTIVE.activation, "feet", "head", VEC3.get().immediate(new Vec3(0, 1, 0))))
                .addAction(LabelAction.make(ACTIVE.activation, "inner_loop"))
                
                .addAction(GetBlockAction.make(ACTIVE.activation, "below", "", "", "below_is_air"))
                .addAction(GetBlockAction.make(ACTIVE.activation, "feet", "", "", "feet_is_air"))
                .addAction(GetBlockAction.make(ACTIVE.activation, "head", "", "", "head_is_air"))
                .addAction(BooleanActivationAction.make(ACTIVE.activation, "success", Compiler.compileString(" !below_is_air && feet_is_air && head_is_air ", BOOLEAN.get()), BOOLEAN.get().immediate(true), BOOLEAN.get().immediate(true)))
                
                .addAction(DeactivateAction.make("success", ACTIVE.activation))
                .addAction(ClearTargetsAction.make(ACTIVE.activation, "below"))
                .addAction(PickTargetAction.make(ACTIVE.activation, "below", "feet", true, false))
                .addAction(PickTargetAction.make(ACTIVE.activation, "feet", "head", true, false))
                .addAction(OffsetBlockAction.make(ACTIVE.activation, "feet", "head", VEC3.get().immediate(new Vec3(0, 1, 0))))
                .addAction(PutVarAction.makeInt(ACTIVE.activation, Compiler.compileString(" inner_attempts - 1 ", INT.get()), "inner_attempts"))
                .addAction(BranchAction.make(ACTIVE.activation, "inner_loop", Compiler.compileString(" inner_attempts > 0 ", BOOLEAN.get())))
                
                .addAction(PutVarAction.makeInt(ACTIVE.activation, Compiler.compileString(" attempts - 1 ", INT.get()), "attempts"))
                .addAction(BranchAction.make(ACTIVE.activation, "loop", Compiler.compileString(" attempts > 0 ", BOOLEAN.get())))
                .addAction(BurnManaAction.make("success", OWNER.targetGroup, DOUBLE.get().reference(MANA_COST.name)))
                .addAction(ConsumePlayerItemsAction.make("success", OWNER.targetGroup, SpellsUtil.objectToString(Items.CHORUS_FRUIT, ForgeRegistries.ITEMS), INT.get().immediate(1), COMPOUND_TAG.get().immediate(new CompoundTag()), BOOLEAN.get().immediate(true)))
                .addAction(GetPositionAction.make("success", "feet", "feet_pos"))
                .addAction(PutVarAction.makeDouble("success", Compiler.compileString(" get_y(feet_pos) - floor(get_y(feet_pos))", DOUBLE.get()), "feet_pos_floor"))
                .addAction(OffsetBlockAction.make("success", "feet", "teleport_position", Compiler.compileString("vec3(0, -feet_pos_floor, 0)", VEC3.get())))
                .addAction(PlaySoundAction.make("success", OWNER.targetGroup, SoundEvents.ENDERMAN_TELEPORT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(TeleportToAction.make("success", OWNER.targetGroup, "teleport_position"))
                .addAction(PlaySoundAction.make("success", OWNER.targetGroup, SoundEvents.ENDERMAN_TELEPORT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(PlaySoundAction.make(ACTIVE.activation, OWNER.targetGroup, SoundEvents.ENDERMAN_SCREAM, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                
                .addParameter(INT.get(), "max_attempts", 10)
                .addParameter(INT.get(), "max_inner_attempts", 10)
                .addParameter(DOUBLE.get(), "range", 32D)
                .addTooltip(Component.translatable(Spells.KEY_RANDOM_TELEPORT_DESC))
        );
        
        addSpell(Spells.FORCED_TELEPORT, new Spell(modId, "forced_teleport", Spells.KEY_FORCED_TELEPORT, 10F)
                .addAction(HasManaAction.make(ACTIVE.activation, OWNER.targetGroup, DOUBLE.get().reference(MANA_COST.name)))
                .addAction(PlayerHasItemsAction.make(ACTIVE.activation, OWNER.targetGroup, SpellsUtil.objectToString(Items.CHORUS_FRUIT, ForgeRegistries.ITEMS), INT.get().immediate(1), COMPOUND_TAG.get().immediate(new CompoundTag()), BOOLEAN.get().immediate(true)))
                .addAction(LookAtTargetAction.make(ACTIVE.activation, OWNER.targetGroup, DOUBLE.get().reference("target_range"), 0.5F, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, "fail", "on_entity_hit", "fail"))
                .addAction(CopyTargetsAction.make("on_entity_hit", "player", ENTITY_HIT.targetGroup))
                .addAction(DeactivateAction.make("fail", ACTIVE.activation))
                .addAction(PutVarAction.makeInt(ACTIVE.activation, INT.get().reference("max_attempts"), "attempts"))
                .addAction(LabelAction.make(ACTIVE.activation, "loop"))
                
                .addAction(ClearTargetsAction.make(ACTIVE.activation, "below"))
                .addAction(ClearTargetsAction.make(ACTIVE.activation, "feet"))
                .addAction(ClearTargetsAction.make(ACTIVE.activation, "head"))
                .addAction(PutVarAction.makeInt(ACTIVE.activation, INT.get().reference("max_inner_attempts"), "inner_attempts"))
                .addAction(PutVarAction.makeDouble(ACTIVE.activation, Compiler.compileString(" random_double() * 2 * teleport_range - teleport_range ", DOUBLE.get()), "x"))
                .addAction(PutVarAction.makeDouble(ACTIVE.activation, Compiler.compileString(" min(" + MAX_BLOCK_HEIGHT.name + ", max(" + MIN_BLOCK_HEIGHT.name + ", random_double() * 2 * teleport_range - teleport_range)) ", DOUBLE.get()), "y"))
                .addAction(PutVarAction.makeDouble(ACTIVE.activation, Compiler.compileString(" random_double() * 2 * teleport_range - teleport_range ", DOUBLE.get()), "z"))
                .addAction(OffsetBlockAction.make(ACTIVE.activation, "player", "below", Compiler.compileString(" vec3(x, y, z) ", VEC3.get())))
                .addAction(OffsetBlockAction.make(ACTIVE.activation, "below", "feet", VEC3.get().immediate(new Vec3(0, 1, 0))))
                .addAction(OffsetBlockAction.make(ACTIVE.activation, "feet", "head", VEC3.get().immediate(new Vec3(0, 1, 0))))
                .addAction(LabelAction.make(ACTIVE.activation, "inner_loop"))
                
                .addAction(GetBlockAction.make(ACTIVE.activation, "below", "", "", "below_is_air"))
                .addAction(GetBlockAction.make(ACTIVE.activation, "feet", "", "", "feet_is_air"))
                .addAction(GetBlockAction.make(ACTIVE.activation, "head", "", "", "head_is_air"))
                .addAction(BooleanActivationAction.make(ACTIVE.activation, "success", Compiler.compileString(" !below_is_air && feet_is_air && head_is_air ", BOOLEAN.get()), BOOLEAN.get().immediate(true), BOOLEAN.get().immediate(true)))
                
                .addAction(DeactivateAction.make("success", ACTIVE.activation))
                .addAction(ClearTargetsAction.make(ACTIVE.activation, "below"))
                .addAction(PickTargetAction.make(ACTIVE.activation, "below", "feet", true, false))
                .addAction(PickTargetAction.make(ACTIVE.activation, "feet", "head", true, false))
                .addAction(OffsetBlockAction.make(ACTIVE.activation, "feet", "head", VEC3.get().immediate(new Vec3(0, 1, 0))))
                .addAction(PutVarAction.makeInt(ACTIVE.activation, Compiler.compileString(" inner_attempts - 1 ", INT.get()), "inner_attempts"))
                .addAction(BranchAction.make(ACTIVE.activation, "inner_loop", Compiler.compileString(" inner_attempts > 0 ", BOOLEAN.get())))
                
                .addAction(PutVarAction.makeInt(ACTIVE.activation, Compiler.compileString(" attempts - 1 ", INT.get()), "attempts"))
                .addAction(BranchAction.make(ACTIVE.activation, "loop", Compiler.compileString(" attempts > 0 ", BOOLEAN.get())))
                .addAction(BurnManaAction.make("success", OWNER.targetGroup, DOUBLE.get().reference(MANA_COST.name)))
                .addAction(ConsumePlayerItemsAction.make("success", OWNER.targetGroup, SpellsUtil.objectToString(Items.CHORUS_FRUIT, ForgeRegistries.ITEMS), INT.get().immediate(1), COMPOUND_TAG.get().immediate(new CompoundTag()), BOOLEAN.get().immediate(true)))
                .addAction(GetPositionAction.make("success", "feet", "feet_pos"))
                .addAction(PutVarAction.makeDouble("success", Compiler.compileString(" get_y(feet_pos) - floor(get_y(feet_pos))", DOUBLE.get()), "feet_pos_floor"))
                .addAction(OffsetBlockAction.make("success", "feet", "teleport_position", Compiler.compileString("vec3(0, -feet_pos_floor, 0)", VEC3.get())))
                .addAction(PlaySoundAction.make("success", "player", SoundEvents.ENDERMAN_TELEPORT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(TeleportToAction.make("success", "player", "teleport_position"))
                .addAction(PlaySoundAction.make("success", "player", SoundEvents.ENDERMAN_TELEPORT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(PlaySoundAction.make("fail", OWNER.targetGroup, SoundEvents.ENDERMAN_SCREAM, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                
                .addParameter(INT.get(), "max_attempts", 10)
                .addParameter(INT.get(), "max_inner_attempts", 10)
                .addParameter(DOUBLE.get(), "teleport_range", 32D)
                .addParameter(DOUBLE.get(), "target_range", 32D)
                .addTooltip(Component.translatable(Spells.KEY_FORCED_TELEPORT_DESC))
        );
        
        addSpell(Spells.TELEPORT, new Spell(modId, "teleport", Spells.KEY_TELEPORT, 10F)
                .addAction(PlayerHasItemsAction.make(ACTIVE.activation, OWNER.targetGroup, SpellsUtil.objectToString(Items.CHORUS_FRUIT, ForgeRegistries.ITEMS), INT.get().immediate(1), COMPOUND_TAG.get().immediate(new CompoundTag()), BOOLEAN.get().immediate(true)))
                .addAction(ManaCheckAction.make(ACTIVE.activation, OWNER.targetGroup, DOUBLE.get().reference(MANA_COST.name)))
                .addAction(LookAtTargetAction.make(ACTIVE.activation, OWNER.targetGroup, DOUBLE.get().reference("range"), 0.5F, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, "on_block_hit", "on_entity_hit", "on_miss"))
                .addAction(CopyTargetsAction.make("on_entity_hit", "teleport_position", ENTITY_HIT.targetGroup))
                .addAction(ActivateAction.make("on_entity_hit", "teleport"))
                .addAction(OffsetBlockAction.make("on_block_hit", BLOCK_HIT.targetGroup, "teleport_position", VEC3.get().immediate(new Vec3(0, 0.5, 0))))
                .addAction(ActivateAction.make("on_block_hit", "teleport"))
                .addAction(CopyTargetsAction.make("on_miss", "teleport_position", HIT_POSITION.targetGroup))
                .addAction(ActivateAction.make("on_miss", "teleport"))
                .addAction(ConsumePlayerItemsAction.make("teleport", OWNER.targetGroup, SpellsUtil.objectToString(Items.CHORUS_FRUIT, ForgeRegistries.ITEMS), INT.get().immediate(1), COMPOUND_TAG.get().immediate(new CompoundTag()), BOOLEAN.get().immediate(true)))
                .addAction(PlaySoundAction.make("teleport", OWNER.targetGroup, SoundEvents.ENDERMAN_TELEPORT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(TeleportToAction.make("teleport", OWNER.targetGroup, "teleport_position"))
                .addAction(PlaySoundAction.make("teleport", OWNER.targetGroup, SoundEvents.ENDERMAN_TELEPORT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addParameter(DOUBLE.get(), "range", 32D)
                .addTooltip(Component.translatable(Spells.KEY_TELEPORT_DESC))
        );
        
        addSpell(Spells.LIGHTNING_STRIKE, new Spell(modId, "lightning_strike", Spells.KEY_LIGHTNING_STRIKE, 8F)
                .addParameter(DOUBLE.get(), "range", 20D)
                .addAction(SimpleManaCheckAction.make(ACTIVE.activation))
                .addAction(ItemCheckAction.make(ACTIVE.activation, OWNER.targetGroup, BOOLEAN.get().immediate(true), new ItemStack(Items.COPPER_INGOT)))
                .addAction(LookAtTargetAction.make(ACTIVE.activation, OWNER.targetGroup, DOUBLE.get().reference("range"), 0.5F, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, "on_block_hit", "on_entity_hit", ""))
                .addAction(CopyTargetsAction.make("on_block_hit", "position", BLOCK_HIT.targetGroup))
                .addAction(CopyTargetsAction.make("on_entity_hit", "position", ENTITY_HIT.targetGroup))
                .addAction(ActivateAction.make("on_block_hit", "on_hit"))
                .addAction(ActivateAction.make("on_entity_hit", "on_hit"))
                .addAction(SpawnEntityAction.make("on_hit", "", SpellsUtil.objectToString(EntityType.LIGHTNING_BOLT, ForgeRegistries.ENTITY_TYPES), "position", VEC3.get().immediate(Vec3.ZERO), VEC3.get().immediate(Vec3.ZERO), COMPOUND_TAG.get().immediate(new CompoundTag())))
                .addTooltip(Component.translatable(Spells.KEY_LIGHTNING_STRIKE_DESC))
        );
        
        addSpell(Spells.DRAIN_FLAME, new Spell(modId, "drain_flame", Spells.KEY_DRAIN_FLAME, 0F)
                .addAction(LookAtTargetAction.make(ACTIVE.activation, OWNER.targetGroup, DOUBLE.get().reference("range"), 0F, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, "on_block_hit", "", ""))
                .addAction(CubeBlockTargetsAction.make("on_block_hit", BLOCK_HIT.targetGroup, "blocks", Compiler.compileString(" vec3(-radius, -radius, -radius) ", VEC3.get()), Compiler.compileString(" vec3(radius, radius, radius) ", VEC3.get())))
                .addAction(LabelAction.make("on_block_hit", "loop"))
                .addAction(ClearTargetsAction.make("on_block_hit", "block_to_check"))
                .addAction(PickTargetAction.make("on_block_hit", "block_to_check", "blocks", true, true))
                .addAction(GetBlockAction.make("on_block_hit", "block_to_check", "block_type", "", ""))
                .addAction(BooleanActivationAction.make("on_block_hit", "success", Compiler.compileString(" block_type == '" + ForgeRegistries.BLOCKS.getKey(Blocks.FIRE).toString() + "' || block_type == '" + ForgeRegistries.BLOCKS.getKey(Blocks.SOUL_FIRE) + "' ", BOOLEAN.get()), BOOLEAN.get().immediate(true), BOOLEAN.get().immediate(false)))
                .addAction(DeactivateAction.make("success", "on_block_hit"))
                .addAction(JumpAction.make("on_block_hit", "loop"))
                .addAction(RemoveBlockAction.make("success", "block_to_check"))
                .addAction(PlaySoundAction.make("success", OWNER.targetGroup, SoundEvents.FIRE_EXTINGUISH, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(PlaySoundAction.make("success", "block_to_check", SoundEvents.FIRE_EXTINGUISH, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(HomeAction.make("success", "block_to_check", OWNER.targetGroup, DOUBLE.get().immediate(1D), INT.get().immediate(100), "", "owner_hit", "", ""))
                .addAction(ApplyMobEffectAction.make("owner_hit", ENTITY_HIT.targetGroup, SpellsUtil.objectToString(BuiltinRegistries.REPLENISHMENT_EFFECT.get(), ForgeRegistries.MOB_EFFECTS), INT.get().reference("replenishment_duration"), INT.get().immediate(0), BOOLEAN.get().immediate(false), BOOLEAN.get().immediate(true), BOOLEAN.get().immediate(true)))
                .addAction(PlaySoundAction.make("owner_hit", ENTITY_HIT.targetGroup, SoundEvents.FIRE_AMBIENT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addParameter(DOUBLE.get(), "range", 50D)
                .addParameter(INT.get(), "replenishment_duration", 100)
                .addParameter(INT.get(), "radius", 1)
                .addTooltip(Component.translatable(Spells.KEY_DRAIN_FLAME_DESC))
        );
        
        addSpell(Spells.GROWTH, new Spell(modId, "growth", Spells.KEY_GROWTH, 4F)
                .addAction(HasManaAction.make(ACTIVE.activation, OWNER.targetGroup, DOUBLE.get().reference(MANA_COST.name)))
                .addAction(PlayerHasItemsAction.make(ACTIVE.activation, OWNER.targetGroup, SpellsUtil.objectToString(Items.BONE_MEAL, ForgeRegistries.ITEMS), INT.get().immediate(1), COMPOUND_TAG.get().immediate(new CompoundTag()), BOOLEAN.get().immediate(true)))
                .addAction(BurnManaAction.make(ACTIVE.activation, OWNER.targetGroup, DOUBLE.get().reference(MANA_COST.name)))
                .addAction(ConsumePlayerItemsAction.make(ACTIVE.activation, OWNER.targetGroup, SpellsUtil.objectToString(Items.BONE_MEAL, ForgeRegistries.ITEMS), INT.get().immediate(1), COMPOUND_TAG.get().immediate(new CompoundTag()), BOOLEAN.get().immediate(true)))
                .addAction(CubeBlockTargetsAction.make(ACTIVE.activation, OWNER.targetGroup, "blocks", Compiler.compileString(" vec3(-range, -1, -range) ", VEC3.get()), Compiler.compileString(" vec3(range, 1, range) ", VEC3.get())))
                .addAction(UseItemOnBlocksAction.make(ACTIVE.activation, "blocks", OWNER.targetGroup, new ItemStack(Items.BONE_MEAL), false, Direction.UP))
                .addAction(SpawnParticlesAction.make(ACTIVE.activation, "blocks", ParticleTypes.POOF, INT.get().immediate(1), DOUBLE.get().immediate(0.25D)))
                .addAction(PlaySoundAction.make(ACTIVE.activation, OWNER.targetGroup, SoundEvents.BONE_MEAL_USE, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addParameter(INT.get(), "range", 3)
                .addParameter(INT.get(), "duration", 20)
                .addTooltip(Component.translatable(Spells.KEY_GROWTH_DESC))
        );
        
        addSpell(Spells.GHAST, new Spell(AdvancedSpellIcon.make(new ResourceLocation("textures/entity/ghast/ghast_shooting.png"), 16, 16, 16, 16, 64, 32), Spells.KEY_GHAST, 4F)
                .addAction(SimpleManaCheckAction.make(ACTIVE.activation))
                .addAction(ItemCheckAction.make(ACTIVE.activation, OWNER.targetGroup, BOOLEAN.get().immediate(true), new ItemStack(Items.FIRE_CHARGE)))
                .addAction(AddDelayedSpellAction.make(ACTIVE.activation, OWNER.targetGroup, "sound", INT.get().immediate(10), STRING.get().immediate(""), COMPOUND_TAG.get().immediate(new CompoundTag())))
                .addAction(AddDelayedSpellAction.make(ACTIVE.activation, OWNER.targetGroup, "shoot", INT.get().immediate(20), STRING.get().immediate(""), COMPOUND_TAG.get().immediate(new CompoundTag())))
                .addAction(PlaySoundAction.make("sound", HOLDER.targetGroup, SoundEvents.GHAST_WARN, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(PutVarAction.makeCompoundTag("shoot", tag, "tag"))
                .addAction(GetEntityUUIDAction.make("shoot", HOLDER.targetGroup, "uuid"))
                .addAction(GetEntityPositionDirectionMotionAction.make("shoot", HOLDER.targetGroup, "", "direction", ""))
                .addAction(PutVarAction.makeCompoundTag("shoot", Compiler.compileString(" put_nbt_uuid(tag, 'Owner', uuid) ", COMPOUND_TAG.get()), "tag"))
                .addAction(PutVarAction.makeCompoundTag("shoot", Compiler.compileString(" put_nbt_vec3(tag, 'power', direction * 2.0 * 0.1) ", COMPOUND_TAG.get()), "tag"))
                .addAction(GetEntityEyePositionAction.make("shoot", HOLDER.targetGroup, "position"))
                .addAction(SpawnEntityAction.make("shoot", "fire_charge", SpellsUtil.objectToString(EntityType.FIREBALL, ForgeRegistries.ENTITY_TYPES), "position", VEC3.get().reference("direction"), VEC3.get().immediate(Vec3.ZERO), COMPOUND_TAG.get().reference("tag")))
                .addAction(PlaySoundAction.make("shoot", HOLDER.targetGroup, SoundEvents.GHAST_SHOOT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addTooltip(Component.translatable(Spells.KEY_GHAST_DESC))
        );
        
        addSpell(Spells.ENDER_ARMY, new Spell(modId, "ender_army", Spells.KEY_ENDER_ARMY, 20F)
                .addAction(HasManaAction.make(ACTIVE.activation, OWNER.targetGroup, DOUBLE.get().reference(MANA_COST.name)))
                .addAction(PlayerHasItemsAction.make(ACTIVE.activation, OWNER.targetGroup, SpellsUtil.objectToString(Items.DRAGON_HEAD, ForgeRegistries.ITEMS), INT.get().immediate(1), COMPOUND_TAG.get().immediate(new CompoundTag()), BOOLEAN.get().immediate(true)))
                .addAction(LookAtTargetAction.make(ACTIVE.activation, OWNER.targetGroup, DOUBLE.get().reference("target_range"), 0.5F, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, "", "on_entity_hit", ""))
                .addAction(BurnManaAction.make("on_entity_hit", OWNER.targetGroup, DOUBLE.get().reference(MANA_COST.name)))
                .addAction(ConsumePlayerItemsAction.make("on_entity_hit", OWNER.targetGroup, SpellsUtil.objectToString(Items.DRAGON_HEAD, ForgeRegistries.ITEMS), INT.get().immediate(1), COMPOUND_TAG.get().immediate(new CompoundTag()), BOOLEAN.get().immediate(true)))
                .addAction(RangedEntityTargetsAction.make("on_entity_hit", "targets", ENTITY_HIT.targetGroup, DOUBLE.get().reference("enderman_range")))
                .addAction(LabelAction.make("on_entity_hit", "loop"))
                .addAction(ClearTargetsAction.make("on_entity_hit", "to_check"))
                .addAction(PickTargetAction.make("on_entity_hit", "to_check", "targets", true, false))
                .addAction(GetEntityTypeAction.make("on_entity_hit", "to_check", "type", "", ""))
                .addAction(BooleanActivationAction.make("on_entity_hit", "move_entity", Compiler.compileString(" type == '" + ForgeRegistries.ENTITY_TYPES.getKey(EntityType.ENDERMAN).toString() + "' ", BOOLEAN.get()), BOOLEAN.get().immediate(true), BOOLEAN.get().immediate(false)))
                .addAction(CopyTargetsAction.make("move_entity", "endermen", "to_check"))
                .addAction(DeactivateAction.make("move_entity", "move_entity"))
                .addAction(GetTargetGroupSizeAction.make("on_entity_hit", "targets", "size"))
                .addAction(BranchAction.make("on_entity_hit", "loop", Compiler.compileString(" size > 0 ", BOOLEAN.get())))
                .addAction(SetMobTargetAction.make("on_entity_hit", ENTITY_HIT.targetGroup, "endermen"))
                .addAction(PlaySoundAction.make("on_entity_hit", OWNER.targetGroup, SoundEvents.ENDERMAN_SCREAM, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(PlaySoundAction.make("on_entity_hit", ENTITY_HIT.targetGroup, SoundEvents.ENDERMAN_SCREAM, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addParameter(DOUBLE.get(), "target_range", 50D)
                .addParameter(DOUBLE.get(), "enderman_range", 40D)
                .addTooltip(Component.translatable(Spells.KEY_ENDER_ARMY_DESC))
        );
        
        addPermanentEffectSpell(Spells.PERMANENT_REPLENISHMENT, Spells.KEY_PERMANENT_REPLENISHMENT, Spells.KEY_PERMANENT_REPLENISHMENT_DESC, BuiltinRegistries.REPLENISHMENT_EFFECT.get(), 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_REPLENISHMENT, Spells.KEY_TEMPORARY_REPLENISHMENT, Spells.KEY_TEMPORARY_REPLENISHMENT_DESC, BuiltinRegistries.REPLENISHMENT_EFFECT.get(), 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_REPLENISHMENT, Spells.KEY_TOGGLE_REPLENISHMENT, Spells.KEY_TOGGLE_REPLENISHMENT_DESC, BuiltinRegistries.REPLENISHMENT_EFFECT.get(), 4F, 50, 0);
        
        addPermanentEffectSpell(Spells.PERMANENT_MAGIC_IMMUNE, Spells.KEY_PERMANENT_MAGIC_IMMUNE, Spells.KEY_PERMANENT_MAGIC_IMMUNE_DESC, BuiltinRegistries.MAGIC_IMMUNE_EFFECT.get(), 50, 0);
        addTemporaryEffectSpell(Spells.TEMPORARY_MAGIC_IMMUNE, Spells.KEY_TEMPORARY_MAGIC_IMMUNE, Spells.KEY_TEMPORARY_MAGIC_IMMUNE_DESC, BuiltinRegistries.MAGIC_IMMUNE_EFFECT.get(), 13F, 400, 0);
        addToggleEffectSpell(Spells.TOGGLE_MAGIC_IMMUNE, Spells.KEY_TOGGLE_MAGIC_IMMUNE, Spells.KEY_TOGGLE_MAGIC_IMMUNE_DESC, BuiltinRegistries.MAGIC_IMMUNE_EFFECT.get(), 4F, 50, 0);
        
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
    
    @Override
    public void run(CachedOutput pOutput) throws IOException
    {
        addSpells();
        JsonCodecProvider<Spell> provider = JsonCodecProvider.forDatapackRegistry(gen, exFileHelper, modId, registryOps, Spells.REGISTRY_KEY, spells);
        provider.run(pOutput);
    }
    
    @Override
    public String getName()
    {
        return "Spells & Shields Spells Files";
    }
}
