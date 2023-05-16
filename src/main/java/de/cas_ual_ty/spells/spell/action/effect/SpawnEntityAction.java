package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class SpawnEntityAction extends SpellAction
{
    public static Codec<SpawnEntityAction> makeCodec(SpellActionType<SpawnEntityAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                Codec.STRING.fieldOf(ParamNames.destinationTarget("entity")).forGetter(SpawnEntityAction::getEntity),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("entity_type")).forGetter(SpawnEntityAction::getEntityType),
                Codec.STRING.fieldOf(ParamNames.singleTarget("position")).forGetter(SpawnEntityAction::getPosition),
                CtxVarTypes.VEC3.get().refCodec().fieldOf(ParamNames.paramVec3("direction")).forGetter(SpawnEntityAction::getDirection),
                CtxVarTypes.VEC3.get().refCodec().fieldOf(ParamNames.paramVec3("motion")).forGetter(SpawnEntityAction::getMotion),
                CtxVarTypes.TAG.get().optionalRefCodec(ParamNames.paramCompoundTag("tag")).forGetter(SpawnEntityAction::getTag)
        ).apply(instance, (activation, entity, entityType, position, direction, motion, tag) -> new SpawnEntityAction(type, activation, entity, entityType, position, direction, motion, tag)));
    }
    
    public static SpawnEntityAction make(Object activation, String entity, DynamicCtxVar<String> entityType, Object position, DynamicCtxVar<Vec3> direction, DynamicCtxVar<Vec3> motion, @Nullable DynamicCtxVar<CompoundTag> tag)
    {
        return new SpawnEntityAction(SpellActionTypes.SPAWN_ENTITY.get(), activation.toString(), entity, entityType, position.toString(), direction, motion, tag);
    }
    
    public static SpawnEntityAction make(Object activation, String entity, DynamicCtxVar<String> entityType, Object position, DynamicCtxVar<Vec3> direction, DynamicCtxVar<Vec3> motion)
    {
        return make(activation, entity, entityType, position, direction, motion, null);
    }
    
    protected String entity;
    protected DynamicCtxVar<String> entityType;
    protected String position;
    protected DynamicCtxVar<Vec3> direction;
    protected DynamicCtxVar<Vec3> motion;
    protected DynamicCtxVar<CompoundTag> tag;
    
    public SpawnEntityAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public SpawnEntityAction(SpellActionType<?> type, String activation, String entity, DynamicCtxVar<String> entityType, String position, DynamicCtxVar<Vec3> direction, DynamicCtxVar<Vec3> motion, DynamicCtxVar<CompoundTag> tag)
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
    
    public DynamicCtxVar<String> getEntityType()
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
    
    public DynamicCtxVar<CompoundTag> getTag()
    {
        return tag;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        SpellsUtil.stringToObject(ctx, entityType, ForgeRegistries.ENTITY_TYPES).ifPresent(entityType ->
        {
            if(entityType != EntityType.PLAYER)
            {
                ctx.getTargetGroup(this.position).getSingleType(TargetTypes.POSITION.get(), position ->
                {
                    Vec3 direction = this.direction.getValue(ctx).orElse(Vec3.ZERO);
                    Vec3 motion = this.motion.getValue(ctx).orElse(Vec3.ZERO);
                    Entity entity = entityType.create(ctx.getLevel());
                    
                    if(entity != null)
                    {
                        this.tag.getValue(ctx).ifPresent(tag0 ->
                        {
                            CompoundTag tag = entity.saveWithoutId(new CompoundTag());
                            for(String key : tag0.getAllKeys())
                            {
                                Tag t = tag0.get(key);
                                
                                if(t != null)
                                {
                                    tag.put(key, t);
                                }
                            }
                            
                            entity.load(tag);
                        });
                        
                        entity.setPos(position.getPosition());
                        
                        // TODO doesnt quite work
                        entity.setYRot((float) (Mth.atan2(direction.x, direction.z) * (double) (180F / (float) Math.PI)));
                        entity.setXRot((float) (Mth.atan2(direction.y, direction.horizontalDistance()) * (double) (180F / (float) Math.PI)));
                        
                        entity.setDeltaMovement(motion);
                        
                        ctx.getLevel().addFreshEntity(entity);
                        ctx.getOrCreateTargetGroup(this.entity).addTargets(Target.of(entity));
                    }
                });
            }
        });
    }
}
