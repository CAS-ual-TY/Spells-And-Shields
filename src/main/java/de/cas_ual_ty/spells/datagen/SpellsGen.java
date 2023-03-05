package de.cas_ual_ty.spells.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.cas_ual_ty.spells.registers.SpellIconTypes;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.action.attribute.*;
import de.cas_ual_ty.spells.spell.action.control.ActivateAction;
import de.cas_ual_ty.spells.spell.action.control.DeactivateAction;
import de.cas_ual_ty.spells.spell.action.control.ItemEqualsActivationAction;
import de.cas_ual_ty.spells.spell.action.control.SimpleManaCheckAction;
import de.cas_ual_ty.spells.spell.action.effect.*;
import de.cas_ual_ty.spells.spell.action.fx.PlaySoundAction;
import de.cas_ual_ty.spells.spell.action.fx.SpawnParticlesAction;
import de.cas_ual_ty.spells.spell.action.item.ConsumeItemAction;
import de.cas_ual_ty.spells.spell.action.item.OverrideItemAction;
import de.cas_ual_ty.spells.spell.action.item.SimpleItemCheckAction;
import de.cas_ual_ty.spells.spell.action.target.*;
import de.cas_ual_ty.spells.spell.action.variable.PutVarAction;
import de.cas_ual_ty.spells.spell.compiler.Compiler;
import de.cas_ual_ty.spells.spell.icon.DefaultSpellIcon;
import de.cas_ual_ty.spells.spell.icon.SpellIcon;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static de.cas_ual_ty.spells.registers.CtxVarTypes.*;
import static de.cas_ual_ty.spells.spell.context.BuiltinActivations.ACTIVE;
import static de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups.*;
import static de.cas_ual_ty.spells.spell.context.BuiltinVariables.MANA_COST;

public class SpellsGen implements DataProvider
{
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
        dummy(rl, key, descKey, new DefaultSpellIcon(SpellIconTypes.DEFAULT.get(), new ResourceLocation(modId, "textures/spell/" + rl.getPath() + ".png")));
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
    
