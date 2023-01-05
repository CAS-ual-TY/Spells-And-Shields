package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.SyncedSpellActionType;
import de.cas_ual_ty.spells.spell.action.attribute.GetEntityPositionDirectionAction;
import de.cas_ual_ty.spells.spell.action.control.ActivateAction;
import de.cas_ual_ty.spells.spell.action.control.BooleanActivationAction;
import de.cas_ual_ty.spells.spell.action.control.DeactivateAction;
import de.cas_ual_ty.spells.spell.action.effect.*;
import de.cas_ual_ty.spells.spell.action.target.*;
import de.cas_ual_ty.spells.spell.action.target.filter.TypeFilterAction;
import de.cas_ual_ty.spells.spell.action.variable.MappedBinaryVarAction;
import de.cas_ual_ty.spells.spell.action.variable.MappedTernaryVarAction;
import de.cas_ual_ty.spells.spell.action.variable.MappedUnaryVarAction;
import de.cas_ual_ty.spells.spell.action.variable.PutVarAction;
import de.cas_ual_ty.spells.spell.compiler.BinaryOperation;
import de.cas_ual_ty.spells.spell.compiler.TernaryOperation;
import de.cas_ual_ty.spells.spell.compiler.UnaryOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import java.util.Objects;
import java.util.function.Supplier;

import static de.cas_ual_ty.spells.SpellsAndShields.MOD_ID;

