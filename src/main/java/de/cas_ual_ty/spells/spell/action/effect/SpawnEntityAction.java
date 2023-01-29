package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PositionTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class SpawnEntityAction extends AffectSingleTypeAction<PositionTarget>
{
    public static Codec<SpawnEntityAction> makeCodec(SpellActionType<SpawnEntityAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                targetCodec(),
                ForgeRegistries.ENTITY_TYPES.getCodec().fieldOf("entity").forGetter(SpawnEntityAction::getEntity),
                Codec.STRING.fieldOf(ParamNames.singleTarget("position")).forGetter(SpawnEntityAction::getPosition),
                CtxVarTypes.VEC3.get().refCodec().fieldOf(ParamNames.paramVec3("direction")).forGetter(SpawnEntityAction::getDirection),
                CtxVarTypes.VEC3.get().refCodec().fieldOf(ParamNames.paramVec3("motion")).forGetter(SpawnEntityAction::getMotion)
        ).apply(instance, (activation, target, entity, position, direction, motion) -> new SpawnEntityAction(type, activation, target, entity, position, direction, motion)));
    }
    
    public static SpawnEntityAction make(String activation, String target, EntityType<?> entity, String position, DynamicCtxVar<Vec3> direction, DynamicCtxVar<Vec3> motion)
    {
        return new SpawnEntityAction(SpellActionTypes.SPAWN_ENTITY.get(), activation, target, entity, position, direction, motion);
    }
    
    //TODO maybe allow setting the nbt tag, then load from it, then set pos/dir/mot
    // this allows entity based customization
    
    protected EntityType<? extends Entity> entity;
    protected String position;
    protected DynamicCtxVar<Vec3> direction;
    protected DynamicCtxVar<Vec3> motion;
    
    public SpawnEntityAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public SpawnEntityAction(SpellActionType<?> type, String activation, String target, EntityType<?> entity, String position, DynamicCtxVar<Vec3> direction, DynamicCtxVar<Vec3> motion)
    {
        super(type, activation, target);
        this.entity = entity;
        this.position = position;
        this.direction = direction;
        this.motion = motion;
    }
    
    public EntityType<? extends Entity> getEntity()
    {
        return entity;
    }
    
    public String getPosition()
    {
        return position;
    }
    
    public DynamicCtxVar<Vec3> getDirection()
    {
        return direction;
    }
    
    public DynamicCtxVar<Vec3> getMotion()
    {
        return motion;
    }
    
    @Override
    public ITargetType<PositionTarget> getAffectedType()
    {
        return TargetTypes.POSITION.get();
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, PositionTarget positionTarget)
    {
        if(entity != EntityType.PLAYER)
        {
            Vec3 direction = this.direction.getValue(ctx).orElse(Vec3.ZERO);
            Vec3 motion = this.motion.getValue(ctx).orElse(Vec3.ZERO);
            Entity entity = this.entity.create(ctx.getLevel());
            
            if(entity != null)
            {
                entity.setPos(positionTarget.getPosition());
                
                //TODO
                //entity.setXRot(direction.something);
                //entity.setYRot(direction.something);
                
                entity.setDeltaMovement(motion);
                
                ctx.getLevel().addFreshEntity(entity);
            }
        }
    }
}
