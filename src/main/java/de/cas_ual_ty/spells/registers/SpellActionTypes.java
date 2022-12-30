package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.SyncedSpellActionType;
import de.cas_ual_ty.spells.spell.action.attribute.GetEntityPositionDirectionAction;
import de.cas_ual_ty.spells.spell.action.effect.*;
import de.cas_ual_ty.spells.spell.action.target.CopyTargetsAction;
import de.cas_ual_ty.spells.spell.action.target.LookAtTargetAction;
import de.cas_ual_ty.spells.spell.action.target.PickTargetAction;
import de.cas_ual_ty.spells.spell.action.variable.*;
import de.cas_ual_ty.spells.spell.compiler.BinaryOperation;
import de.cas_ual_ty.spells.spell.compiler.TernaryOperation;
import de.cas_ual_ty.spells.spell.compiler.UnaryOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

import static de.cas_ual_ty.spells.SpellsAndShields.MOD_ID;

public class SpellActionTypes
{
    public static Supplier<IForgeRegistry<SpellActionType<?>>> REGISTRY;
    private static final DeferredRegister<SpellActionType<?>> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(MOD_ID, "spell_actions"), MOD_ID);
    
    // effects
    public static final RegistryObject<SpellActionType<DamageAction>> DAMAGE = DEFERRED_REGISTER.register("damage", () -> new SpellActionType<>(DamageAction::new, DamageAction::makeCodec));
    public static final RegistryObject<SpellActionType<ResetFallDistanceAction>> RESET_FALL_DISTANCE = DEFERRED_REGISTER.register("reset_fall_distance", () -> new SpellActionType<>(ResetFallDistanceAction::new, ResetFallDistanceAction::makeCodec));
    public static final RegistryObject<SpellActionType<SetMotionAction>> SET_MOTION = DEFERRED_REGISTER.register("set_motion", () -> new SyncedSpellActionType<>(SetMotionAction::new, SetMotionAction::makeCodec, SetMotionAction.ClientAction::new));
    public static final RegistryObject<SpellActionType<BurnManaAction>> BURN_MANA = DEFERRED_REGISTER.register("burn_mana", () -> new SpellActionType<>(BurnManaAction::new, BurnManaAction::makeCodec));
    public static final RegistryObject<SpellActionType<CheckBurnManaAction>> CHECK_BURN_MANA = DEFERRED_REGISTER.register("check_burn_mana", () -> new SpellActionType<>(CheckBurnManaAction::new, CheckBurnManaAction::makeCodec));
    
    //target
    public static final RegistryObject<SpellActionType<CopyTargetsAction>> COPY_TARGETS = DEFERRED_REGISTER.register("copy_targets", () -> new SpellActionType<>(CopyTargetsAction::new, CopyTargetsAction::makeCodec));
    public static final RegistryObject<SpellActionType<PickTargetAction>> PICK_TARGET = DEFERRED_REGISTER.register("pick_target", () -> new SpellActionType<>(PickTargetAction::new, PickTargetAction::makeCodec2));
    public static final RegistryObject<SpellActionType<LookAtTargetAction>> LOOK_AT_TARGET = DEFERRED_REGISTER.register("look_at_target", () -> new SpellActionType<>(LookAtTargetAction::new, LookAtTargetAction::makeCodec));
    
    //variable
    public static final RegistryObject<SpellActionType<PutVarAction<Integer>>> PUT_INT = DEFERRED_REGISTER.register("put_int", () -> PutVarAction.makeType(CtxVarTypes.INT));
    public static final RegistryObject<SpellActionType<PutVarAction<Double>>> PUT_DOUBLE = DEFERRED_REGISTER.register("put_double", () -> PutVarAction.makeType(CtxVarTypes.DOUBLE));
    public static final RegistryObject<SpellActionType<PutVarAction<Vec3>>> PUT_VEC3 = DEFERRED_REGISTER.register("put_vec3", () -> PutVarAction.makeType(CtxVarTypes.VEC3));
    public static final RegistryObject<SpellActionType<PutVarAction<BlockPos>>> PUT_BLOCK_POS = DEFERRED_REGISTER.register("put_block_pos", () -> PutVarAction.makeType(CtxVarTypes.BLOCK_POS));
    public static final RegistryObject<SpellActionType<MakeVectorAction>> MAKE_VEC3 = DEFERRED_REGISTER.register("make_vec3", () -> new SpellActionType<>(MakeVectorAction::new, MakeVectorAction::makeCodec));
    
    //variable / mapped binary
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> ADD = DEFERRED_REGISTER.register("add", () -> MappedBinaryVarAction.makeType(BinaryOperation.ADD));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> SUB = DEFERRED_REGISTER.register("sub", () -> MappedBinaryVarAction.makeType(BinaryOperation.SUB));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> MUL = DEFERRED_REGISTER.register("mul", () -> MappedBinaryVarAction.makeType(BinaryOperation.MUL));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> DIV = DEFERRED_REGISTER.register("div", () -> MappedBinaryVarAction.makeType(BinaryOperation.DIV));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> MIN = DEFERRED_REGISTER.register("min", () -> MappedBinaryVarAction.makeType(BinaryOperation.MIN));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> MAX = DEFERRED_REGISTER.register("max", () -> MappedBinaryVarAction.makeType(BinaryOperation.MAX));
    
    //variable / simple binary
    public static final RegistryObject<SpellActionType<SimpleBinaryVarAction<Vec3, Double, Vec3>>> SCALE = DEFERRED_REGISTER.register("scale", () -> SimpleBinaryVarAction.makeType(CtxVarTypes.VEC3, CtxVarTypes.DOUBLE, CtxVarTypes.VEC3, (x, y) -> x.scale(y)));
    
    //variable / mapped unary
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> SQRT = DEFERRED_REGISTER.register("sqrt", () -> MappedUnaryVarAction.makeType(UnaryOperation.SQRT));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> GET_X = DEFERRED_REGISTER.register("x", () -> MappedUnaryVarAction.makeType(UnaryOperation.GET_X));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> GET_Y = DEFERRED_REGISTER.register("y", () -> MappedUnaryVarAction.makeType(UnaryOperation.GET_Y));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> GET_Z = DEFERRED_REGISTER.register("z", () -> MappedUnaryVarAction.makeType(UnaryOperation.GET_Z));
    
    //variable / simple unary
    public static final RegistryObject<SpellActionType<SimpleUnaryVarAction<Vec3, Double>>> LENGTH = DEFERRED_REGISTER.register("length", () -> SimpleUnaryVarAction.makeType(CtxVarTypes.VEC3, CtxVarTypes.DOUBLE, x -> x.length()));
    public static final RegistryObject<SpellActionType<SimpleUnaryVarAction<Vec3, Vec3>>> NORMALIZE = DEFERRED_REGISTER.register("normalize", () -> SimpleUnaryVarAction.makeType(CtxVarTypes.VEC3, CtxVarTypes.VEC3, x -> x.normalize()));
    public static final RegistryObject<SpellActionType<SimpleUnaryVarAction<Double, Integer>>> ROUND = DEFERRED_REGISTER.register("round", () -> SimpleUnaryVarAction.makeType(CtxVarTypes.DOUBLE, CtxVarTypes.INT, x -> (int) Math.round(x)));
    public static final RegistryObject<SpellActionType<SimpleUnaryVarAction<Double, Integer>>> FLOOR = DEFERRED_REGISTER.register("floor", () -> SimpleUnaryVarAction.makeType(CtxVarTypes.DOUBLE, CtxVarTypes.INT, x -> (int) Math.floor(x)));
    public static final RegistryObject<SpellActionType<SimpleUnaryVarAction<Double, Integer>>> CEIL = DEFERRED_REGISTER.register("ceil", () -> SimpleUnaryVarAction.makeType(CtxVarTypes.DOUBLE, CtxVarTypes.INT, x -> (int) Math.ceil(x)));
    
    //attribute
    public static final RegistryObject<SpellActionType<GetEntityPositionDirectionAction>> GET_POSITION_DIRECTION = DEFERRED_REGISTER.register("get_position_direction", () -> new SpellActionType<>(GetEntityPositionDirectionAction::new, GetEntityPositionDirectionAction::makeCodec));
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpellActionTypes::newRegistry);
        DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpellActionTypes::setup);
    }
    
    private static void newRegistry(NewRegistryEvent event)
    {
        REGISTRY = event.create(new RegistryBuilder<SpellActionType<?>>().setMaxID(1024).setName(new ResourceLocation(MOD_ID, "spell_actions")));
    }
    
    private static void setup(FMLCommonSetupEvent event)
    {
        UnaryOperation.NEGATE.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x) -> -x)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x) -> -x)
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), (x) -> x.reverse());
        
        UnaryOperation.SQRT.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x) -> Math.sqrt(Math.abs(x)));
        
        UnaryOperation.GET_X.register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), (x) -> x.x())
                .register(CtxVarTypes.BLOCK_POS.get(), CtxVarTypes.INT.get(), (x) -> x.getX());
        
        UnaryOperation.GET_Y.register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), (x) -> x.y())
                .register(CtxVarTypes.BLOCK_POS.get(), CtxVarTypes.INT.get(), (x) -> x.getY());
        
        UnaryOperation.GET_Z.register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), (x) -> x.z())
                .register(CtxVarTypes.BLOCK_POS.get(), CtxVarTypes.INT.get(), (x) -> x.getZ());
        
        BinaryOperation.ADD.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> x + y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> x + y)
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), (x, y) -> x.add(y));
        
        BinaryOperation.SUB.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> x - y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> x - y)
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), (x, y) -> x.subtract(y));
        
        BinaryOperation.MUL.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> x * y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> x * y)
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.VEC3.get(), (x, y) -> x.scale(y));
        
        BinaryOperation.DIV.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> y == 0 ? 0 : x / y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> y == 0 ? 0 : x / y)
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.VEC3.get(), (x, y) -> y == 0 ? Vec3.ZERO : x.scale(x.length() / y));
        
        BinaryOperation.MIN.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> Math.min(x, y))
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> Math.min(x, y));
        
        BinaryOperation.MAX.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> Math.max(x, y))
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> Math.max(x, y));
    
        TernaryOperation.VEC3.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.VEC3.get(), (x, y, z) -> new Vec3(x, y, z));
    
        TernaryOperation.BLOCK_POS.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.BLOCK_POS.get(), (x, y, z) -> new BlockPos(x, y, z));
    }
}
