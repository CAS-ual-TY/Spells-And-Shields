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
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

public class ApplyEntityTagAction extends SpellAction
{
    public static Codec<ApplyEntityTagAction> makeCodec(SpellActionType<ApplyEntityTagAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                Codec.STRING.fieldOf(ParamNames.singleTarget("entity")).forGetter(ApplyEntityTagAction::getEntity),
                CtxVarTypes.COMPOUND_TAG.get().refCodec().fieldOf("tag").forGetter(ApplyEntityTagAction::getTag)
        ).apply(instance, (activation, entity, tag) -> new ApplyEntityTagAction(type, activation, entity, tag)));
    }
    
    public static ApplyEntityTagAction make(String activation, String entity, DynamicCtxVar<CompoundTag> tag)
    {
        return new ApplyEntityTagAction(SpellActionTypes.APPLY_ENTITY_TAG.get(), activation, entity, tag);
    }
    
    protected String entity;
    protected DynamicCtxVar<CompoundTag> tag;
    
    public ApplyEntityTagAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ApplyEntityTagAction(SpellActionType<?> type, String activation, String entity, DynamicCtxVar<CompoundTag> tag)
    {
        super(type, activation);
        this.entity = entity;
        this.tag = tag;
    }
    
    public String getEntity()
    {
        return entity;
    }
    
    public DynamicCtxVar<CompoundTag> getTag()
    {
        return tag;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        ctx.getOrCreateTargetGroup(entity).getSingleTarget(target ->
        {
            if(entity != null)
            {
                TargetTypes.ENTITY.get().ifType(target, entityTarget ->
                {
                    this.tag.getValue(ctx).ifPresent(tag0 ->
                    {
                        Entity entity = entityTarget.getEntity();
                        
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
                });
            }
        });
    }
}
