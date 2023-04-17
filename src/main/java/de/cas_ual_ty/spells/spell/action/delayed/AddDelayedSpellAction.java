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
import de.cas_ual_ty.spells.util.SpellsCodecs;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AddDelayedSpellAction extends AffectTypeAction<EntityTarget>
{
    public static Codec<AddDelayedSpellAction> makeCodec(SpellActionType<AddDelayedSpellAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                Codec.STRING.fieldOf(ParamNames.asynchronousActivation("on_remove")).forGetter(AddDelayedSpellAction::getRemoveActivation),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("tick_time")).forGetter(AddDelayedSpellAction::getTickTime),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("uuid")).forGetter(AddDelayedSpellAction::getUuid),
                CtxVarTypes.TAG.get().refCodec().fieldOf(ParamNames.paramCompoundTag("extra_data")).forGetter(AddDelayedSpellAction::getTag),
                Codec.optionalField("events_map", ExtraCodecs.lazyInitializedCodec(() -> SpellsCodecs.STRING_MAP)).xmap(o -> o.orElse(new HashMap<>()), m -> m.isEmpty() ? Optional.empty() : Optional.of(m)).forGetter(AddDelayedSpellAction::getEventsMap)
        ).apply(instance, (activation, multiTargets, removeActivation, tickTime, uuid, tag, eventsMap) -> new AddDelayedSpellAction(type, activation, multiTargets, removeActivation, tickTime, uuid, tag, eventsMap)));
    }
    
    public static AddDelayedSpellAction make(String activation, String multiTargets, String removeActivation, DynamicCtxVar<Integer> tickTime, DynamicCtxVar<String> uuid, DynamicCtxVar<CompoundTag> tag, Map<String, String> eventsMap)
    {
        return new AddDelayedSpellAction(SpellActionTypes.ADD_DELAYED_SPELL.get(), activation, multiTargets, removeActivation, tickTime, uuid, tag, eventsMap);
    }
    
    protected String removeActivation;
    protected DynamicCtxVar<Integer> tickTime;
    protected DynamicCtxVar<String> uuid;
    protected DynamicCtxVar<CompoundTag> tag;
    protected Map<String, String> eventsMap;
    
    public AddDelayedSpellAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public AddDelayedSpellAction(SpellActionType<?> type, String activation, String multiTargets, String removeActivation, DynamicCtxVar<Integer> tickTime, DynamicCtxVar<String> uuid, DynamicCtxVar<CompoundTag> tag, Map<String, String> eventsMap)
    {
        super(type, activation, multiTargets);
        this.removeActivation = removeActivation;
        this.tickTime = tickTime;
        this.uuid = uuid;
        this.tag = tag;
        this.eventsMap = eventsMap;
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
    
    public Map<String, String> getEventsMap()
    {
        return eventsMap;
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
                            UUID uuid = this.uuid.getValue(ctx).map(SpellsUtil::uuidFromString).orElse(null);
                            
                            holder.addDelayedSpell(ctx.spell, uuid, removeActivation, tickTime, tag, eventsMap);
                        }
                    });
                });
            });
        }
    }
}
