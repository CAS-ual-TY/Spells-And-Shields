package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class SpawnEntityAction extends SpellAction
{
    public static Codec<SpawnEntityAction> makeCodec(SpellActionType<SpawnEntityAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                Codec.STRING.fieldOf(ParamNames.singleTarget("entity")).forGetter(SpawnEntityAction::getEntity),
                ForgeRegistries.ENTITY_TYPES.getCodec().fieldOf("entity_type").forGetter(SpawnEntityAction::getEntityType),
                Codec.STRING.fieldOf(ParamNames.singleTarget("position")).forGetter(SpawnEntityAction::getPosition),
                CtxVarTypes.VEC3.get().refCodec().fieldOf(ParamNames.paramVec3("direction")).forGetter(SpawnEntityAction::getDirection),
                CtxVarTypes.VEC3.get().refCodec().fieldOf(ParamNames.paramVec3("motion")).forGetter(SpawnEntityAction::getMotion),
                CompoundTag.CODEC.fieldOf("tag").forGetter(SpawnEntityAction::getTag)
        ).apply(instance, (activation, entity, entityType, position, direction, motion, tag) -> new SpawnEntityAction(type, activation, entity, entityType, position, direction, motion, tag)));
    }
    
    public static SpawnEntityAction make(String activation, String entity, EntityType<?> entityType, String position, DynamicCtxVar<Vec3> direction, DynamicCtxVar<Vec3> motion, CompoundTag tag)
    {
        return new SpawnEntityAction(SpellActionTypes.SPAWN_ENTITY.get(), activation, entity, entityType, position, direction, motion, tag);
    }
    
    //TODO maybe allow setting the nbt tag, then load from it, then set pos/dir/mot
    // this allows entity based customization
    
    protected String entity;
    protected EntityType<? extends Entity> entityType;
    protected String position;
    protected DynamicCtxVar<Vec3> direction;
    protected DynamicCtxVar<Vec3> motion;
    protected CompoundTag tag;
    
    public SpawnEntityAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public SpawnEntityAction(SpellActionType<?> type, String activation, String entity, EntityType<?> entityType, String position, DynamicCtxVar<Vec3> direction, DynamicCtxVar<Vec3> motion, CompoundTag tag)
    {
        super(type, activation);
        this.entity = entity;
        this.entityType = entityType;
        this.position = position;
        this.direction = direction;
        this.motion = motion;
        this.tag = tag;
    }
    
    public String getEntity()
    {
        return entity;
    }
    
    public EntityType<? extends Entity> getEntityType()
    {
        return entityType;
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
    
    public CompoundTag getTag()
    {
        return tag;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        if(entityType != EntityType.PLAYER)
        {
            ctx.getTargetGroup(this.position).getSingleTarget(target ->
            {
                TargetTypes.STATIC.get().ifType(target, position ->
                {
                    Vec3 direction = this.direction.getValue(ctx).orElse(Vec3.ZERO);
                    Vec3 motion = this.motion.getValue(ctx).orElse(Vec3.ZERO);
                    Entity entity = this.entityType.create(ctx.getLevel());
                    
                    if(entity != null)
                    {
                        entity.setPos(position.getPosition());
                        
                        //TODO
                        //entity.setXRot(direction.something);
                        //entity.setYRot(direction.something);
                        
                        entity.setDeltaMovement(motion);
                        
                        CompoundTag tag = entity.saveWithoutId(new CompoundTag());
                        for(String key : this.tag.getAllKeys())
                        {
                            tag.put(key, this.tag.get(key));
                        }
                        
                        entity.load(tag);
                        ctx.getLevel().addFreshEntity(entity);
                    }
                });
            });
        }
    }
}