    protected void addSpells()
    {
        dummy(Spells.DUMMY);
        
        addSpell(Spells.LEAP, new Spell(modId, "leap", Spells.KEY_LEAP, 2.5F)
                .addParameter(DOUBLE.get(), "speed", 2.5)
                .addAction(SimpleManaCheckAction.make(ACTIVE.activation))
                .addAction(ResetFallDistanceAction.make(ACTIVE.activation, OWNER.targetGroup))
                .addAction(GetEntityPositionDirectionAction.make(ACTIVE.activation, OWNER.targetGroup, "", "look"))
                .addAction(PutVarAction.makeVec3(ACTIVE.activation, " (normalize(look + vec3(0, -get_y(look), 0))) * speed ", "direction"))
                .addAction(SetMotionAction.make(ACTIVE.activation, OWNER.targetGroup, Compiler.compileString(" vec3(get_x(direction), max(0.5, get_y(look) + 0.5), get_z(direction)) ", VEC3.get())))
                .addAction(SpawnParticlesAction.make(ACTIVE.activation, OWNER.targetGroup, ParticleTypes.POOF, INT.get().immediate(4), DOUBLE.get().immediate(0.1)))
                .addAction(PlaySoundAction.make(ACTIVE.activation, OWNER.targetGroup, SoundEvents.ENDER_DRAGON_FLAP, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addTooltip(Component.translatable(Spells.KEY_LEAP_DESC))
        );
        
        CompoundTag childTag = new CompoundTag();
        childTag.putInt("Age", -24000);
        addSpell(Spells.SUMMON_ANIMAL, new Spell(modId, "summon_animal", Spells.KEY_SUMMON_ANIMAL, 4F)
                .addAction(GetEntityPositionDirectionAction.make(ACTIVE.activation, OWNER.targetGroup, "", "direction"))
                .addAction(SimpleManaCheckAction.make(ACTIVE.activation))
                .addAction(SpawnParticlesAction.make(ACTIVE.activation, OWNER.targetGroup, ParticleTypes.EXPLOSION, INT.get().immediate(3), DOUBLE.get().immediate(0.4)))
                .addAction(ActivateAction.make(ACTIVE.activation, "cow"))
                .addAction(SimpleItemCheckAction.make("cow", OWNER.targetGroup, BOOLEAN.get().immediate(true), new ItemStack(Items.BEEF, 8)))
                .addAction(SpawnEntityAction.make("cow", "baby", EntityType.COW, OWNER.targetGroup, Compiler.compileString(" -direction ", VEC3.get()), VEC3.get().immediate(Vec3.ZERO), COMPOUND_TAG.get().immediate(childTag)))
                .addAction(ActivateAction.make(ACTIVE.activation, "chicken"))
                .addAction(SimpleItemCheckAction.make("chicken", OWNER.targetGroup, BOOLEAN.get().immediate(true), new ItemStack(Items.CHICKEN, 8)))
                .addAction(SpawnEntityAction.make("chicken", "baby", EntityType.CHICKEN, OWNER.targetGroup, Compiler.compileString(" -direction ", VEC3.get()), VEC3.get().immediate(Vec3.ZERO), COMPOUND_TAG.get().immediate(childTag)))
                .addAction(ActivateAction.make(ACTIVE.activation, "pig"))
                .addAction(SimpleItemCheckAction.make("pig", OWNER.targetGroup, BOOLEAN.get().immediate(true), new ItemStack(Items.PORKCHOP, 8)))
                .addAction(SpawnEntityAction.make("pig", "baby", EntityType.PIG, OWNER.targetGroup, Compiler.compileString(" -direction ", VEC3.get()), VEC3.get().immediate(Vec3.ZERO), COMPOUND_TAG.get().immediate(childTag)))
                .addAction(ActivateAction.make(ACTIVE.activation, "sheep"))
                .addAction(SimpleItemCheckAction.make("sheep", OWNER.targetGroup, BOOLEAN.get().immediate(true), new ItemStack(Items.MUTTON, 8)))
                .addAction(SpawnEntityAction.make("sheep", "baby", EntityType.SHEEP, OWNER.targetGroup, Compiler.compileString(" -direction ", VEC3.get()), VEC3.get().immediate(Vec3.ZERO), COMPOUND_TAG.get().immediate(childTag)))
                .addTooltip(Component.translatable(Spells.KEY_SUMMON_ANIMAL_DESC))
        );
        
        addSpell(Spells.FIRE_BALL, new Spell(modId, "fire_ball", Spells.KEY_FIRE_BALL, 5F)
                .addParameter(DOUBLE.get(), "speed", 2.5)
                .addAction(SimpleItemCheckAction.make(ACTIVE.activation, OWNER.targetGroup, BOOLEAN.get().immediate(true), new ItemStack(Items.BLAZE_POWDER)))
                .addAction(SimpleManaCheckAction.make(ACTIVE.activation))
                .addAction(ShootAction.make(ACTIVE.activation, OWNER.targetGroup, DOUBLE.get().immediate(3D), DOUBLE.get().immediate(0D), INT.get().immediate(200), "on_block_hit", "on_entity_hit", "on_timeout", ""))
                .addAction(PlaySoundAction.make(ACTIVE.activation, OWNER.targetGroup, SoundEvents.BLAZE_SHOOT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(SourcedDamageAction.make("on_entity_hit", ENTITY_HIT.targetGroup, DOUBLE.get().immediate(2D), PROJECTILE.targetGroup))
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
                .addAction(LookAtTargetAction.make(ACTIVE.activation, OWNER.targetGroup, 25D, 0.5F, ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, "looked_at_block", "looked_at_entity", "looked_at_nothing"))
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
                .addAction(GetEntityPositionDirectionAction.make(ACTIVE.activation, OWNER.targetGroup, "", "direction"))
                .addAction(GetEntityEyePositionAction.make(ACTIVE.activation, OWNER.targetGroup, "position"))
                .addAction(MainhandItemTargetAction.make(ACTIVE.activation, OWNER.targetGroup, "item"))
                .addAction(ItemEqualsActivationAction.make(ACTIVE.activation, "item", "shoot", new ItemStack(Items.ARROW), BOOLEAN.get().immediate(true), INT.get().immediate(1), INT.get().immediate(-1)))
                .addAction(ItemEqualsActivationAction.make(ACTIVE.activation, "item", "potion", new ItemStack(Items.TIPPED_ARROW), BOOLEAN.get().immediate(true), INT.get().immediate(1), INT.get().immediate(-1)))
                .addAction(GetItemTagAction.make("potion", "item", "potion_tag"))
                .addAction(PutVarAction.makeCompoundTag("potion", Compiler.compileString(" put_nbt_string(tag, 'Potion', get_nbt_string(potion_tag, 'Potion')) ", COMPOUND_TAG.get()), "tag"))
                .addAction(ActivateAction.make("potion", "shoot"))
                .addAction(SpawnEntityAction.make("shoot", "arrow", EntityType.ARROW, "position", VEC3.get().reference("direction"), Compiler.compileString(" 3 * direction ", VEC3.get()), COMPOUND_TAG.get().reference("tag")))
                .addAction(PlaySoundAction.make("shoot", OWNER.targetGroup, SoundEvents.ARROW_SHOOT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(ConsumeItemAction.make("shoot", "item", INT.get().immediate(1), OWNER.targetGroup))
                .addAction(ItemEqualsActivationAction.make(ACTIVE.activation, "item", "spectral", new ItemStack(Items.SPECTRAL_ARROW), BOOLEAN.get().immediate(true), INT.get().immediate(1), INT.get().immediate(-1)))
                .addAction(SpawnEntityAction.make("spectral", "arrow", EntityType.SPECTRAL_ARROW, "position", VEC3.get().reference("direction"), Compiler.compileString(" 3 * direction ", VEC3.get()), COMPOUND_TAG.get().reference("tag")))
                .addAction(PlaySoundAction.make("spectral", OWNER.targetGroup, SoundEvents.ARROW_SHOOT, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(1D)))
                .addAction(ConsumeItemAction.make("spectral", "item", INT.get().immediate(1), OWNER.targetGroup))
                .addTooltip(Component.translatable(Spells.KEY_BLOW_ARROW_DESC))
        );
        
        dummy(Spells.HEALTH_BOOST);
        dummy(Spells.MANA_BOOST);
        dummy(Spells.WATER_LEAP);
        dummy(Spells.AQUA_AFFINITY);
        
        //TODO fx, sound, test
        addSpell(Spells.WATER_WHIP, new Spell(modId, "water_whip", Spells.KEY_WATER_WHIP, 5F)
                .addParameter(DOUBLE.get(), "damage", 10.0)
                .addAction(MainhandItemTargetAction.make(ACTIVE.activation, OWNER.targetGroup, "item"))
                .addAction(ItemEqualsActivationAction.make(ACTIVE.activation, "item", "shoot", new ItemStack(Items.WATER_BUCKET), BOOLEAN.get().immediate(true), INT.get().immediate(1), INT.get().immediate(-1)))
                .addAction(ActivateAction.make(ACTIVE.activation, "offhand"))
                .addAction(DeactivateAction.make("shoot", "offhand"))
                .addAction(SimpleItemCheckAction.make("offhand", OWNER.targetGroup, BOOLEAN.get().immediate(true), new ItemStack(Items.WATER_BUCKET)))
                .addAction(OffhandItemTargetAction.make("offhand", OWNER.targetGroup, "item"))
                .addAction(ItemEqualsActivationAction.make("offhand", "item", "shoot", new ItemStack(Items.WATER_BUCKET), BOOLEAN.get().immediate(true), INT.get().immediate(1), INT.get().immediate(-1)))
                .addAction(GetItemAttributesAction.make("shoot", "item", "amount", "damage", "item_tag"))
                .addAction(OverrideItemAction.make("shoot", "item", INT.get().reference("amount"), INT.get().reference("amount"), COMPOUND_TAG.get().reference("item_tag"), Items.BUCKET))
                .addAction(GetEntityUUIDAction.make("shoot", OWNER.targetGroup, "uuid"))
                .addAction(PutVarAction.makeCompoundTag("shoot", new CompoundTag(), "tag"))
                .addAction(PutVarAction.makeCompoundTag("shoot", Compiler.compileString(" put_nbt_uuid(tag, 'Owner', uuid) ", COMPOUND_TAG.get()), "tag"))
                .addAction(ShootAction.make("shoot", OWNER.targetGroup, DOUBLE.get().immediate(1D), DOUBLE.get().immediate(0D), INT.get().immediate(100), "return", "on_entity_hit", "return", "projectile"))
                .addAction(ApplyEntityTagAction.make("shoot", "projectile", COMPOUND_TAG.get().reference("tag")))
                .addAction(DamageAction.make("on_entity_hit", ENTITY_HIT.targetGroup, DOUBLE.get().immediate(10D)))
                .addAction(ActivateAction.make("on_entity_hit", "return"))
                .addTooltip(Component.translatable(Spells.KEY_BLOW_ARROW_DESC))
        );
        
        dummy(Spells.POTION_SHOT);
        dummy(Spells.FROST_WALKER);
        dummy(Spells.JUMP);
        dummy(Spells.MANA_SOLES);
        dummy(Spells.FIRE_CHARGE);
        dummy(Spells.PRESSURIZE);
        dummy(Spells.INSTANT_MINE);
        dummy(Spells.SPIT_METAL);
        dummy(Spells.FLAMETHROWER);
        dummy(Spells.LAVA_WALKER);
        dummy(Spells.SILENCE_TARGET);
        dummy(Spells.RANDOM_TELEPORT);
        dummy(Spells.FORCED_TELEPORT);
        dummy(Spells.TELEPORT);
        dummy(Spells.LIGHTNING_STRIKE);
        dummy(Spells.DRAIN_FLAME);
        dummy(Spells.GROWTH);
        dummy(Spells.GHAST);
        dummy(Spells.ENDER_ARMY);
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
