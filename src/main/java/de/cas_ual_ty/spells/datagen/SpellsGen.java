package de.cas_ual_ty.spells.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.action.attribute.GetEntityPositionDirectionAction;
import de.cas_ual_ty.spells.spell.action.attribute.GetEntityTagAction;
import de.cas_ual_ty.spells.spell.action.control.ActivateAction;
import de.cas_ual_ty.spells.spell.action.control.ItemEqualsActivationAction;
import de.cas_ual_ty.spells.spell.action.control.SimpleManaCheckAction;
import de.cas_ual_ty.spells.spell.action.effect.*;
import de.cas_ual_ty.spells.spell.action.fx.PlaySoundAction;
import de.cas_ual_ty.spells.spell.action.fx.SpawnParticlesAction;
import de.cas_ual_ty.spells.spell.action.item.SimpleItemCheckAction;
import de.cas_ual_ty.spells.spell.action.target.HomeAction;
import de.cas_ual_ty.spells.spell.action.target.LookAtTargetAction;
import de.cas_ual_ty.spells.spell.action.target.MainhandItemTargetAction;
import de.cas_ual_ty.spells.spell.action.target.ShootAction;
import de.cas_ual_ty.spells.spell.action.variable.PutVarAction;
import de.cas_ual_ty.spells.spell.compiler.Compiler;
import de.cas_ual_ty.spells.spell.context.BuiltinActivations;
import de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups;
import de.cas_ual_ty.spells.spell.context.BuiltinVariables;
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
        addSpell(Spells.LEAP, new Spell(modId, "leap", Spells.KEY_LEAP, 2.5F)
                .addParameter(CtxVarTypes.DOUBLE.get(), "speed", 2.5)
                .addAction(SimpleManaCheckAction.make(BuiltinActivations.ACTIVE.activation))
                .addAction(ResetFallDistanceAction.make(BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup))
                .addAction(GetEntityPositionDirectionAction.make(BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, "", "look"))
                .addAction(PutVarAction.makeVec3(BuiltinActivations.ACTIVE.activation, " (normalize(look + vec3(0, -get_y(look), 0))) * speed ", "direction"))
                .addAction(SetMotionAction.make(BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, Compiler.compileString(" vec3(get_x(direction), max(0.5, get_y(look) + 0.5), get_z(direction)) ", CtxVarTypes.VEC3.get())))
                .addAction(SpawnParticlesAction.make(BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, ParticleTypes.POOF, CtxVarTypes.INT.get().immediate(4), CtxVarTypes.DOUBLE.get().immediate(0.1)))
                .addAction(PlaySoundAction.make(BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, SoundEvents.ENDER_DRAGON_FLAP, CtxVarTypes.DOUBLE.get().immediate(1D), CtxVarTypes.DOUBLE.get().immediate(1D)))
                .addTooltip(Component.translatable(Spells.KEY_LEAP_DESC))
        );
        
        CompoundTag childTag = new CompoundTag();
        childTag.putInt("Age", -24000);
        addSpell(Spells.SUMMON_ANIMAL, new Spell(modId, "summon_animal", Spells.KEY_SUMMON_ANIMAL, 4F)
                .addAction(GetEntityPositionDirectionAction.make(BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, "", "direction"))
                .addAction(SimpleManaCheckAction.make(BuiltinActivations.ACTIVE.activation))
                .addAction(SpawnParticlesAction.make(BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, ParticleTypes.EXPLOSION, CtxVarTypes.INT.get().immediate(3), CtxVarTypes.DOUBLE.get().immediate(0.4)))
                .addAction(ActivateAction.make(BuiltinActivations.ACTIVE.activation, "cow"))
                .addAction(SimpleItemCheckAction.make("cow", BuiltinTargetGroups.OWNER.targetGroup, CtxVarTypes.BOOLEAN.get().immediate(true), new ItemStack(Items.BEEF, 8)))
                .addAction(SpawnEntityAction.make("cow", "baby", EntityType.COW, BuiltinTargetGroups.OWNER.targetGroup, Compiler.compileString(" -direction ", CtxVarTypes.VEC3.get()), CtxVarTypes.VEC3.get().immediate(Vec3.ZERO), CtxVarTypes.COMPOUND_TAG.get().immediate(childTag)))
                .addAction(ActivateAction.make(BuiltinActivations.ACTIVE.activation, "chicken"))
                .addAction(SimpleItemCheckAction.make("chicken", BuiltinTargetGroups.OWNER.targetGroup, CtxVarTypes.BOOLEAN.get().immediate(true), new ItemStack(Items.CHICKEN, 8)))
                .addAction(SpawnEntityAction.make("chicken", "baby", EntityType.CHICKEN, BuiltinTargetGroups.OWNER.targetGroup, Compiler.compileString(" -direction ", CtxVarTypes.VEC3.get()), CtxVarTypes.VEC3.get().immediate(Vec3.ZERO), CtxVarTypes.COMPOUND_TAG.get().immediate(childTag)))
                .addAction(ActivateAction.make(BuiltinActivations.ACTIVE.activation, "pig"))
                .addAction(SimpleItemCheckAction.make("pig", BuiltinTargetGroups.OWNER.targetGroup, CtxVarTypes.BOOLEAN.get().immediate(true), new ItemStack(Items.PORKCHOP, 8)))
                .addAction(SpawnEntityAction.make("pig", "baby", EntityType.PIG, BuiltinTargetGroups.OWNER.targetGroup, Compiler.compileString(" -direction ", CtxVarTypes.VEC3.get()), CtxVarTypes.VEC3.get().immediate(Vec3.ZERO), CtxVarTypes.COMPOUND_TAG.get().immediate(childTag)))
                .addAction(ActivateAction.make(BuiltinActivations.ACTIVE.activation, "sheep"))
                .addAction(SimpleItemCheckAction.make("sheep", BuiltinTargetGroups.OWNER.targetGroup, CtxVarTypes.BOOLEAN.get().immediate(true), new ItemStack(Items.MUTTON, 8)))
                .addAction(SpawnEntityAction.make("sheep", "baby", EntityType.SHEEP, BuiltinTargetGroups.OWNER.targetGroup, Compiler.compileString(" -direction ", CtxVarTypes.VEC3.get()), CtxVarTypes.VEC3.get().immediate(Vec3.ZERO), CtxVarTypes.COMPOUND_TAG.get().immediate(childTag)))
                .addTooltip(Component.translatable(Spells.KEY_SUMMON_ANIMAL_DESC))
        );
        
        addSpell(Spells.FIRE_BALL, new Spell(modId, "fire_ball", Spells.KEY_FIRE_BALL, 5F)
                .addParameter(CtxVarTypes.DOUBLE.get(), "speed", 2.5)
                .addAction(SimpleItemCheckAction.make(BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, CtxVarTypes.BOOLEAN.get().immediate(true), new ItemStack(Items.BLAZE_POWDER)))
                .addAction(SimpleManaCheckAction.make(BuiltinActivations.ACTIVE.activation))
                .addAction(ShootAction.make(BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, CtxVarTypes.DOUBLE.get().immediate(3D), CtxVarTypes.DOUBLE.get().immediate(0D), CtxVarTypes.INT.get().immediate(200), "on_block_hit", "on_entity_hit", "on_timeout"))
                .addAction(PlaySoundAction.make(BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, SoundEvents.BLAZE_SHOOT, CtxVarTypes.DOUBLE.get().immediate(1D), CtxVarTypes.DOUBLE.get().immediate(1D)))
                .addAction(SourcedDamageAction.make("on_entity_hit", BuiltinTargetGroups.ENTITY_HIT.targetGroup, CtxVarTypes.DOUBLE.get().immediate(2D), BuiltinTargetGroups.PROJECTILE.targetGroup))
                .addAction(ActivateAction.make("on_entity_hit", "fx"))
                .addAction(ActivateAction.make("on_block_hit", "fx"))
                .addAction(ActivateAction.make("on_timeout", "fx"))
                .addAction(PlaySoundAction.make("fx", BuiltinTargetGroups.HIT_POSITION.targetGroup, SoundEvents.BLAZE_SHOOT, CtxVarTypes.DOUBLE.get().immediate(1D), CtxVarTypes.DOUBLE.get().immediate(1D)))
                .addAction(SpawnParticlesAction.make("fx", BuiltinTargetGroups.HIT_POSITION.targetGroup, ParticleTypes.LARGE_SMOKE, CtxVarTypes.INT.get().immediate(3), CtxVarTypes.DOUBLE.get().immediate(0.2)))
                .addAction(SpawnParticlesAction.make("fx", BuiltinTargetGroups.HIT_POSITION.targetGroup, ParticleTypes.LAVA, CtxVarTypes.INT.get().immediate(1), CtxVarTypes.DOUBLE.get().immediate(0.2)))
                .addAction(SpawnParticlesAction.make("fx", BuiltinTargetGroups.HIT_POSITION.targetGroup, ParticleTypes.SMOKE, CtxVarTypes.INT.get().immediate(2), CtxVarTypes.DOUBLE.get().immediate(0.1)))
                .addAction(SpawnParticlesAction.make("fx", BuiltinTargetGroups.HIT_POSITION.targetGroup, ParticleTypes.FLAME, CtxVarTypes.INT.get().immediate(2), CtxVarTypes.DOUBLE.get().immediate(0.1)))
                .addTooltip(Component.translatable(Spells.KEY_FIRE_BALL_DESC))
        );
        
        //TODO Blast Smelt
        
        addSpell(Spells.TRANSFER_MANA, new Spell(modId, "transfer_mana", Spells.KEY_TRANSFER_MANA, 4F)
                .addParameter(CtxVarTypes.DOUBLE.get(), "speed", 2.5)
                .addAction(LookAtTargetAction.make(BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, 25D, 0.5F, ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, "looked_at_block", "looked_at_entity", "looked_at_nothing"))
                .addAction(SimpleManaCheckAction.make("looked_at_entity"))
                .addAction(HomeAction.make("looked_at_entity", BuiltinTargetGroups.OWNER.targetGroup, BuiltinTargetGroups.ENTITY_HIT.targetGroup, CtxVarTypes.DOUBLE.get().immediate(3D), CtxVarTypes.INT.get().immediate(200), "on_block_hit", "on_entity_hit", "on_timeout"))
                .addAction(PlaySoundAction.make("looked_at_entity", BuiltinTargetGroups.OWNER.targetGroup, SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, CtxVarTypes.DOUBLE.get().immediate(1D), CtxVarTypes.DOUBLE.get().immediate(1D)))
                .addAction(ReplenishManaAction.make("on_entity_hit", BuiltinTargetGroups.ENTITY_HIT.targetGroup, CtxVarTypes.DOUBLE.get().reference(BuiltinVariables.MANA_COST.name)))
                .addAction(ActivateAction.make("on_entity_hit", "fx"))
                .addAction(ActivateAction.make("on_block_hit", "fx"))
                .addAction(ActivateAction.make("on_timeout", "fx"))
                .addAction(PlaySoundAction.make("fx", BuiltinTargetGroups.HIT_POSITION.targetGroup, SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, CtxVarTypes.DOUBLE.get().immediate(1D), CtxVarTypes.DOUBLE.get().immediate(1D)))
                .addAction(SpawnParticlesAction.make("fx", BuiltinTargetGroups.HIT_POSITION.targetGroup, ParticleTypes.BUBBLE, CtxVarTypes.INT.get().immediate(3), CtxVarTypes.DOUBLE.get().immediate(0.2)))
                .addAction(SpawnParticlesAction.make("fx", BuiltinTargetGroups.HIT_POSITION.targetGroup, ParticleTypes.POOF, CtxVarTypes.INT.get().immediate(2), CtxVarTypes.DOUBLE.get().immediate(0.2)))
                .addTooltip(Component.translatable(Spells.KEY_TRANSFER_MANA_DESC))
        );
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
