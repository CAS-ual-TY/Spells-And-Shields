package de.cas_ual_ty.spells.spell.action.fx;

import de.cas_ual_ty.spells.client.particle.CustomParticleAttachMode;
import de.cas_ual_ty.spells.client.particle.CustomParticleContext;
import de.cas_ual_ty.spells.client.particle.CustomParticleEmitterInstance;
import de.cas_ual_ty.spells.client.particle.CustomParticleInitVar;
import de.cas_ual_ty.spells.client.particle.CustomParticleInstance;
import de.cas_ual_ty.spells.client.particle.CustomParticleRenderer;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.spell.action.IClientAction;
import de.cas_ual_ty.spells.spell.compiler.Compiler;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.List;

/**
 * Shared network payload and client-side spawn logic for {@link CustomParticleEmitterMotionAction}/
 * {@link CustomParticleEmitterPositionAction} - exactly one of {@link #initialPosition}/{@link #motion} or
 * {@link #position} is non-empty, the rest sent as {@code ""} ("absent" sentinel).
 * <p>
 * {@link #execute} must call {@code setFrameVars} before the initial {@code spawnBatch()} - without it,
 * {@code source_motion}/{@code source_yaw}/{@code source_pitch} are unset for the first batch's formulas.
 */
public class CustomParticleEmitterClientAction implements IClientAction
{
    protected int entityId;
    protected CustomParticleAttachMode positionAttachMode;
    protected CustomParticleAttachMode rotationAttachMode;
    protected int count;
    protected int totalLifetime;
    protected int period;
    protected int delay;
    protected String initialPosition;
    protected String motion;
    protected String position;
    protected String color;
    protected String alpha;
    protected String particleLifetime;
    protected List<CtxVar<?>> capturedVariables;
    protected List<CustomParticleInitEntry> initialize;

    public CustomParticleEmitterClientAction(int entityId, CustomParticleAttachMode positionAttachMode, CustomParticleAttachMode rotationAttachMode, int count, int totalLifetime, int period, int delay, String initialPosition, String motion, String position, String color, String alpha, String particleLifetime, List<CtxVar<?>> capturedVariables, List<CustomParticleInitEntry> initialize)
    {
        this.entityId = entityId;
        this.positionAttachMode = positionAttachMode;
        this.rotationAttachMode = rotationAttachMode;
        this.count = count;
        this.totalLifetime = totalLifetime;
        this.period = period;
        this.delay = delay;
        this.initialPosition = initialPosition;
        this.motion = motion;
        this.position = position;
        this.color = color;
        this.alpha = alpha;
        this.particleLifetime = particleLifetime;
        this.capturedVariables = capturedVariables;
        this.initialize = initialize;
    }

    public CustomParticleEmitterClientAction()
    {
        this(0, CustomParticleAttachMode.ABSOLUTE, CustomParticleAttachMode.ABSOLUTE, 0, 0, 0, 0, "", "", "", "", "", "", new LinkedList<>(), new LinkedList<>());
    }

    @Override
    public void writeToBuf(RegistryFriendlyByteBuf buf)
    {
        buf.writeInt(entityId);
        buf.writeEnum(positionAttachMode);
        buf.writeEnum(rotationAttachMode);
        buf.writeVarInt(count);
        buf.writeVarInt(totalLifetime);
        buf.writeVarInt(period);
        buf.writeVarInt(delay);
        buf.writeUtf(initialPosition);
        buf.writeUtf(motion);
        buf.writeUtf(position);
        buf.writeUtf(color);
        buf.writeUtf(alpha);
        buf.writeUtf(particleLifetime);
        buf.writeVarInt(capturedVariables.size());

        for(CtxVar<?> var : capturedVariables)
        {
            writeCtxVar(buf, var);
        }

        buf.writeVarInt(initialize.size());

        for(CustomParticleInitEntry entry : initialize)
        {
            buf.writeById(CtxVarTypes.REGISTRY::getId, entry.type());
            buf.writeUtf(entry.name());
            buf.writeUtf(entry.value());
        }
    }

    @Override
    public void readFromBuf(RegistryFriendlyByteBuf buf)
    {
        entityId = buf.readInt();
        positionAttachMode = buf.readEnum(CustomParticleAttachMode.class);
        rotationAttachMode = buf.readEnum(CustomParticleAttachMode.class);
        count = buf.readVarInt();
        totalLifetime = buf.readVarInt();
        period = buf.readVarInt();
        delay = buf.readVarInt();
        initialPosition = buf.readUtf();
        motion = buf.readUtf();
        position = buf.readUtf();
        color = buf.readUtf();
        alpha = buf.readUtf();
        particleLifetime = buf.readUtf();

        int capturedCount = buf.readVarInt();
        capturedVariables = new LinkedList<>();

        for(int i = 0; i < capturedCount; i++)
        {
            capturedVariables.add(readCtxVar(buf));
        }

        int initializeCount = buf.readVarInt();
        initialize = new LinkedList<>();

        for(int i = 0; i < initializeCount; i++)
        {
            CtxVarType<?> type = buf.readById(CtxVarTypes.REGISTRY::byId);
            String name = buf.readUtf();
            String formula = buf.readUtf();
            initialize.add(new CustomParticleInitEntry(type, name, formula));
        }
    }

