package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.IClientAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class SetMotionAction extends AffectTypeAction<EntityTarget>
{
    public static Codec<SetMotionAction> makeCodec(SpellActionType<SetMotionAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.VEC3.get().refCodec().fieldOf(ParamNames.paramVec3("motion")).forGetter(SetMotionAction::getMotion)
        ).apply(instance, (activation, multiTargets, motion) -> new SetMotionAction(type, activation, multiTargets, motion)));
    }
    
    public static SetMotionAction make(Object activation, Object multiTargets, DynamicCtxVar<Vec3> motion)
    {
        return new SetMotionAction(SpellActionTypes.SET_MOTION.get(), activation.toString(), multiTargets.toString(), motion);
    }
    
    protected DynamicCtxVar<Vec3> motion;
    
    public SetMotionAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public SetMotionAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<Vec3> motion)
    {
        super(type, activation, multiTargets);
        this.motion = motion;
    }
    
    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
    
    public DynamicCtxVar<Vec3> getMotion()
    {
        return motion;
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, EntityTarget entityTarget)
    {
        motion.getValue(ctx).ifPresent(motion ->
        {
            entityTarget.getEntity().setDeltaMovement(motion);
            
            if(entityTarget.getEntity() instanceof ServerPlayer player)
            {
                sendClientAction(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new ClientAction(player.getId(), motion));
            }
        });
    }
    
    public static class ClientAction implements IClientAction
    {
        protected int entityId;
        protected Vec3 motion;
        
        public ClientAction(int entityId, Vec3 motion)
        {
            this.entityId = entityId;
            this.motion = motion;
        }
        
        public ClientAction()
        {
            this(0, null);
        }
        
        @Override
        public void writeToBuf(FriendlyByteBuf buf)
        {
            buf.writeInt(entityId);
            buf.writeDouble(motion.x());
            buf.writeDouble(motion.y());
            buf.writeDouble(motion.z());
        }
        
        @Override
        public void readFromBuf(FriendlyByteBuf buf)
        {
            entityId = buf.readInt();
            motion = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }
        
        @Override
        public void execute(Level clientLevel, Player clientPlayer)
        {
            Entity e = clientLevel.getEntity(entityId);
            
            if(e != null)
            {
                e.setDeltaMovement(motion);
            }
        }
    }
}
