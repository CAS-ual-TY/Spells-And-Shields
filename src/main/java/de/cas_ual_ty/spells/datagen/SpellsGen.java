package de.cas_ual_ty.spells.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.action.attribute.GetEntityPositionDirectionAction;
import de.cas_ual_ty.spells.spell.action.effect.CheckBurnManaAction;
import de.cas_ual_ty.spells.spell.action.effect.DamageAction;
import de.cas_ual_ty.spells.spell.action.effect.ResetFallDistanceAction;
import de.cas_ual_ty.spells.spell.action.effect.SetMotionAction;
import de.cas_ual_ty.spells.spell.action.variable.*;
import de.cas_ual_ty.spells.spell.compiler.BinaryOperation;
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
        addSpell(Spells.TEST, new Spell(modId, "default_fallback", Spells.KEY_TEST, 2F)
                .addTooltip(Component.literal("Description here // this is a debug spell"))
                .addTooltip(Component.literal("You damage yourself using it"))
                .addTooltip(Component.translatable("translated.description.key.line1"))
                .addTooltip(Component.translatable("translated.description.key.line2"))
                .addAction(new DamageAction(SpellActionTypes.DAMAGE.get(), BuiltinActivations.ACTIVE, BuiltinTargetGroups.OWNER, 5D))
        );
        
        addSpell(Spells.TEST2, new Spell(modId, "default_fallback", Spells.KEY_TEST2, 2F)
                .addAction(new PutVarAction<>(SpellActionTypes.PUT_DOUBLE.get(), BuiltinActivations.ACTIVE.activation, CtxVarTypes.DOUBLE.get().refImm(1D), "var1", CtxVarTypes.DOUBLE.get()))
                .addAction(new PutVarAction<>(SpellActionTypes.PUT_INT.get(), BuiltinActivations.ACTIVE.activation, CtxVarTypes.INT.get().refImm(30), "var2", CtxVarTypes.INT.get()))
                .addAction(new DamageAction(SpellActionTypes.DAMAGE.get(), BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, Compiler.compile("max(var1,var2)", CtxVarTypes.DOUBLE.get())))
        );
        
        addSpell(Spells.LEAP, new Spell(modId, "leap", Spells.KEY_LEAP, 2.5F)
                .addAction(new CheckBurnManaAction(SpellActionTypes.CHECK_BURN_MANA.get(), BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, CtxVarTypes.DOUBLE.get().refDyn("mana_cost")))
                // vector <- look
                .addAction(new GetEntityPositionDirectionAction(SpellActionTypes.GET_POSITION_DIRECTION.get(), BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, "", "vector"))
                // y <- vector.y = look.y
                .addAction(new MappedUnaryVarAction(SpellActionTypes.GET_Y.get(), BuiltinActivations.ACTIVE.activation, "vector", "y", UnaryOperation.GET_Y))
                // neg <- -1
                .addAction(new PutVarAction<>(SpellActionTypes.PUT_DOUBLE.get(), BuiltinActivations.ACTIVE.activation, CtxVarTypes.DOUBLE.get().refImm(-1D), "neg", CtxVarTypes.DOUBLE.get()))
                // y2 <- y * neg = -y
                .addAction(new MappedBinaryVarAction(SpellActionTypes.MUL.get(), BuiltinActivations.ACTIVE.activation, "y", "neg", "y2", BinaryOperation.ADD))
                // vector2 <- (0, y2, 0) = (0, -look.y, 0)
                .addAction(new MakeVectorAction(SpellActionTypes.MAKE_VEC3.get(), BuiltinActivations.ACTIVE.activation, CtxVarTypes.DOUBLE.get().refImm(0D), CtxVarTypes.DOUBLE.get().refDyn("y2"), CtxVarTypes.DOUBLE.get().refImm(0D), "vector2"))
                // vector3 <- vector + vector2 = (look.x, 0, look.y)
                .addAction(new MappedBinaryVarAction(SpellActionTypes.ADD.get(), BuiltinActivations.ACTIVE.activation, "vector", "vector2", "vector3", BinaryOperation.ADD))
                // vector3n <- vector3.normalized
                .addAction(SimpleUnaryVarAction.makeInstance(SpellActionTypes.NORMALIZE.get(), BuiltinActivations.ACTIVE.activation, "vector3", "vector3n"))
                // speed <- 2.5
                .addAction(new PutVarAction<>(SpellActionTypes.PUT_DOUBLE.get(), BuiltinActivations.ACTIVE.activation, CtxVarTypes.DOUBLE.get().refImm(2.5D), "speed", CtxVarTypes.DOUBLE.get()))
                // vector4 <- vector3.scale(speed)
                .addAction(SimpleBinaryVarAction.makeInstance(SpellActionTypes.SCALE.get(), BuiltinActivations.ACTIVE.activation, "vector3n", "speed", "vector4"))
                // half <- 0.5
                .addAction(new PutVarAction<>(SpellActionTypes.PUT_DOUBLE.get(), BuiltinActivations.ACTIVE.activation, CtxVarTypes.DOUBLE.get().refImm(0.5D), "half", CtxVarTypes.DOUBLE.get()))
                // y3 <- y + half
                .addAction(new MappedBinaryVarAction(SpellActionTypes.ADD.get(), BuiltinActivations.ACTIVE.activation, "y", "half", "y3", BinaryOperation.ADD))
                // y4 <- max(half, y3) = max(0.5, look.y + 0.5)
                .addAction(new MappedBinaryVarAction(SpellActionTypes.MAX.get(), BuiltinActivations.ACTIVE.activation, "y3", "half", "y4", BinaryOperation.ADD))
                // vector5 <- (0, y4, 0)
                .addAction(new MakeVectorAction(SpellActionTypes.MAKE_VEC3.get(), BuiltinActivations.ACTIVE.activation, CtxVarTypes.DOUBLE.get().refImm(0D), CtxVarTypes.DOUBLE.get().refDyn("y4"), CtxVarTypes.DOUBLE.get().refImm(0D), "vector5"))
                // vector6 <- vector4 + vector5 = (look.x * speed, y4, look.z * speed)
                .addAction(new MappedBinaryVarAction(SpellActionTypes.ADD.get(), BuiltinActivations.ACTIVE.activation, "vector4", "vector5", "vector6", BinaryOperation.ADD))
                .addAction(new ResetFallDistanceAction(SpellActionTypes.RESET_FALL_DISTANCE.get(), BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup))
                .addAction(new SetMotionAction(SpellActionTypes.SET_MOTION.get(), BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, CtxVarTypes.VEC3.get().refDyn("vector6")))
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
