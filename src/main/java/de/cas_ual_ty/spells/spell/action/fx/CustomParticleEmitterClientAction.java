package de.cas_ual_ty.spells.spell.action.fx;

import de.cas_ual_ty.spells.client.particle.CustomParticleAttachMode;
import de.cas_ual_ty.spells.client.particle.CustomParticleContext;
import de.cas_ual_ty.spells.client.particle.CustomParticleEmitterInstance;
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
 * Shared network payload and client-side spawn logic for both {@link CustomParticleEmitterMotionAction} and
 * {@link CustomParticleEmitterPositionAction} - exactly one of {@link #initialPosition}/{@link #motion} (motion
 * mode) or {@link #position} (position mode) is non-empty; the other(s) are sent as {@code ""} (raw DSL is never
 * legitimately empty, so that's a safe "absent" sentinel, same as everywhere else in this mod's ad-hoc buffer
 * encodings). See {@code CustomParticleEmitterInstance}/{@code CustomParticleManager} for how the two modes tick
 * differently once spawned, and how {@link #period} drives repeat spawns.
 */
public class CustomParticleEmitterClientAction implements IClientAction
{
    protected int entityId;
    protected CustomParticleAttachMode positionAttachMode;
    protected CustomParticleAttachMode rotationAttachMode;
    protected int count;
    protected int duration;
    protected int period;
    protected String initialPosition;
    protected String motion;
    protected String position;
    protected String color;
    protected String alpha;
    protected List<CtxVar<?>> capturedVariables;

    public CustomParticleEmitterClientAction(int entityId, CustomParticleAttachMode positionAttachMode, CustomParticleAttachMode rotationAttachMode, int count, int duration, int period, String initialPosition, String motion, String position, String color, String alpha, List<CtxVar<?>> capturedVariables)
    {
        this.entityId = entityId;
        this.positionAttachMode = positionAttachMode;
        this.rotationAttachMode = rotationAttachMode;
        this.count = count;
        this.duration = duration;
        this.period = period;
        this.initialPosition = initialPosition;
        this.motion = motion;
        this.position = position;
        this.color = color;
        this.alpha = alpha;
        this.capturedVariables = capturedVariables;
    }

    public CustomParticleEmitterClientAction()
    {
        this(0, CustomParticleAttachMode.ABSOLUTE, CustomParticleAttachMode.ABSOLUTE, 0, 0, 0, "", "", "", "", "", new LinkedList<>());
    }

    @Override
    public void writeToBuf(RegistryFriendlyByteBuf buf)
    {
        buf.writeInt(entityId);
        buf.writeEnum(positionAttachMode);
        buf.writeEnum(rotationAttachMode);
        buf.writeVarInt(count);
        buf.writeVarInt(duration);
        buf.writeVarInt(period);
        buf.writeUtf(initialPosition);
        buf.writeUtf(motion);
        buf.writeUtf(position);
        buf.writeUtf(color);
        buf.writeUtf(alpha);
        buf.writeVarInt(capturedVariables.size());

        for(CtxVar<?> var : capturedVariables)
        {
            writeCtxVar(buf, var);
        }
    }

    @Override
    public void readFromBuf(RegistryFriendlyByteBuf buf)
    {
        entityId = buf.readInt();
        positionAttachMode = buf.readEnum(CustomParticleAttachMode.class);
        rotationAttachMode = buf.readEnum(CustomParticleAttachMode.class);
        count = buf.readVarInt();
        duration = buf.readVarInt();
        period = buf.readVarInt();
        initialPosition = buf.readUtf();
        motion = buf.readUtf();
        position = buf.readUtf();
        color = buf.readUtf();
        alpha = buf.readUtf();

        int capturedCount = buf.readVarInt();
        capturedVariables = new LinkedList<>();

        for(int i = 0; i < capturedCount; i++)
        {
            capturedVariables.add(readCtxVar(buf));
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

        List<CustomParticleInstance> particles = new LinkedList<>();
        CustomParticleEmitterInstance emitter = new CustomParticleEmitterInstance(clientLevel, attachedTo, positionAttachMode, rotationAttachMode, particles, count, duration, period, spawnPosition, spawnYaw);

        emitter.context.capture(CtxVarTypes.INT.get(), CustomParticleContext.MAX_AGE_NAME, period > 0 ? period : duration);

        for(CtxVar<?> var : capturedVariables)
        {
            captureInto(emitter.context, var);
        }

        emitter.motionExpr = motion.isEmpty() ? null : Compiler.compileString(motion, CtxVarTypes.VEC3.get());
        emitter.positionExpr = position.isEmpty() ? null : Compiler.compileString(position, CtxVarTypes.VEC3.get());
        emitter.colorExpr = Compiler.compileString(color, CtxVarTypes.VEC3.get());
        emitter.alphaExpr = Compiler.compileString(alpha, CtxVarTypes.DOUBLE.get());
        emitter.initialPositionExpr = initialPosition.isEmpty() ? null : Compiler.compileString(initialPosition, CtxVarTypes.VEC3.get());

        emitter.spawnBatch();

        CustomParticleRenderer.ACTIVE.add(emitter);
    }

    private static <T> void captureInto(CustomParticleContext ctx, CtxVar<T> var)
    {
        ctx.capture(var.getType(), var.getName(), var.getValue());
    }
}
