package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

public class ApplyEntityTagAction extends AffectTypeAction<EntityTarget>
{
    public static Codec<ApplyEntityTagAction> makeCodec(SpellActionType<ApplyEntityTagAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.TAG.get().refCodec().fieldOf(ParamNames.paramCompoundTag("tag")).forGetter(ApplyEntityTagAction::getTag)
        ).apply(instance, (activation, multiTargets, tag) -> new ApplyEntityTagAction(type, activation, multiTargets, tag)));
    }
    
    public static ApplyEntityTagAction make(Object activation, Object multiTargets, DynamicCtxVar<CompoundTag> tag)
    {
        return new ApplyEntityTagAction(SpellActionTypes.APPLY_ENTITY_TAG.get(), activation.toString(), multiTargets.toString(), tag);
    }
    
    protected DynamicCtxVar<CompoundTag> tag;
    
    public ApplyEntityTagAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ApplyEntityTagAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<CompoundTag> tag)
    {
        super(type, activation, multiTargets);
        this.multiTargets = multiTargets;
        this.tag = tag;
    }
    
    public DynamicCtxVar<CompoundTag> getTag()
    {
        return tag;
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, EntityTarget target)
    {
        this.tag.getValue(ctx).ifPresent(tag0 ->
        {
            Entity entity = target.getEntity();
            
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
    }
    
    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
}
