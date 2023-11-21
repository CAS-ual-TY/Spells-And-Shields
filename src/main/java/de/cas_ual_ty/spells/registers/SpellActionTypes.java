package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.SyncedSpellActionType;
import de.cas_ual_ty.spells.spell.action.ai.ClearMobTargetAction;
import de.cas_ual_ty.spells.spell.action.ai.GetMobTargetAction;
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
import de.cas_ual_ty.spells.spell.action.server.ExecuteCommandAction;
import de.cas_ual_ty.spells.spell.action.server.ExecutePlayerCommandAction;
import de.cas_ual_ty.spells.spell.action.target.*;
import de.cas_ual_ty.spells.spell.action.variable.MappedBinaryVarAction;
import de.cas_ual_ty.spells.spell.action.variable.MappedTernaryVarAction;
import de.cas_ual_ty.spells.spell.action.variable.MappedUnaryVarAction;
import de.cas_ual_ty.spells.spell.action.variable.PutVarAction;
import de.cas_ual_ty.spells.spell.compiler.BinaryOperation;
import de.cas_ual_ty.spells.spell.compiler.Compiler;
import de.cas_ual_ty.spells.spell.compiler.TernaryOperation;
import de.cas_ual_ty.spells.spell.compiler.UnaryOperation;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Objects;
import java.util.Optional;

import static de.cas_ual_ty.spells.SpellsAndShields.MOD_ID;

