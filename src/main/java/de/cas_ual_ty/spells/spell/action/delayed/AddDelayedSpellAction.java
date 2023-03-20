package de.cas_ual_ty.spells.spell.action.delayed;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.DelayedSpellHolder;
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

import java.util.UUID;

public class AddDelayedSpellAction extends AffectTypeAction<EntityTarget>
{
    public static Codec<AddDelayedSpellAction> makeCodec(SpellActionType<AddDelayedSpellAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                targetsCodec(),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("on_remove")).forGetter(AddDelayedSpellAction::getRemoveActivation),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("tick_time")).forGetter(AddDelayedSpellAction::getTickTime),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("uuid")).forGetter(AddDelayedSpellAction::getUuid),
                CtxVarTypes.COMPOUND_TAG.get().refCodec().fieldOf(ParamNames.paramCompoundTag("extra_data")).forGetter(AddDelayedSpellAction::getTag)
        ).apply(instance, (activation, targets, removeActivation, tickTime, uuid, tag) -> new AddDelayedSpellAction(type, activation, targets, removeActivation, tickTime, uuid, tag)));
    }
    
    public static AddDelayedSpellAction make(String activation, String targets, String removeActivation, DynamicCtxVar<Integer> tickTime, DynamicCtxVar<String> uuid, DynamicCtxVar<CompoundTag> tag)
    {
        return new AddDelayedSpellAction(SpellActionTypes.ADD_DELAYED_SPELL.get(), activation, targets, removeActivation, tickTime, uuid, tag);
    }
    
    protected String removeActivation;
    protected DynamicCtxVar<Integer> tickTime;
    protected DynamicCtxVar<String> uuid;
    protected DynamicCtxVar<CompoundTag> tag;
    
    public AddDelayedSpellAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public AddDelayedSpellAction(SpellActionType<?> type, String activation, String targets, String removeActivation, DynamicCtxVar<Integer> tickTime, DynamicCtxVar<String> uuid, DynamicCtxVar<CompoundTag> tag)
    {
        super(type, activation, targets);
        this.removeActivation = removeActivation;
        this.tickTime = tickTime;
        this.uuid = uuid;
        this.tag = tag;
    }
    
    public String getRemoveActivation()
    {
        return removeActivation;
    }
    
    public DynamicCtxVar<Integer> getTickTime()
    {
        return tickTime;
    }
    
    public DynamicCtxVar<String> getUuid()
    {
        return uuid;
    }
    
    public DynamicCtxVar<CompoundTag> getTag()
    {
        return tag;
    }
    
    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, EntityTarget target)
    {
        if(!removeActivation.isEmpty())
        {
            DelayedSpellHolder.getHolder(target.getEntity()).ifPresent(holder ->
            {
                tickTime.getValue(ctx).ifPresent(tickTime ->
                {
                    tag.getValue(ctx).ifPresent(tag ->
                    {
                        if(tickTime > 0)
                        {
                            UUID uuid = this.uuid.getValue(ctx).map(s ->
                            {
                                try
                                {
                                    return UUID.fromString(s);
                                }
                                catch(IllegalArgumentException e)
                                {
                                    return null;
                                }
                            }).orElse(null);
                            
                            holder.addDelayedSpell(ctx.spell.getNodeId(), uuid, removeActivation, tickTime, tag);
                        }
                    });
                });
            });
        }
    }
}