    private static <T> void writeCtxVar(RegistryFriendlyByteBuf buf, CtxVar<T> var)
    {
        buf.writeById(CtxVarTypes.REGISTRY::getId, var.getType());
        buf.writeUtf(var.getName());
        ByteBufCodecs.fromCodec(var.getType().getImmCodec()).encode(buf, var.getValue());
    }

    private static CtxVar<?> readCtxVar(RegistryFriendlyByteBuf buf)
    {
        CtxVarType<?> type = buf.readById(CtxVarTypes.REGISTRY::byId);
        String name = buf.readUtf();
        return readCtxVarTyped(buf, type, name);
    }

    private static <T> CtxVar<T> readCtxVarTyped(RegistryFriendlyByteBuf buf, CtxVarType<T> type, String name)
    {
        T value = ByteBufCodecs.fromCodec(type.getImmCodec()).decode(buf);
        return new CtxVar<>(type, name, value);
    }

    @Override
    public void execute(Level clientLevel, Player clientPlayer)
    {
        Entity attachedTo = clientLevel.getEntity(entityId);

        if(attachedTo == null && (positionAttachMode == CustomParticleAttachMode.RELATIVE || rotationAttachMode == CustomParticleAttachMode.RELATIVE))
        {
            return;
        }

        Vec3 spawnPosition = attachedTo != null ? attachedTo.position() : Vec3.ZERO;
        float spawnYaw = attachedTo != null ? attachedTo.getViewYRot(1.0F) : 0.0F;
        float spawnPitch = attachedTo != null ? attachedTo.getViewXRot(1.0F) : 0.0F;

        List<CustomParticleInstance> particles = new LinkedList<>();
        CustomParticleEmitterInstance emitter = new CustomParticleEmitterInstance(clientLevel, attachedTo, positionAttachMode, rotationAttachMode, particles, count, totalLifetime, period, delay, spawnPosition);

        emitter.context.setFrameVars(
                attachedTo != null ? attachedTo.getDeltaMovement() : Vec3.ZERO,
                spawnYaw,
                spawnPitch
        );
        emitter.context.capture(CtxVarTypes.INT.get(), CustomParticleContext.MAX_AGE_NAME, period > 0 ? period : totalLifetime);

        for(CtxVar<?> var : capturedVariables)
        {
            captureInto(emitter.context, var);
        }

        emitter.motionExpr = motion.isEmpty() ? null : Compiler.compileString(motion, CtxVarTypes.VEC3.get());
        emitter.positionExpr = position.isEmpty() ? null : Compiler.compileString(position, CtxVarTypes.VEC3.get());
        emitter.colorExpr = Compiler.compileString(color, CtxVarTypes.VEC3.get());
        emitter.alphaExpr = Compiler.compileString(alpha, CtxVarTypes.DOUBLE.get());
        emitter.initialPositionExpr = initialPosition.isEmpty() ? null : Compiler.compileString(initialPosition, CtxVarTypes.VEC3.get());
        emitter.particleLifetimeExpr = particleLifetime.isEmpty() ? null : Compiler.compileString(particleLifetime, CtxVarTypes.INT.get());

        List<CustomParticleInitVar<?>> compiledInitVars = new LinkedList<>();

        for(CustomParticleInitEntry entry : initialize)
        {
            compiledInitVars.add(compileInitVar(entry));
        }

        emitter.initVars = compiledInitVars;

        if(delay <= 0)
        {
            emitter.spawnBatch();
        }

        CustomParticleRenderer.ACTIVE.add(emitter);
    }

    private static <T> void captureInto(CustomParticleContext ctx, CtxVar<T> var)
    {
        ctx.capture(var.getType(), var.getName(), var.getValue());
    }

    private static <T> CustomParticleInitVar<T> compileInitVar(CtxVarType<T> type, String name, String formula)
    {
        return new CustomParticleInitVar<>(type, name, Compiler.compileString(formula, type));
    }

    private static CustomParticleInitVar<?> compileInitVar(CustomParticleInitEntry entry)
    {
        return compileInitVar(entry.type(), entry.name(), entry.value());
    }
}