public class SpellActionTypes
{
    public static Supplier<IForgeRegistry<SpellActionType<?>>> REGISTRY;
    private static final DeferredRegister<SpellActionType<?>> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(MOD_ID, "spell_actions"), MOD_ID);
    
    // effects
    public static final RegistryObject<SpellActionType<DamageAction>> DAMAGE = DEFERRED_REGISTER.register("damage", () -> new SpellActionType<>(DamageAction::new, DamageAction::makeCodec));
    public static final RegistryObject<SpellActionType<SourcedDamageAction>> SOURCED_DAMAGE = DEFERRED_REGISTER.register("sourced_damage", () -> new SpellActionType<>(SourcedDamageAction::new, SourcedDamageAction::makeCodec));
    public static final RegistryObject<SpellActionType<ResetFallDistanceAction>> RESET_FALL_DISTANCE = DEFERRED_REGISTER.register("reset_fall_distance", () -> new SpellActionType<>(ResetFallDistanceAction::new, ResetFallDistanceAction::makeCodec));
    public static final RegistryObject<SpellActionType<SetMotionAction>> SET_MOTION = DEFERRED_REGISTER.register("set_motion", () -> new SyncedSpellActionType<>(SetMotionAction::new, SetMotionAction::makeCodec, SetMotionAction.ClientAction::new));
    public static final RegistryObject<SpellActionType<BurnManaAction>> BURN_MANA = DEFERRED_REGISTER.register("burn_mana", () -> new SpellActionType<>(BurnManaAction::new, BurnManaAction::makeCodec));
    public static final RegistryObject<SpellActionType<ReplenishManaAction>> REPLENISH_MANA = DEFERRED_REGISTER.register("replenish_mana", () -> new SpellActionType<>(ReplenishManaAction::new, ReplenishManaAction::makeCodec));
    public static final RegistryObject<SpellActionType<SimpleManaCheck>> SIMPLE_MANA_CHECK = DEFERRED_REGISTER.register("simple_mana_check", () -> new SpellActionType<>(SimpleManaCheck::new, SimpleManaCheck::makeCodec));
    public static final RegistryObject<SpellActionType<SpawnParticlesAction>> SPAWN_PARTICLES = DEFERRED_REGISTER.register("spawn_particles", () -> new SpellActionType<>(SpawnParticlesAction::new, SpawnParticlesAction::makeCodec));
    public static final RegistryObject<SpellActionType<PlaySoundAction>> PLAY_SOUND = DEFERRED_REGISTER.register("play_sound", () -> new SpellActionType<>(PlaySoundAction::new, PlaySoundAction::makeCodec));
    
    //target
    public static final RegistryObject<SpellActionType<CopyTargetsAction>> COPY_TARGETS = DEFERRED_REGISTER.register("copy_targets", () -> new SpellActionType<>(CopyTargetsAction::new, CopyTargetsAction::makeCodec));
    public static final RegistryObject<SpellActionType<ClearTargetsAction>> CLEAR_TARGETS = DEFERRED_REGISTER.register("clear_targets", () -> new SpellActionType<>(ClearTargetsAction::new, ClearTargetsAction::makeCodec));
    public static final RegistryObject<SpellActionType<PickTargetAction>> PICK_TARGET = DEFERRED_REGISTER.register("pick_target", () -> new SpellActionType<>(PickTargetAction::new, PickTargetAction::makeCodec2));
    public static final RegistryObject<SpellActionType<LookAtTargetAction>> LOOK_AT_TARGET = DEFERRED_REGISTER.register("look_at_target", () -> new SpellActionType<>(LookAtTargetAction::new, LookAtTargetAction::makeCodec));
    public static final RegistryObject<SpellActionType<ShootAction>> SHOOT = DEFERRED_REGISTER.register("shoot", () -> new SpellActionType<>(ShootAction::new, ShootAction::makeCodec));
    public static final RegistryObject<SpellActionType<HomeAction>> HOME = DEFERRED_REGISTER.register("home", () -> new SpellActionType<>(HomeAction::new, HomeAction::makeCodec));
    
    //target filter
    public static final RegistryObject<SpellActionType<TypeFilterAction>> TYPE_FILTER = DEFERRED_REGISTER.register("type_filter", () -> new SpellActionType<>(TypeFilterAction::new, TypeFilterAction::makeCodec));
    
    //attribute
    public static final RegistryObject<SpellActionType<GetEntityPositionDirectionAction>> GET_POSITION_DIRECTION = DEFERRED_REGISTER.register("get_position_direction", () -> new SpellActionType<>(GetEntityPositionDirectionAction::new, GetEntityPositionDirectionAction::makeCodec));
    
    //control
    public static final RegistryObject<SpellActionType<BooleanActivationAction>> BOOLEAN_ACTIVATION = DEFERRED_REGISTER.register("boolean_activation", () -> new SpellActionType<>(BooleanActivationAction::new, BooleanActivationAction::makeCodec));
    public static final RegistryObject<SpellActionType<ActivateAction>> ACTIVATE = DEFERRED_REGISTER.register("activate", () -> new SpellActionType<>(ActivateAction::new, ActivateAction::makeCodec));
    public static final RegistryObject<SpellActionType<DeactivateAction>> DEACTIVATE = DEFERRED_REGISTER.register("deactivate", () -> new SpellActionType<>(DeactivateAction::new, DeactivateAction::makeCodec));
    
    //variable
    public static final RegistryObject<SpellActionType<PutVarAction<Integer>>> PUT_INT = DEFERRED_REGISTER.register("put_int", () -> PutVarAction.makeType(CtxVarTypes.INT));
    public static final RegistryObject<SpellActionType<PutVarAction<Double>>> PUT_DOUBLE = DEFERRED_REGISTER.register("put_double", () -> PutVarAction.makeType(CtxVarTypes.DOUBLE));
    public static final RegistryObject<SpellActionType<PutVarAction<Vec3>>> PUT_VEC3 = DEFERRED_REGISTER.register("put_vec3", () -> PutVarAction.makeType(CtxVarTypes.VEC3));
    public static final RegistryObject<SpellActionType<PutVarAction<BlockPos>>> PUT_BLOCK_POS = DEFERRED_REGISTER.register("put_block_pos", () -> PutVarAction.makeType(CtxVarTypes.BLOCK_POS));
    public static final RegistryObject<SpellActionType<PutVarAction<Boolean>>> PUT_BOOLEAN = DEFERRED_REGISTER.register("put_boolean", () -> PutVarAction.makeType(CtxVarTypes.BOOLEAN));
    
    //variable / mapped unary
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> NEGATE = DEFERRED_REGISTER.register("negate", () -> MappedUnaryVarAction.makeType(UnaryOperation.NEGATE));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> NOT = DEFERRED_REGISTER.register("not", () -> MappedUnaryVarAction.makeType(UnaryOperation.NOT));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> ROUND = DEFERRED_REGISTER.register("round", () -> MappedUnaryVarAction.makeType(UnaryOperation.ROUND));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> FLOOR = DEFERRED_REGISTER.register("floor", () -> MappedUnaryVarAction.makeType(UnaryOperation.FLOOR));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> CEIL = DEFERRED_REGISTER.register("ceil", () -> MappedUnaryVarAction.makeType(UnaryOperation.CEIL));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> SQRT = DEFERRED_REGISTER.register("sqrt", () -> MappedUnaryVarAction.makeType(UnaryOperation.SQRT));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> GET_X = DEFERRED_REGISTER.register("get_x", () -> MappedUnaryVarAction.makeType(UnaryOperation.GET_X));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> GET_Y = DEFERRED_REGISTER.register("get_y", () -> MappedUnaryVarAction.makeType(UnaryOperation.GET_Y));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> GET_Z = DEFERRED_REGISTER.register("get_z", () -> MappedUnaryVarAction.makeType(UnaryOperation.GET_Z));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> LENGTH = DEFERRED_REGISTER.register("length", () -> MappedUnaryVarAction.makeType(UnaryOperation.LENGTH));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> NORMALIZE = DEFERRED_REGISTER.register("normalized", () -> MappedUnaryVarAction.makeType(UnaryOperation.NORMALIZE));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> SIN = DEFERRED_REGISTER.register("sin", () -> MappedUnaryVarAction.makeType(UnaryOperation.SIN));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> COS = DEFERRED_REGISTER.register("cos", () -> MappedUnaryVarAction.makeType(UnaryOperation.COS));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> ASIN = DEFERRED_REGISTER.register("asin", () -> MappedUnaryVarAction.makeType(UnaryOperation.ASIN));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> ACOS = DEFERRED_REGISTER.register("acos", () -> MappedUnaryVarAction.makeType(UnaryOperation.ACOS));
    
    //variable / mapped binary
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> ADD = DEFERRED_REGISTER.register("add", () -> MappedBinaryVarAction.makeType(BinaryOperation.ADD));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> SUB = DEFERRED_REGISTER.register("sub", () -> MappedBinaryVarAction.makeType(BinaryOperation.SUB));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> MUL = DEFERRED_REGISTER.register("mul", () -> MappedBinaryVarAction.makeType(BinaryOperation.MUL));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> DIV = DEFERRED_REGISTER.register("div", () -> MappedBinaryVarAction.makeType(BinaryOperation.DIV));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> REM = DEFERRED_REGISTER.register("rem", () -> MappedBinaryVarAction.makeType(BinaryOperation.REM));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> EQ = DEFERRED_REGISTER.register("eq", () -> MappedBinaryVarAction.makeType(BinaryOperation.EQ));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> NEQ = DEFERRED_REGISTER.register("neq", () -> MappedBinaryVarAction.makeType(BinaryOperation.NEQ));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> GT = DEFERRED_REGISTER.register("gt", () -> MappedBinaryVarAction.makeType(BinaryOperation.GT));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> GEQ = DEFERRED_REGISTER.register("geq", () -> MappedBinaryVarAction.makeType(BinaryOperation.GEQ));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> LT = DEFERRED_REGISTER.register("lt", () -> MappedBinaryVarAction.makeType(BinaryOperation.LT));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> LEQ = DEFERRED_REGISTER.register("leq", () -> MappedBinaryVarAction.makeType(BinaryOperation.LEQ));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> MIN = DEFERRED_REGISTER.register("min", () -> MappedBinaryVarAction.makeType(BinaryOperation.MIN));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> MAX = DEFERRED_REGISTER.register("max", () -> MappedBinaryVarAction.makeType(BinaryOperation.MAX));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> MOVE_X = DEFERRED_REGISTER.register("move_x", () -> MappedBinaryVarAction.makeType(BinaryOperation.MOVE_X));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> MOVE_Y = DEFERRED_REGISTER.register("move_y", () -> MappedBinaryVarAction.makeType(BinaryOperation.MOVE_Y));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> MOVE_Z = DEFERRED_REGISTER.register("move_z", () -> MappedBinaryVarAction.makeType(BinaryOperation.MOVE_Z));
    
    //variable / mapped binary
    public static final RegistryObject<SpellActionType<MappedTernaryVarAction>> CONDITIONAL = DEFERRED_REGISTER.register("conditional", () -> MappedTernaryVarAction.makeType(TernaryOperation.CONDITIONAL));
    public static final RegistryObject<SpellActionType<MappedTernaryVarAction>> VEC3 = DEFERRED_REGISTER.register("vec3", () -> MappedTernaryVarAction.makeType(TernaryOperation.VEC3));
    public static final RegistryObject<SpellActionType<MappedTernaryVarAction>> BLOCK_POS = DEFERRED_REGISTER.register("block_pos", () -> MappedTernaryVarAction.makeType(TernaryOperation.BLOCK_POS));
    
    //variable / simple unary
    // -/-
    
    //variable / simple binary
    // -/-
    
    //variable / simple binary
    // -/-
    
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
        UnaryOperation.NOT.register(CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), (x) -> !x);
        
        UnaryOperation.ROUND.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.INT.get(), (x) -> (int) Math.round(x));
        UnaryOperation.FLOOR.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.INT.get(), (x) -> (int) Math.floor(x));
        UnaryOperation.CEIL.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.INT.get(), (x) -> (int) Math.ceil(x));
        
        UnaryOperation.SQRT.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x) -> x < 0 ? null : Math.sqrt(x));
        
        UnaryOperation.GET_X.register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), (x) -> x.x())
                .register(CtxVarTypes.BLOCK_POS.get(), CtxVarTypes.INT.get(), (x) -> x.getX());
        UnaryOperation.GET_Y.register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), (x) -> x.y())
                .register(CtxVarTypes.BLOCK_POS.get(), CtxVarTypes.INT.get(), (x) -> x.getY());
        UnaryOperation.GET_Z.register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), (x) -> x.z())
                .register(CtxVarTypes.BLOCK_POS.get(), CtxVarTypes.INT.get(), (x) -> x.getZ());
        
        UnaryOperation.LENGTH.register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), (x) -> x.length());
        UnaryOperation.NORMALIZE.register(CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), (x) -> x.normalize());
        
        UnaryOperation.SIN.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x) -> Math.sin(x));
        UnaryOperation.COS.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x) -> Math.cos(x));
        UnaryOperation.ASIN.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x) -> Math.asin(x));
        UnaryOperation.ACOS.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x) -> Math.acos(x));
        UnaryOperation.TO_RADIANS.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x) -> Math.toRadians(x));
        UnaryOperation.TO_DEGREES.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x) -> Math.toDegrees(x));
        
        BinaryOperation.ADD.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> x + y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> x + y)
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), (x, y) -> x.add(y));
        BinaryOperation.SUB.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> x - y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> x - y)
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), (x, y) -> x.subtract(y));
        BinaryOperation.MUL.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> x * y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> x * y)
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.VEC3.get(), (x, y) -> x.scale(y));
        BinaryOperation.DIV.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> y == 0 ? null : x / y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> y == 0 ? null : x / y)
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.VEC3.get(), (x, y) -> y == 0 ? null : x.scale(x.length() / y));
        BinaryOperation.REM.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> y == 0 ? null : x % y);
        
        BinaryOperation.EQ.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x.equals(y))
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x.equals(y))
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x.equals(y))
                .register(CtxVarTypes.BLOCK_POS.get(), CtxVarTypes.BLOCK_POS.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x.equals(y))
                .register(CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x == y);
        BinaryOperation.NEQ.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> !Objects.equals(x, y))
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> !Objects.equals(x, y))
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> !x.equals(y))
                .register(CtxVarTypes.BLOCK_POS.get(), CtxVarTypes.BLOCK_POS.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> !x.equals(y))
                .register(CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), (x, y) -> x != y);
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
        
        BinaryOperation.MOVE_X.register(CtxVarTypes.BLOCK_POS.get(), CtxVarTypes.INT.get(), CtxVarTypes.BLOCK_POS.get(), (x, y) -> x.east(y));
        BinaryOperation.MOVE_Y.register(CtxVarTypes.BLOCK_POS.get(), CtxVarTypes.INT.get(), CtxVarTypes.BLOCK_POS.get(), (x, y) -> x.above(y));
        BinaryOperation.MOVE_Z.register(CtxVarTypes.BLOCK_POS.get(), CtxVarTypes.INT.get(), CtxVarTypes.BLOCK_POS.get(), (x, y) -> x.south(y));
        
        TernaryOperation.CONDITIONAL.register(CtxVarTypes.BOOLEAN.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y, z) -> x ? y : z)
                .register(CtxVarTypes.BOOLEAN.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y, z) -> x ? y : z)
                .register(CtxVarTypes.BOOLEAN.get(), CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), (x, y, z) -> x ? y : z)
                .register(CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BLOCK_POS.get(), CtxVarTypes.BLOCK_POS.get(), CtxVarTypes.BLOCK_POS.get(), (x, y, z) -> x ? y : z)
                .register(CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), CtxVarTypes.BOOLEAN.get(), (x, y, z) -> x ? y : z);
        
        TernaryOperation.VEC3.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.VEC3.get(), (x, y, z) -> new Vec3(x, y, z));
        TernaryOperation.BLOCK_POS.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.BLOCK_POS.get(), (x, y, z) -> new BlockPos(x, y, z));
    }
}
