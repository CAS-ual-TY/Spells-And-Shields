package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.effect.DamageAction;
import de.cas_ual_ty.spells.spell.action.target.CopyTargetsAction;
import de.cas_ual_ty.spells.spell.action.target.LookAtTargetAction;
import de.cas_ual_ty.spells.spell.action.target.PickTargetAction;
import de.cas_ual_ty.spells.spell.action.variable.MappedBinaryVarAction;
import de.cas_ual_ty.spells.spell.action.variable.MappedUnaryVarAction;
import de.cas_ual_ty.spells.spell.action.variable.SimpleUnaryVarAction;
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
    
    //target
    public static final RegistryObject<SpellActionType<CopyTargetsAction>> COPY_TARGETS = DEFERRED_REGISTER.register("copy_targets", () -> new SpellActionType<>(CopyTargetsAction::new, CopyTargetsAction::makeCodec));
    public static final RegistryObject<SpellActionType<PickTargetAction>> PICK_TARGET = DEFERRED_REGISTER.register("pick_target", () -> new SpellActionType<>(PickTargetAction::new, PickTargetAction::makeCodec2));
    public static final RegistryObject<SpellActionType<LookAtTargetAction>> LOOK_AT_TARGET = DEFERRED_REGISTER.register("look_at_target", () -> new SpellActionType<>(LookAtTargetAction::new, LookAtTargetAction::makeCodec));
    
    //variable / mapped binary
    public static final MappedBinaryVarAction.BinaryOperatorMap ADD_MAP = new MappedBinaryVarAction.BinaryOperatorMap();
    public static final MappedBinaryVarAction.BinaryOperatorMap SUB_MAP = new MappedBinaryVarAction.BinaryOperatorMap();
    public static final MappedBinaryVarAction.BinaryOperatorMap MUL_MAP = new MappedBinaryVarAction.BinaryOperatorMap();
    public static final MappedBinaryVarAction.BinaryOperatorMap DIV_MAP = new MappedBinaryVarAction.BinaryOperatorMap();
    public static final MappedBinaryVarAction.BinaryOperatorMap MIN_MAP = new MappedBinaryVarAction.BinaryOperatorMap();
    public static final MappedBinaryVarAction.BinaryOperatorMap MAX_MAP = new MappedBinaryVarAction.BinaryOperatorMap();
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> ADD = DEFERRED_REGISTER.register("add", () -> MappedBinaryVarAction.makeType(ADD_MAP));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> SUB = DEFERRED_REGISTER.register("sub", () -> MappedBinaryVarAction.makeType(SUB_MAP));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> MUL = DEFERRED_REGISTER.register("mul", () -> MappedBinaryVarAction.makeType(MUL_MAP));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> DIV = DEFERRED_REGISTER.register("div", () -> MappedBinaryVarAction.makeType(DIV_MAP));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> MIN = DEFERRED_REGISTER.register("min", () -> MappedBinaryVarAction.makeType(MIN_MAP));
    public static final RegistryObject<SpellActionType<MappedBinaryVarAction>> MAX = DEFERRED_REGISTER.register("max", () -> MappedBinaryVarAction.makeType(MAX_MAP));
    
    //variable / mapped unary
    public static final MappedUnaryVarAction.UnaryOperatorMap SQRT_MAP = new MappedUnaryVarAction.UnaryOperatorMap();
    public static final MappedUnaryVarAction.UnaryOperatorMap X_MAP = new MappedUnaryVarAction.UnaryOperatorMap();
    public static final MappedUnaryVarAction.UnaryOperatorMap Y_MAP = new MappedUnaryVarAction.UnaryOperatorMap();
    public static final MappedUnaryVarAction.UnaryOperatorMap Z_MAP = new MappedUnaryVarAction.UnaryOperatorMap();
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> SQRT = DEFERRED_REGISTER.register("sqrt", () -> MappedUnaryVarAction.makeType(SQRT_MAP));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> X = DEFERRED_REGISTER.register("x", () -> MappedUnaryVarAction.makeType(X_MAP));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> Y = DEFERRED_REGISTER.register("y", () -> MappedUnaryVarAction.makeType(Y_MAP));
    public static final RegistryObject<SpellActionType<MappedUnaryVarAction>> Z = DEFERRED_REGISTER.register("z", () -> MappedUnaryVarAction.makeType(Z_MAP));
    
    //variable / simple unary
    public static final RegistryObject<SpellActionType<SimpleUnaryVarAction<Vec3, Double>>> LENGTH = DEFERRED_REGISTER.register("length", () -> SimpleUnaryVarAction.makeType(CtxVarTypes.VEC3, CtxVarTypes.DOUBLE, x -> x.length()));
    public static final RegistryObject<SpellActionType<SimpleUnaryVarAction<Vec3, Vec3>>> NORMALIZE = DEFERRED_REGISTER.register("normalize", () -> SimpleUnaryVarAction.makeType(CtxVarTypes.VEC3, CtxVarTypes.VEC3, x -> x.normalize()));
    public static final RegistryObject<SpellActionType<SimpleUnaryVarAction<Double, Integer>>> ROUND = DEFERRED_REGISTER.register("round", () -> SimpleUnaryVarAction.makeType(CtxVarTypes.DOUBLE, CtxVarTypes.INT, x -> (int) Math.round(x)));
    public static final RegistryObject<SpellActionType<SimpleUnaryVarAction<Double, Integer>>> FLOOR = DEFERRED_REGISTER.register("floor", () -> SimpleUnaryVarAction.makeType(CtxVarTypes.DOUBLE, CtxVarTypes.INT, x -> (int) Math.floor(x)));
    public static final RegistryObject<SpellActionType<SimpleUnaryVarAction<Double, Integer>>> CEIL = DEFERRED_REGISTER.register("ceil", () -> SimpleUnaryVarAction.makeType(CtxVarTypes.DOUBLE, CtxVarTypes.INT, x -> (int) Math.ceil(x)));
    
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
        ADD_MAP.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> x + y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> x + y)
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), (x, y) -> x.add(y));
        
        SUB_MAP.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> x - y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> x - y)
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), CtxVarTypes.VEC3.get(), (x, y) -> x.subtract(y));
        
        MUL_MAP.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> x * y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> x * y)
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.VEC3.get(), (x, y) -> x.scale(y));
        
        DIV_MAP.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> y == 0 ? 0 : x / y)
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> y == 0 ? 0 : x / y)
                .register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.VEC3.get(), (x, y) -> y == 0 ? Vec3.ZERO : x.scale(x.length() / y));
        
        MIN_MAP.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> Math.min(x, y))
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> Math.min(x, y));
        
        MAX_MAP.register(CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), CtxVarTypes.INT.get(), (x, y) -> Math.max(x, y))
                .register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x, y) -> Math.max(x, y));
        
        SQRT_MAP.register(CtxVarTypes.DOUBLE.get(), CtxVarTypes.DOUBLE.get(), (x) -> Math.sqrt(Math.abs(x)));
        
        X_MAP.register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), (x) -> x.x())
                .register(CtxVarTypes.BLOCK_POS.get(), CtxVarTypes.INT.get(), (x) -> x.getX());
        
        Y_MAP.register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), (x) -> x.y())
                .register(CtxVarTypes.BLOCK_POS.get(), CtxVarTypes.INT.get(), (x) -> x.getY());
        
        Z_MAP.register(CtxVarTypes.VEC3.get(), CtxVarTypes.DOUBLE.get(), (x) -> x.z())
                .register(CtxVarTypes.BLOCK_POS.get(), CtxVarTypes.INT.get(), (x) -> x.getZ());
    }
}
