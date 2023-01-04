package de.cas_ual_ty.spells.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.action.attribute.GetEntityPositionDirectionAction;
import de.cas_ual_ty.spells.spell.action.control.ActivateAction;
import de.cas_ual_ty.spells.spell.action.effect.*;
import de.cas_ual_ty.spells.spell.action.target.ShootAction;
import de.cas_ual_ty.spells.spell.action.variable.PutVarAction;
import de.cas_ual_ty.spells.spell.compiler.Compiler;
import de.cas_ual_ty.spells.spell.context.BuiltinActivations;
import de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
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
                .addParameter(new CtxVar<>(CtxVarTypes.DOUBLE.get(), "speed", 2.5))
                .addAction(new SimpleManaCheck(SpellActionTypes.SIMPLE_MANA_CHECK.get(), BuiltinActivations.ACTIVE.activation))
                .addAction(new ResetFallDistanceAction(SpellActionTypes.RESET_FALL_DISTANCE.get(), BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup))
                .addAction(new GetEntityPositionDirectionAction(SpellActionTypes.GET_POSITION_DIRECTION.get(), BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, "", "look"))
                .addAction(new PutVarAction<>(SpellActionTypes.PUT_VEC3.get(), BuiltinActivations.ACTIVE.activation, Compiler.compileString(" (normalize(look + vec3(0, -get_y(look), 0))) * speed ", CtxVarTypes.VEC3.get()), "direction", CtxVarTypes.VEC3.get()))
                .addAction(new SetMotionAction(SpellActionTypes.SET_MOTION.get(), BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, Compiler.compileString(" vec3(get_x(direction), max(0.5, get_y(look) + 0.5), get_z(direction)) ", CtxVarTypes.VEC3.get())))
                .addAction(new SpawnParticlesAction(SpellActionTypes.SPAWN_PARTICLES.get(), BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, ParticleTypes.POOF, CtxVarTypes.INT.get().refImm(4), CtxVarTypes.DOUBLE.get().refImm(0.1)))
                .addAction(new PlaySoundAction(SpellActionTypes.PLAY_SOUND.get(), BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, SoundEvents.ENDER_DRAGON_FLAP, CtxVarTypes.DOUBLE.get().refImm(1D), CtxVarTypes.DOUBLE.get().refImm(1D)))
                .addTooltip(Component.translatable(Spells.KEY_LEAP_DESC))
        );
        
        addSpell(Spells.FIRE_BALL, new Spell(modId, "fire_ball", Spells.KEY_FIRE_BALL, 5F)
                .addParameter(new CtxVar<>(CtxVarTypes.DOUBLE.get(), "speed", 2.5))
                .addAction(new SimpleManaCheck(SpellActionTypes.SIMPLE_MANA_CHECK.get(), BuiltinActivations.ACTIVE.activation))
                .addAction(new ShootAction(SpellActionTypes.SHOOT.get(), BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, CtxVarTypes.DOUBLE.get().refImm(3D), CtxVarTypes.DOUBLE.get().refImm(0D), CtxVarTypes.INT.get().refImm(200), "on_block_hit", "on_entity_hit", "on_timeout"))
                .addAction(new PlaySoundAction(SpellActionTypes.PLAY_SOUND.get(), BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, SoundEvents.BLAZE_SHOOT, CtxVarTypes.DOUBLE.get().refImm(1D), CtxVarTypes.DOUBLE.get().refImm(1D)))
                .addAction(new SourcedDamageAction(SpellActionTypes.SOURCED_DAMAGE.get(), "on_entity_hit", BuiltinTargetGroups.ENTITY_HIT.targetGroup, CtxVarTypes.DOUBLE.get().refImm(2D), BuiltinTargetGroups.PROJECTILE.targetGroup))
                .addAction(new ActivateAction(SpellActionTypes.ACTIVATE.get(), "on_entity_hit", "fx"))
                .addAction(new ActivateAction(SpellActionTypes.ACTIVATE.get(), "on_block_hit", "fx"))
                .addAction(new ActivateAction(SpellActionTypes.ACTIVATE.get(), "on_timeout", "fx"))
                .addAction(new PlaySoundAction(SpellActionTypes.PLAY_SOUND.get(), "fx", BuiltinTargetGroups.HIT_POSITION.targetGroup, SoundEvents.BLAZE_SHOOT, CtxVarTypes.DOUBLE.get().refImm(1D), CtxVarTypes.DOUBLE.get().refImm(1D)))
                .addAction(new SpawnParticlesAction(SpellActionTypes.SPAWN_PARTICLES.get(), "fx", BuiltinTargetGroups.HIT_POSITION.targetGroup, ParticleTypes.LARGE_SMOKE, CtxVarTypes.INT.get().refImm(3), CtxVarTypes.DOUBLE.get().refImm(0.2)))
                .addAction(new SpawnParticlesAction(SpellActionTypes.SPAWN_PARTICLES.get(), "fx", BuiltinTargetGroups.HIT_POSITION.targetGroup, ParticleTypes.LAVA, CtxVarTypes.INT.get().refImm(1), CtxVarTypes.DOUBLE.get().refImm(0.2)))
                .addAction(new SpawnParticlesAction(SpellActionTypes.SPAWN_PARTICLES.get(), "fx", BuiltinTargetGroups.HIT_POSITION.targetGroup, ParticleTypes.SMOKE, CtxVarTypes.INT.get().refImm(2), CtxVarTypes.DOUBLE.get().refImm(0.1)))
                .addAction(new SpawnParticlesAction(SpellActionTypes.SPAWN_PARTICLES.get(), "fx", BuiltinTargetGroups.HIT_POSITION.targetGroup, ParticleTypes.FLAME, CtxVarTypes.INT.get().refImm(2), CtxVarTypes.DOUBLE.get().refImm(0.1)))
                .addTooltip(Component.translatable(Spells.KEY_FIRE_BALL_DESC))
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
