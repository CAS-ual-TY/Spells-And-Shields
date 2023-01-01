package de.cas_ual_ty.spells.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.action.attribute.GetEntityPositionDirectionAction;
import de.cas_ual_ty.spells.spell.action.effect.CheckBurnManaAction;
import de.cas_ual_ty.spells.spell.action.effect.ResetFallDistanceAction;
import de.cas_ual_ty.spells.spell.action.effect.SetMotionAction;
import de.cas_ual_ty.spells.spell.action.variable.MappedUnaryVarAction;
import de.cas_ual_ty.spells.spell.action.variable.PutVarAction;
import de.cas_ual_ty.spells.spell.compiler.Compiler;
import de.cas_ual_ty.spells.spell.compiler.UnaryOperation;
import de.cas_ual_ty.spells.spell.context.BuiltinActivations;
import de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
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
                .addAction(new CheckBurnManaAction(SpellActionTypes.CHECK_BURN_MANA.get(), BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, CtxVarTypes.DOUBLE.get().refDyn("mana_cost")))
                // speed <- 2.5
                .addAction(new PutVarAction<>(SpellActionTypes.PUT_DOUBLE.get(), BuiltinActivations.ACTIVE.activation, CtxVarTypes.DOUBLE.get().refImm(2.5), "speed", CtxVarTypes.DOUBLE.get()))
                // look <- look vector
                .addAction(new GetEntityPositionDirectionAction(SpellActionTypes.GET_POSITION_DIRECTION.get(), BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, "", "look"))
                // y <- vector.y = look.y
                .addAction(new MappedUnaryVarAction(SpellActionTypes.GET_Y.get(), BuiltinActivations.ACTIVE.activation, "look", "y", UnaryOperation.GET_Y))
                // lookn <- normalize(look)
                .addAction(new PutVarAction<>(SpellActionTypes.PUT_VEC3.get(), BuiltinActivations.ACTIVE.activation, Compiler.compile("(normalize(look)+vec3(0,-y,0))*speed", CtxVarTypes.VEC3.get()), "direction", CtxVarTypes.VEC3.get()))
                .addAction(new ResetFallDistanceAction(SpellActionTypes.RESET_FALL_DISTANCE.get(), BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup))
                .addAction(new SetMotionAction(SpellActionTypes.SET_MOTION.get(), BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, Compiler.compile("vec3(get_x(direction),max(0.5,y+0.5),get_z(direction))", CtxVarTypes.VEC3.get())))
                .addTooltip(Component.translatable(Spells.KEY_LEAP_DESC))
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