public class SpellActionTypes
{
    public static final ResourceKey<Registry<SpellActionType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(MOD_ID, "spell_actions"));
    private static final DeferredRegister<SpellActionType<?>> DEFERRED_REGISTER = DeferredRegister.create(REGISTRY_KEY, MOD_ID);
    public static final Registry<SpellActionType<?>> REGISTRY = DEFERRED_REGISTER.makeRegistry(builder -> builder.maxId(1024));
    
    // ai
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ClearMobTargetAction>> CLEAR_MOB_TARGET = DEFERRED_REGISTER.register("clear_mob_target", () -> new SpellActionType<>(ClearMobTargetAction::new, ClearMobTargetAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GetMobTargetAction>> GET_MOB_TARGET = DEFERRED_REGISTER.register("get_mob_target", () -> new SpellActionType<>(GetMobTargetAction::new, GetMobTargetAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<SetMobTargetAction>> SET_MOB_TARGET = DEFERRED_REGISTER.register("set_mob_target", () -> new SpellActionType<>(SetMobTargetAction::new, SetMobTargetAction::makeCodec));
    
    // attribute
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<CheckTagAction>> CHECK_TAG = DEFERRED_REGISTER.register("check_tag", () -> new SpellActionType<>(CheckTagAction::new, CheckTagAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GetAttributeValueAction>> GET_ATTRIBUTE_VALUE = DEFERRED_REGISTER.register("get_attribute_value", () -> new SpellActionType<>(GetAttributeValueAction::new, GetAttributeValueAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GetEntityExtraTagAction>> GET_ENTITY_EXTRA_TAG = DEFERRED_REGISTER.register("get_entity_extra_tag", () -> new SpellActionType<>(GetEntityExtraTagAction::new, GetEntityExtraTagAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GetEntityEyePositionAction>> GET_ENTITY_EYE_POSITION = DEFERRED_REGISTER.register("get_entity_eye_position", () -> new SpellActionType<>(GetEntityEyePositionAction::new, GetEntityEyePositionAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GetEntityPositionDirectionMotionAction>> GET_ENTITY_POSITION_DIRECTION_MOTION = DEFERRED_REGISTER.register("get_entity_position_direction", () -> new SpellActionType<>(GetEntityPositionDirectionMotionAction::new, GetEntityPositionDirectionMotionAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GetEntityTagAction>> GET_ENTITY_TAG = DEFERRED_REGISTER.register("get_entity_tag", () -> new SpellActionType<>(GetEntityTagAction::new, GetEntityTagAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GetEntityTypeAction>> GET_ENTITY_TYPE = DEFERRED_REGISTER.register("get_entity_type", () -> new SpellActionType<>(GetEntityTypeAction::new, GetEntityTypeAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GetEntityUUIDAction>> GET_ENTITY_UUID = DEFERRED_REGISTER.register("get_entity_uuid", () -> new SpellActionType<>(GetEntityUUIDAction::new, GetEntityUUIDAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GetIsCreativeAction>> GET_IS_CREATIVE = DEFERRED_REGISTER.register("get_is_creative", () -> new SpellActionType<>(GetIsCreativeAction::new, GetIsCreativeAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GetPlayerNameAction>> GET_PLAYER_NAME = DEFERRED_REGISTER.register("get_player_name", () -> new SpellActionType<>(GetPlayerNameAction::new, GetPlayerNameAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GetPositionAction>> GET_POSITION = DEFERRED_REGISTER.register("get_position", () -> new SpellActionType<>(GetPositionAction::new, GetPositionAction::makeCodec));
    
    // control flow
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ActivateAction>> ACTIVATE = DEFERRED_REGISTER.register("activate", () -> new SpellActionType<>(ActivateAction::new, ActivateAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<BooleanActivationAction>> BOOLEAN_ACTIVATION = DEFERRED_REGISTER.register("boolean_activation", () -> new SpellActionType<>(BooleanActivationAction::new, BooleanActivationAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<BranchAction>> BRANCH = DEFERRED_REGISTER.register("branch", () -> new SpellActionType<>(BranchAction::new, BranchAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ConditionalActivationAction>> CONDITIONAL_ACTIVATION = DEFERRED_REGISTER.register("conditional_activation", () -> new SpellActionType<>(ConditionalActivationAction::new, ConditionalActivationAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ConditionalDeactivationAction>> CONDITIONAL_DEACTIVATION = DEFERRED_REGISTER.register("conditional_deactivation", () -> new SpellActionType<>(ConditionalDeactivationAction::new, ConditionalDeactivationAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<DeactivateAction>> DEACTIVATE = DEFERRED_REGISTER.register("deactivate", () -> new SpellActionType<>(DeactivateAction::new, DeactivateAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<JumpAction>> JUMP = DEFERRED_REGISTER.register("jump", () -> new SpellActionType<>(JumpAction::new, JumpAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<LabelAction>> LABEL = DEFERRED_REGISTER.register("label", () -> new SpellActionType<>(LabelAction::new, LabelAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<TerminateAction>> TERMINATE = DEFERRED_REGISTER.register("terminate", () -> new SpellActionType<>(TerminateAction::new, TerminateAction::makeCodec));
    
    // delayed
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<AddDelayedSpellAction>> ADD_DELAYED_SPELL = DEFERRED_REGISTER.register("add_delayed_spell", () -> new SpellActionType<>(AddDelayedSpellAction::new, AddDelayedSpellAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<CheckHasDelayedSpellAction>> CHECK_HAS_DELAYED_SPELL = DEFERRED_REGISTER.register("check_has_delayed_spell", () -> new SpellActionType<>(CheckHasDelayedSpellAction::new, CheckHasDelayedSpellAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<RemoveDelayedSpellAction>> REMOVE_DELAYED_SPELL = DEFERRED_REGISTER.register("remove_delayed_spell", () -> new SpellActionType<>(RemoveDelayedSpellAction::new, RemoveDelayedSpellAction::makeCodec));
    
    // effects
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<AddAttributeModifierAction>> ADD_ATTRIBUTE_MODIFIER = DEFERRED_REGISTER.register("add_attribute_modifier", () -> new SpellActionType<>(AddAttributeModifierAction::new, AddAttributeModifierAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ApplyEntityExtraTagAction>> APPLY_ENTITY_EXTRA_TAG = DEFERRED_REGISTER.register("apply_entity_extra_tag", () -> new SpellActionType<>(ApplyEntityExtraTagAction::new, ApplyEntityExtraTagAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ApplyEntityTagAction>> APPLY_ENTITY_TAG = DEFERRED_REGISTER.register("apply_entity_tag", () -> new SpellActionType<>(ApplyEntityTagAction::new, ApplyEntityTagAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ApplyMobEffectAction>> APPLY_MOB_EFFECT = DEFERRED_REGISTER.register("apply_mob_effect", () -> new SpellActionType<>(ApplyMobEffectAction::new, ApplyMobEffectAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ApplyPotionEffectAction>> APPLY_POTION_EFFECT = DEFERRED_REGISTER.register("apply_potion_effect", () -> new SpellActionType<>(ApplyPotionEffectAction::new, ApplyPotionEffectAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<DamageAction>> DAMAGE = DEFERRED_REGISTER.register("damage", () -> new SpellActionType<>(DamageAction::new, DamageAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<HealAction>> HEAL = DEFERRED_REGISTER.register("heal", () -> new SpellActionType<>(HealAction::new, HealAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<KnockbackAction>> KNOCKBACK = DEFERRED_REGISTER.register("knockback", () -> new SpellActionType<>(KnockbackAction::new, KnockbackAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<RemoveAttributeModifierAction>> REMOVE_ATTRIBUTE_MODIFIER = DEFERRED_REGISTER.register("remove_attribute_modifier", () -> new SpellActionType<>(RemoveAttributeModifierAction::new, RemoveAttributeModifierAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ResetFallDistanceAction>> RESET_FALL_DISTANCE = DEFERRED_REGISTER.register("reset_fall_distance", () -> new SpellActionType<>(ResetFallDistanceAction::new, ResetFallDistanceAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<SetMotionAction>> SET_MOTION = DEFERRED_REGISTER.register("set_motion", () -> new SyncedSpellActionType<>(SetMotionAction::new, SetMotionAction::makeCodec, SetMotionAction.ClientAction::new));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<SetOnFireAction>> SET_ON_FIRE = DEFERRED_REGISTER.register("set_on_fire", () -> new SpellActionType<>(SetOnFireAction::new, SetOnFireAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<SourcedDamageAction>> SOURCED_DAMAGE = DEFERRED_REGISTER.register("sourced_damage", () -> new SpellActionType<>(SourcedDamageAction::new, SourcedDamageAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<SourcedKnockbackAction>> SOURCED_KNOCKBACK = DEFERRED_REGISTER.register("sourced_knockback", () -> new SpellActionType<>(SourcedKnockbackAction::new, SourcedKnockbackAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<SpawnEntityAction>> SPAWN_ENTITY = DEFERRED_REGISTER.register("spawn_entity", () -> new SpellActionType<>(SpawnEntityAction::new, SpawnEntityAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<TeleportToAction>> TELEPORT_TO = DEFERRED_REGISTER.register("teleport_to", () -> new SpellActionType<>(TeleportToAction::new, TeleportToAction::makeCodec));
    
    // fx
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ParticleEmitterAction>> PARTICLE_EMITTER = DEFERRED_REGISTER.register("particle_emitter", () -> new SpellActionType<>(ParticleEmitterAction::new, ParticleEmitterAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<PlaySoundAction>> PLAY_SOUND = DEFERRED_REGISTER.register("play_sound", () -> new SpellActionType<>(PlaySoundAction::new, PlaySoundAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<SpawnParticlesAction>> SPAWN_PARTICLES = DEFERRED_REGISTER.register("spawn_particles", () -> new SpellActionType<>(SpawnParticlesAction::new, SpawnParticlesAction::makeCodec));
    
    // item
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ConsumeItemAction>> CONSUME_ITEM = DEFERRED_REGISTER.register("consume_item", () -> new SpellActionType<>(ConsumeItemAction::new, ConsumeItemAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ConsumePlayerItemsAction>> CONSUME_PLAYER_ITEMS = DEFERRED_REGISTER.register("consume_player_items", () -> new SpellActionType<>(ConsumePlayerItemsAction::new, ConsumePlayerItemsAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<DamageItemAction>> DAMAGE_ITEM = DEFERRED_REGISTER.register("damage_item", () -> new SpellActionType<>(DamageItemAction::new, DamageItemAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<FindItemAction>> FIND_ITEM = DEFERRED_REGISTER.register("find_item", () -> new SpellActionType<>(FindItemAction::new, FindItemAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GetItemAttributesAction>> GET_ITEM_ATTRIBUTES = DEFERRED_REGISTER.register("get_item_attributes", () -> new SpellActionType<>(GetItemAttributesAction::new, GetItemAttributesAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GetItemTagAction>> GET_ITEM_TAG = DEFERRED_REGISTER.register("get_item_tag", () -> new SpellActionType<>(GetItemTagAction::new, GetItemTagAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GiveItemAction>> GIVE_ITEM = DEFERRED_REGISTER.register("give_item", () -> new SpellActionType<>(GiveItemAction::new, GiveItemAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ItemCheckAction>> ITEM_CHECK = DEFERRED_REGISTER.register("item_check", () -> new SpellActionType<>(ItemCheckAction::new, ItemCheckAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ItemEqualsAction>> ITEM_EQUALS = DEFERRED_REGISTER.register("item_equals", () -> new SpellActionType<>(ItemEqualsAction::new, ItemEqualsAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ItemTagCheckAction>> ITEM_TAG_CHECK = DEFERRED_REGISTER.register("item_tag_check", () -> new SpellActionType<>(ItemTagCheckAction::new, ItemTagCheckAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ItemTagEqualsAction>> ITEM_TAG_EQUALS = DEFERRED_REGISTER.register("item_tag_equals", () -> new SpellActionType<>(ItemTagEqualsAction::new, ItemTagEqualsAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MainhandItemTargetAction>> MAINHAND_ITEM_TARGET = DEFERRED_REGISTER.register("mainhand_item_target", () -> new SpellActionType<>(MainhandItemTargetAction::new, MainhandItemTargetAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ModifyItemAction>> MODIFY_ITEM = DEFERRED_REGISTER.register("modify_item", () -> new SpellActionType<>(ModifyItemAction::new, ModifyItemAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<OffhandItemTargetAction>> OFFHAND_ITEM_TARGET = DEFERRED_REGISTER.register("offhand_item_target", () -> new SpellActionType<>(OffhandItemTargetAction::new, OffhandItemTargetAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<OverrideItemAction>> OVERRIDE_ITEM = DEFERRED_REGISTER.register("override_item", () -> new SpellActionType<>(OverrideItemAction::new, OverrideItemAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<PlayerHasItemsAction>> PLAYER_HAS_ITEMS = DEFERRED_REGISTER.register("player_has_items", () -> new SpellActionType<>(PlayerHasItemsAction::new, PlayerHasItemsAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<PlayerItemTargetsAction>> PLAYER_ITEM_TARGETS = DEFERRED_REGISTER.register("player_item_targets", () -> new SpellActionType<>(PlayerItemTargetsAction::new, PlayerItemTargetsAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<TryConsumeItemAction>> TRY_CONSUME_ITEM = DEFERRED_REGISTER.register("try_consume_item", () -> new SpellActionType<>(TryConsumeItemAction::new, TryConsumeItemAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<TryDamageItemAction>> TRY_DAMAGE_ITEM = DEFERRED_REGISTER.register("try_damage_item", () -> new SpellActionType<>(TryDamageItemAction::new, TryDamageItemAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<UseItemAction>> USE_ITEM = DEFERRED_REGISTER.register("use_item", () -> new SpellActionType<>(UseItemAction::new, UseItemAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<UseItemOnBlockAction>> USE_ITEM_ON_BLOCK = DEFERRED_REGISTER.register("use_item_on_block", () -> new SpellActionType<>(UseItemOnBlockAction::new, UseItemOnBlockAction::makeCodec));
    
    // level (world)
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<CubeBlockTargetsAction>> CUBE_BLOCK_TARGETS = DEFERRED_REGISTER.register("cube_block_targets", () -> new SpellActionType<>(CubeBlockTargetsAction::new, CubeBlockTargetsAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GetBlockAction>> GET_BLOCK = DEFERRED_REGISTER.register("get_block", () -> new SpellActionType<>(GetBlockAction::new, GetBlockAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GetBlockAttributesAction>> GET_BLOCK_ATTRIBUTES = DEFERRED_REGISTER.register("get_block_attributes", () -> new SpellActionType<>(GetBlockAttributesAction::new, GetBlockAttributesAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GetFluidAction>> GET_FLUID = DEFERRED_REGISTER.register("get_fluid", () -> new SpellActionType<>(GetFluidAction::new, GetFluidAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<OffsetBlockAction>> OFFSET_BLOCK = DEFERRED_REGISTER.register("offset_block", () -> new SpellActionType<>(OffsetBlockAction::new, OffsetBlockAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<PlayerHarvestBlockAction>> PLAYER_HARVEST_BLOCK = DEFERRED_REGISTER.register("player_harvest_block", () -> new SpellActionType<>(PlayerHarvestBlockAction::new, PlayerHarvestBlockAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<PositionToTargetAction>> POSITION_TO_TARGET = DEFERRED_REGISTER.register("position_to_target", () -> new SpellActionType<>(PositionToTargetAction::new, PositionToTargetAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<RemoveBlockAction>> REMOVE_BLOCK = DEFERRED_REGISTER.register("remove_block", () -> new SpellActionType<>(RemoveBlockAction::new, RemoveBlockAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<SetBlockAction>> SET_BLOCK = DEFERRED_REGISTER.register("set_block", () -> new SpellActionType<>(SetBlockAction::new, SetBlockAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<TickBlockAction>> TICK_BLOCK = DEFERRED_REGISTER.register("tick_block", () -> new SpellActionType<>(TickBlockAction::new, TickBlockAction::makeCodec));
    
    // mana
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<BurnManaAction>> BURN_MANA = DEFERRED_REGISTER.register("burn_mana", () -> new SpellActionType<>(BurnManaAction::new, BurnManaAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GetManaAction>> GET_MANA = DEFERRED_REGISTER.register("get_mana", () -> new SpellActionType<>(GetManaAction::new, GetManaAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<HasManaAction>> HAS_MANA = DEFERRED_REGISTER.register("has_mana", () -> new SpellActionType<>(HasManaAction::new, HasManaAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ManaCheckAction>> MANA_CHECK = DEFERRED_REGISTER.register("mana_check", () -> new SpellActionType<>(ManaCheckAction::new, ManaCheckAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ReplenishManaAction>> REPLENISH_MANA = DEFERRED_REGISTER.register("replenish_mana", () -> new SpellActionType<>(ReplenishManaAction::new, ReplenishManaAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<SimpleManaCheckAction>> SIMPLE_MANA_CHECK = DEFERRED_REGISTER.register("simple_mana_check", () -> new SpellActionType<>(SimpleManaCheckAction::new, SimpleManaCheckAction::makeCodec));
    
    // server
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ExecuteCommandAction>> EXECUTE_COMMAND = DEFERRED_REGISTER.register("execute_command", () -> new SpellActionType<>(ExecuteCommandAction::new, ExecuteCommandAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ExecutePlayerCommandAction>> EXECUTE_PLAYER_COMMAND = DEFERRED_REGISTER.register("execute_player_command", () -> new SpellActionType<>(ExecutePlayerCommandAction::new, ExecutePlayerCommandAction::makeCodec));
    
    // target
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ClearTargetsAction>> CLEAR_TARGETS = DEFERRED_REGISTER.register("clear_targets", () -> new SpellActionType<>(ClearTargetsAction::new, ClearTargetsAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<CopyTargetsAction>> COPY_TARGETS = DEFERRED_REGISTER.register("copy_targets", () -> new SpellActionType<>(CopyTargetsAction::new, CopyTargetsAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<EntityUUIDTargetAction>> ENTITY_UUID_TARGET = DEFERRED_REGISTER.register("entity_uuid_target", () -> new SpellActionType<>(EntityUUIDTargetAction::new, EntityUUIDTargetAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GetTargetGroupSizeAction>> GET_TARGET_GROUP_SIZE = DEFERRED_REGISTER.register("get_target_group_size", () -> new SpellActionType<>(GetTargetGroupSizeAction::new, GetTargetGroupSizeAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<HomeAction>> HOME = DEFERRED_REGISTER.register("home", () -> new SpellActionType<>(HomeAction::new, HomeAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<LookAtTargetAction>> LOOK_AT_TARGET = DEFERRED_REGISTER.register("look_at_target", () -> new SpellActionType<>(LookAtTargetAction::new, LookAtTargetAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MovePlayerTargetsAction>> MOVE_PLAYER_TARGETS = DEFERRED_REGISTER.register("move_player_targets", () -> new SpellActionType<>(MovePlayerTargetsAction::new, MovePlayerTargetsAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<PickTargetAction>> PICK_TARGET = DEFERRED_REGISTER.register("pick_target", () -> new SpellActionType<>(PickTargetAction::new, PickTargetAction::makeCodec2));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<RangedEntityTargetsAction>> RANGED_ENTITY_TARGETS = DEFERRED_REGISTER.register("ranged_entity_targets", () -> new SpellActionType<>(RangedEntityTargetsAction::new, RangedEntityTargetsAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ShootAction>> SHOOT = DEFERRED_REGISTER.register("shoot", () -> new SpellActionType<>(ShootAction::new, ShootAction::makeCodec));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ShootAltAction>> SHOOT_ALT = DEFERRED_REGISTER.register("shoot_alt", () -> new SpellActionType<>(ShootAltAction::new, ShootAltAction::makeCodec));
    
    // variable
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<PutVarAction<Integer>>> PUT_INT = DEFERRED_REGISTER.register("put_int", () -> PutVarAction.makeType(CtxVarTypes.INT));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<PutVarAction<Double>>> PUT_DOUBLE = DEFERRED_REGISTER.register("put_double", () -> PutVarAction.makeType(CtxVarTypes.DOUBLE));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<PutVarAction<Vec3>>> PUT_VEC3 = DEFERRED_REGISTER.register("put_vec3", () -> PutVarAction.makeType(CtxVarTypes.VEC3));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<PutVarAction<Boolean>>> PUT_BOOLEAN = DEFERRED_REGISTER.register("put_boolean", () -> PutVarAction.makeType(CtxVarTypes.BOOLEAN));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<PutVarAction<CompoundTag>>> PUT_TAG = DEFERRED_REGISTER.register("put_tag", () -> PutVarAction.makeType(CtxVarTypes.TAG));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<PutVarAction<String>>> PUT_STRING = DEFERRED_REGISTER.register("put_string", () -> PutVarAction.makeType(CtxVarTypes.STRING));
    
    // variable / mapped unary functions
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> NEGATE = DEFERRED_REGISTER.register("negate", () -> MappedUnaryVarAction.makeType(UnaryOperation.NEGATE));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> NOT = DEFERRED_REGISTER.register("not", () -> MappedUnaryVarAction.makeType(UnaryOperation.NOT));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> ROUND = DEFERRED_REGISTER.register("round", () -> MappedUnaryVarAction.makeType(UnaryOperation.ROUND));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> FLOOR = DEFERRED_REGISTER.register("floor", () -> MappedUnaryVarAction.makeType(UnaryOperation.FLOOR));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> CEIL = DEFERRED_REGISTER.register("ceil", () -> MappedUnaryVarAction.makeType(UnaryOperation.CEIL));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> SQRT = DEFERRED_REGISTER.register("sqrt", () -> MappedUnaryVarAction.makeType(UnaryOperation.SQRT));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> GET_X = DEFERRED_REGISTER.register("get_x", () -> MappedUnaryVarAction.makeType(UnaryOperation.GET_X));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> GET_Y = DEFERRED_REGISTER.register("get_y", () -> MappedUnaryVarAction.makeType(UnaryOperation.GET_Y));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> GET_Z = DEFERRED_REGISTER.register("get_z", () -> MappedUnaryVarAction.makeType(UnaryOperation.GET_Z));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> LENGTH = DEFERRED_REGISTER.register("length", () -> MappedUnaryVarAction.makeType(UnaryOperation.LENGTH));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> NORMALIZE = DEFERRED_REGISTER.register("normalized", () -> MappedUnaryVarAction.makeType(UnaryOperation.NORMALIZE));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> SIN = DEFERRED_REGISTER.register("sin", () -> MappedUnaryVarAction.makeType(UnaryOperation.SIN));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> COS = DEFERRED_REGISTER.register("cos", () -> MappedUnaryVarAction.makeType(UnaryOperation.COS));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> ASIN = DEFERRED_REGISTER.register("asin", () -> MappedUnaryVarAction.makeType(UnaryOperation.ASIN));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> ACOS = DEFERRED_REGISTER.register("acos", () -> MappedUnaryVarAction.makeType(UnaryOperation.ACOS));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> TO_RADIANS = DEFERRED_REGISTER.register("to_radians", () -> MappedUnaryVarAction.makeType(UnaryOperation.TO_RADIANS));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> TO_DEGREES = DEFERRED_REGISTER.register("to_degrees", () -> MappedUnaryVarAction.makeType(UnaryOperation.TO_DEGREES));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> UUID_FROM_STRING = DEFERRED_REGISTER.register("uuid_from_string", () -> MappedUnaryVarAction.makeType(UnaryOperation.UUID_FROM_STRING));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedUnaryVarAction>> NEXT_INT = DEFERRED_REGISTER.register("next_int", () -> MappedUnaryVarAction.makeType(UnaryOperation.NEXT_INT));
    
    // variable / mapped binary functions
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> ADD = DEFERRED_REGISTER.register("add", () -> MappedBinaryVarAction.makeType(BinaryOperation.ADD));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> SUB = DEFERRED_REGISTER.register("sub", () -> MappedBinaryVarAction.makeType(BinaryOperation.SUB));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> MUL = DEFERRED_REGISTER.register("mul", () -> MappedBinaryVarAction.makeType(BinaryOperation.MUL));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> DIV = DEFERRED_REGISTER.register("div", () -> MappedBinaryVarAction.makeType(BinaryOperation.DIV));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> REM = DEFERRED_REGISTER.register("rem", () -> MappedBinaryVarAction.makeType(BinaryOperation.REM));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> EQ = DEFERRED_REGISTER.register("eq", () -> MappedBinaryVarAction.makeType(BinaryOperation.EQ));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> NEQ = DEFERRED_REGISTER.register("neq", () -> MappedBinaryVarAction.makeType(BinaryOperation.NEQ));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> GT = DEFERRED_REGISTER.register("gt", () -> MappedBinaryVarAction.makeType(BinaryOperation.GT));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> GEQ = DEFERRED_REGISTER.register("geq", () -> MappedBinaryVarAction.makeType(BinaryOperation.GEQ));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> LT = DEFERRED_REGISTER.register("lt", () -> MappedBinaryVarAction.makeType(BinaryOperation.LT));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> LEQ = DEFERRED_REGISTER.register("leq", () -> MappedBinaryVarAction.makeType(BinaryOperation.LEQ));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> MIN = DEFERRED_REGISTER.register("min", () -> MappedBinaryVarAction.makeType(BinaryOperation.MIN));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> MAX = DEFERRED_REGISTER.register("max", () -> MappedBinaryVarAction.makeType(BinaryOperation.MAX));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> MOVE_X = DEFERRED_REGISTER.register("move_x", () -> MappedBinaryVarAction.makeType(BinaryOperation.MOVE_X));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> MOVE_Y = DEFERRED_REGISTER.register("move_y", () -> MappedBinaryVarAction.makeType(BinaryOperation.MOVE_Y));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> MOVE_Z = DEFERRED_REGISTER.register("move_z", () -> MappedBinaryVarAction.makeType(BinaryOperation.MOVE_Z));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> NBT_CONTAINS = DEFERRED_REGISTER.register("nbt_contains", () -> MappedBinaryVarAction.makeType(BinaryOperation.NBT_CONTAINS));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> GET_NBT_INT = DEFERRED_REGISTER.register("get_nbt_int", () -> MappedBinaryVarAction.makeType(BinaryOperation.GET_NBT_INT));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> GET_NBT_DOUBLE = DEFERRED_REGISTER.register("get_nbt_double", () -> MappedBinaryVarAction.makeType(BinaryOperation.GET_NBT_DOUBLE));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> GET_NBT_BOOLEAN = DEFERRED_REGISTER.register("get_nbt_boolean", () -> MappedBinaryVarAction.makeType(BinaryOperation.GET_NBT_BOOLEAN));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> GET_NBT_TAG = DEFERRED_REGISTER.register("get_nbt_tag", () -> MappedBinaryVarAction.makeType(BinaryOperation.GET_NBT_TAG));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> GET_NBT_STRING = DEFERRED_REGISTER.register("get_nbt_string", () -> MappedBinaryVarAction.makeType(BinaryOperation.GET_NBT_STRING));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> GET_NBT_UUID = DEFERRED_REGISTER.register("get_nbt_uuid", () -> MappedBinaryVarAction.makeType(BinaryOperation.GET_NBT_UUID));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedBinaryVarAction>> GET_NBT_VEC3 = DEFERRED_REGISTER.register("get_nbt_vec3", () -> MappedBinaryVarAction.makeType(BinaryOperation.GET_NBT_VEC3));
    
    // variable / mapped ternary functions
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedTernaryVarAction>> CONDITIONAL = DEFERRED_REGISTER.register("conditional", () -> MappedTernaryVarAction.makeType(TernaryOperation.CONDITIONAL));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedTernaryVarAction>> VEC3 = DEFERRED_REGISTER.register("vec3", () -> MappedTernaryVarAction.makeType(TernaryOperation.VEC3));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedTernaryVarAction>> BLOCK_POS = DEFERRED_REGISTER.register("block_pos", () -> MappedTernaryVarAction.makeType(TernaryOperation.BLOCK_POS));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedTernaryVarAction>> PUT_NBT_INT = DEFERRED_REGISTER.register("put_nbt_int", () -> MappedTernaryVarAction.makeType(TernaryOperation.PUT_NBT_INT));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedTernaryVarAction>> PUT_NBT_DOUBLE = DEFERRED_REGISTER.register("put_nbt_double", () -> MappedTernaryVarAction.makeType(TernaryOperation.PUT_NBT_DOUBLE));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedTernaryVarAction>> PUT_NBT_BOOLEAN = DEFERRED_REGISTER.register("put_nbt_boolean", () -> MappedTernaryVarAction.makeType(TernaryOperation.PUT_NBT_BOOLEAN));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedTernaryVarAction>> PUT_NBT_TAG = DEFERRED_REGISTER.register("put_nbt_tag", () -> MappedTernaryVarAction.makeType(TernaryOperation.PUT_NBT_TAG));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedTernaryVarAction>> PUT_NBT_STRING = DEFERRED_REGISTER.register("put_nbt_string", () -> MappedTernaryVarAction.makeType(TernaryOperation.PUT_NBT_STRING));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedTernaryVarAction>> PUT_NBT_UUID = DEFERRED_REGISTER.register("put_nbt_uuid", () -> MappedTernaryVarAction.makeType(TernaryOperation.PUT_NBT_UUID));
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MappedTernaryVarAction>> PUT_NBT_VEC3 = DEFERRED_REGISTER.register("put_nbt_vec3", () -> MappedTernaryVarAction.makeType(TernaryOperation.PUT_NBT_VEC3));
    
    // variable / simple unary
    // -/-
    
    //variable / simple binary
    // -/-
    
    //variable / simple binary
    // -/-
    
    public static void register()
    {
        DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpellActionTypes::setup);
    }
    
    private static void setup(FMLCommonSetupEvent event)
    {
        UnaryOperation.NEGATE.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x) -> -x)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x) -> -x)
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), (x) -> x.reverse());
        UnaryOperation.NOT.register(CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), (x) -> !x);
        
        UnaryOperation.ROUND.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.INT.get(), (x) -> (int) Math.round(x));
        UnaryOperation.FLOOR.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.INT.get(), (x) -> (int) Math.floor(x));
        UnaryOperation.CEIL.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.INT.get(), (x) -> (int) Math.ceil(x));
        
        UnaryOperation.SQRT.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x) -> x < 0 ? null : Math.sqrt(x));
        
        UnaryOperation.GET_X.register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), (x) -> x.x());
        UnaryOperation.GET_Y.register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), (x) -> x.y());
        UnaryOperation.GET_Z.register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), (x) -> x.z());
        
        UnaryOperation.LENGTH.register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), (x) -> x.length());
        UnaryOperation.NORMALIZE.register(CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), (x) -> x.normalize());
        
        UnaryOperation.SIN.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x) -> Math.sin(x));
        UnaryOperation.COS.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x) -> Math.cos(x));
        UnaryOperation.ASIN.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x) -> Math.asin(x));
        UnaryOperation.ACOS.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x) -> Math.acos(x));
        UnaryOperation.TO_RADIANS.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x) -> Math.toRadians(x));
        UnaryOperation.TO_DEGREES.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x) -> Math.toDegrees(x));
        
        UnaryOperation.UUID_FROM_STRING.register(CtxVarTypes.STRING.get(), CtxVarTypes.STRING.get(), (x) -> SpellsUtil.generateUUIDFromName(x).toString());
        UnaryOperation.NEXT_INT.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x) -> Compiler.RANDOM.nextInt(x));
        
        BinaryOperation.ADD.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> x + y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> x + y)
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), (x, y) -> x.add(y))
                .register(CtxVarTypes.STRING.get(), CtxVarTypes.STRING.get(), CtxVarTypes.STRING.get(), (x, y) -> x + y);
        BinaryOperation.SUB.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> x - y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> x - y)
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), (x, y) -> x.subtract(y));
        BinaryOperation.MUL.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> x * y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> x * y)
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.VEC3.get(), (x, y) -> x.scale(y));
        BinaryOperation.DIV.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> y == 0 ? null : x / y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> y == 0 ? null : x / y)
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.VEC3.get(), (x, y) -> y == 0 ? null : x.scale(1D / y));
        BinaryOperation.REM.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> y == 0 ? null : x % y);
        
        BinaryOperation.EQ.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x.equals(y))
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x.equals(y))
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x.equals(y))
                .register(CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x == y)
                .register(CtxVarTypes.STRING.get(), CtxVarTypes.STRING.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x.equals(y));
        BinaryOperation.NEQ.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> !Objects.equals(x, y))
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> !Objects.equals(x, y))
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> !x.equals(y))
                .register(CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x != y)
                .register(CtxVarTypes.STRING.get(), CtxVarTypes.STRING.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> !x.equals(y));
        BinaryOperation.GT.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x > y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x > y);
        BinaryOperation.GEQ.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x >= y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x >= y);
        BinaryOperation.LT.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x < y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x < y);
        BinaryOperation.LEQ.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x <= y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x <= y);
        
        BinaryOperation.AND.register(CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x && y);
        BinaryOperation.OR.register(CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x || y);
        
        BinaryOperation.MIN.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> Math.min(x, y))
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> Math.min(x, y));
        BinaryOperation.MAX.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> Math.max(x, y))
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> Math.max(x, y));
        
        BinaryOperation.NBT_CONTAINS.register(CtxVarTypes.TAG.get(), CtxVarTypes.STRING.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x.contains(y));
        BinaryOperation.GET_NBT_INT.register(CtxVarTypes.TAG.get(), CtxVarTypes.STRING.get(), CtxVarTypes.INT.get(), (x, y) -> x.contains(y, Tag.TAG_ANY_NUMERIC) ? x.getInt(y) : null);
        BinaryOperation.GET_NBT_DOUBLE.register(CtxVarTypes.TAG.get(), CtxVarTypes.STRING.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> x.contains(y, Tag.TAG_ANY_NUMERIC) ? x.getDouble(y) : null);
        BinaryOperation.GET_NBT_BOOLEAN.register(CtxVarTypes.TAG.get(), CtxVarTypes.STRING.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x.contains(y, Tag.TAG_ANY_NUMERIC) ? x.getBoolean(y) : null);
        BinaryOperation.GET_NBT_TAG.register(CtxVarTypes.TAG.get(), CtxVarTypes.STRING.get(), CtxVarTypes.TAG.get(), (x, y) -> x.contains(y, Tag.TAG_COMPOUND) ? x.getCompound(y) : null);
        BinaryOperation.GET_NBT_STRING.register(CtxVarTypes.TAG.get(), CtxVarTypes.STRING.get(), CtxVarTypes.STRING.get(), (x, y) -> x.contains(y, Tag.TAG_STRING) ? x.getString(y) : null);
        BinaryOperation.GET_NBT_UUID.register(CtxVarTypes.TAG.get(), CtxVarTypes.STRING.get(), CtxVarTypes.STRING.get(), (x, y) -> x.hasUUID(y) ? x.getUUID(y).toString() : null);
        BinaryOperation.GET_NBT_VEC3.register(CtxVarTypes.TAG.get(), CtxVarTypes.STRING.get(), CtxVarTypes.VEC3.get(), (x, y) -> {
            if(x.contains(y, Tag.TAG_LIST))
            {
                ListTag list = x.getList(y, Tag.TAG_DOUBLE);
                if(list.size() != 3)
                {
                    return null;
                }
                
                return new Vec3(list.getDouble(0), list.getDouble(1), list.getDouble(2));
            }
            return null;
        });
        
        TernaryOperation.CONDITIONAL.register(CtxVarTypes.BOOLEAN.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y, z) -> x ? y : z)
                .register(CtxVarTypes.BOOLEAN.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y, z) -> x ? y : z)
                .register(CtxVarTypes.BOOLEAN.get(), CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), (x, y, z) -> x ? y : z)
                .register(CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), (x, y, z) -> x ? y : z);
        
        TernaryOperation.VEC3.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.VEC3.get(), (x, y, z) -> new Vec3(x, y, z));
        
        TernaryOperation.PUT_NBT_INT.register(CtxVarTypes.TAG.get(), CtxVarTypes.STRING.get(), CtxVarTypes.INT.get(), CtxVarTypes.TAG.get(), (x, y, z) -> {
            x.putInt(y, z);
            return x;
        });
        TernaryOperation.PUT_NBT_DOUBLE.register(CtxVarTypes.TAG.get(), CtxVarTypes.STRING.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.TAG.get(), (x, y, z) -> {
            x.putDouble(y, z);
            return x;
        });
        TernaryOperation.PUT_NBT_BOOLEAN.register(CtxVarTypes.TAG.get(), CtxVarTypes.STRING.get(), CtxVarTypes.BOOLEAN.get(), CtxVarTypes.TAG.get(), (x, y, z) -> {
            x.putBoolean(y, z);
            return x;
        });
        TernaryOperation.PUT_NBT_TAG.register(CtxVarTypes.TAG.get(), CtxVarTypes.STRING.get(), CtxVarTypes.TAG.get(), CtxVarTypes.TAG.get(), (x, y, z) -> {
            x.put(y, z);
            return x;
        });
        TernaryOperation.PUT_NBT_STRING.register(CtxVarTypes.TAG.get(), CtxVarTypes.STRING.get(), CtxVarTypes.STRING.get(), CtxVarTypes.TAG.get(), (x, y, z) -> {
            x.putString(y, z);
            return x;
        });
        TernaryOperation.PUT_NBT_UUID.register(CtxVarTypes.TAG.get(), CtxVarTypes.STRING.get(), CtxVarTypes.STRING.get(), CtxVarTypes.TAG.get(), (x, y, z) -> {
            Optional.of(SpellsUtil.uuidFromString(z)).ifPresent(uuid -> x.putUUID(y, uuid));
            return x;
        });
        TernaryOperation.PUT_NBT_VEC3.register(CtxVarTypes.TAG.get(), CtxVarTypes.STRING.get(), CtxVarTypes.VEC3.get(), CtxVarTypes.TAG.get(), (x, y, z) -> {
            ListTag list = new ListTag();
            list.add(DoubleTag.valueOf(z.x()));
            list.add(DoubleTag.valueOf(z.y()));
            list.add(DoubleTag.valueOf(z.z()));
            x.put(y, list);
            return x;
        });
    }
}
