package de.cas_ual_ty.spells.spell.context;

import de.cas_ual_ty.spells.capability.DelayedSpellHolder;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SpellsEvents
{
    public static final Map<String, RegisteredEvent<?>> NAME_TO_ENTRY = new HashMap<>();
    
    public static void registerEvents()
    {
        register(BuiltinEvents.LIVING_ATTACK_ATTACKER.activation, LivingAttackEvent.class, event -> Optional.ofNullable(event.getSource()).map(DamageSource::getEntity))
                .addTargetLink(e -> Target.of(e.getEntity()), "victim")
                .addVariableLink(e -> e.getSource().getMsgId(), CtxVarTypes.STRING, "damage_type")
                .addVariableLink(e -> (double) e.getAmount(), CtxVarTypes.DOUBLE, "damage_amount");
        
        register(BuiltinEvents.LIVING_ATTACK_VICTIM.activation, LivingAttackEvent.class)
                .addTargetLink(e -> e.getSource().getEntity() != null ? Target.of(e.getSource().getEntity()) : null, "attacker")
                .addVariableLink(e -> e.getSource().getMsgId(), CtxVarTypes.STRING, "damage_type")
                .addVariableLink(e -> (double) e.getAmount(), CtxVarTypes.DOUBLE, "damage_amount");
        
        register(BuiltinEvents.LIVING_HURT_ATTACKER.activation, LivingHurtEvent.class, event -> Optional.ofNullable(event.getSource()).map(DamageSource::getEntity))
                .addTargetLink(e -> Target.of(e.getEntity()), "victim")
                .addVariableLink(e -> e.getSource().getMsgId(), CtxVarTypes.STRING, "damage_type")
                .addVariableLink(e -> (double) e.getAmount(), (e, c) -> e.setAmount(c.floatValue()), CtxVarTypes.DOUBLE, "damage_amount");
        
        register(BuiltinEvents.LIVING_HURT_VICTIM.activation, LivingHurtEvent.class)
                .addTargetLink(e -> e.getSource().getEntity() != null ? Target.of(e.getSource().getEntity()) : null, "attacker")
                .addVariableLink(e -> e.getSource().getMsgId(), CtxVarTypes.STRING, "damage_type")
                .addVariableLink(e -> (double) e.getAmount(), (e, c) -> e.setAmount(c.floatValue()), CtxVarTypes.DOUBLE, "damage_amount");
        
        register(BuiltinEvents.LIVING_DAMAGE_ATTACKER.activation, LivingDamageEvent.class, event -> Optional.ofNullable(event.getSource()).map(DamageSource::getEntity))
                .addTargetLink(e -> Target.of(e.getEntity()), "victim")
                .addVariableLink(e -> e.getSource().getMsgId(), CtxVarTypes.STRING, "damage_type")
                .addVariableLink(e -> (double) e.getAmount(), (e, c) -> e.setAmount(c.floatValue()), CtxVarTypes.DOUBLE, "damage_amount");
        
        register(BuiltinEvents.LIVING_DAMAGE_VICTIM.activation, LivingDamageEvent.class)
                .addTargetLink(e -> e.getSource().getEntity() != null ? Target.of(e.getSource().getEntity()) : null, "attacker")
                .addVariableLink(e -> e.getSource().getMsgId(), CtxVarTypes.STRING, "damage_type")
                .addVariableLink(e -> (double) e.getAmount(), (e, c) -> e.setAmount(c.floatValue()), CtxVarTypes.DOUBLE, "damage_amount");
    }
    
    public static <E extends Event> RegisteredEvent<E> register(String eventId, Class<E> eventClass, Function<E, Optional<Entity>> playerGetter)
    {
        RegisteredEvent<E> registeredEvent = new RegisteredEvent<>(eventId, eventClass);
        NAME_TO_ENTRY.put(eventId, registeredEvent);
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, true, eventClass, event ->
        {
            playerGetter.apply(event).ifPresent(entity ->
            {
                Consumer<SpellContext> toContext = ctx ->
                {
                    if(event instanceof ICancellableEvent ce)
                    {
                        ctx.setCtxVar(CtxVarTypes.BOOLEAN.get(), BuiltinVariables.EVENT_IS_CANCELED.name, ce.isCanceled());
                    }
                    registeredEvent.getTargetLinks().forEach(link -> link.toContext(ctx, event));
                    registeredEvent.getVariableLinks().forEach(link -> link.toContext(ctx, event));
                };
                
                Consumer<SpellContext> fromContext = ctx ->
                {
                    if(event instanceof ICancellableEvent ce)
                    {
                        ctx.getCtxVar(CtxVarTypes.BOOLEAN.get(), BuiltinVariables.EVENT_IS_CANCELED.name).ifPresent(ce::setCanceled);
                    }
                    registeredEvent.getVariableLinks().forEach(link -> link.fromContext(ctx, event));
                };
                
                DelayedSpellHolder.getHolder(entity).ifPresent(delayedSpellHolder ->
                {
                    delayedSpellHolder.activateEvent(eventId, toContext, fromContext);
                });
                
                if(entity instanceof Player player && !entity.level().isClientSide)
                {
                    SpellHolder.getSpellHolder(player).ifPresent(spellHolder ->
                    {
                        for(int i = 0; i < spellHolder.getSlots(); i++)
                        {
                            SpellInstance spell = spellHolder.getSpell(i);
                            if(spell != null)
                            {
                                spell.run(player, eventId, toContext, fromContext);
                            }
                        }
                    });
                }
            });
        });
        return registeredEvent;
    }
    
    public static <E extends EntityEvent> RegisteredEvent<E> register(String eventId, Class<E> eventClass)
    {
        return register(eventId, eventClass, event -> Optional.of(event.getEntity()));
    }
    
    private static class RegisteredEvent<E extends Event>
    {
        public final String eventId;
        public final Class<E> eventClass;
        private List<TargetLink<E, ?>> targetLinks;
        private List<VariableLink<E, ?>> variableLinks;
        
        public RegisteredEvent(String eventId, Class<E> eventClass)
        {
            this.eventId = eventId;
            this.eventClass = eventClass;
            targetLinks = new LinkedList<>();
            variableLinks = new LinkedList<>();
        }
        
        public <C extends Target> RegisteredEvent<E> addTargetLink(Function<E, C> getter, String targetGroup)
        {
            targetLinks.add(new TargetLink<>(getter, targetGroup));
            return this;
        }
        
        public <C> RegisteredEvent<E> addVariableLink(Function<E, C> getter, BiConsumer<E, C> setter, Supplier<CtxVarType<C>> varType, String varName)
        {
            variableLinks.add(new VariableLink<>(getter, setter, varType, varName));
            return this;
        }
        
        public <C> RegisteredEvent<E> addVariableLink(Function<E, C> getter, Supplier<CtxVarType<C>> varType, String varName)
        {
            return addVariableLink(getter, (e, c) -> {}, varType, varName);
        }
        
        protected List<TargetLink<E, ?>> getTargetLinks()
        {
            return targetLinks;
        }
        
        protected List<VariableLink<E, ?>> getVariableLinks()
        {
            return variableLinks;
        }
    }
    
    private static record TargetLink<E extends Event, C extends Target>(Function<E, C> getter, String targetGroup)
    {
        public void toContext(SpellContext ctx, E e)
        {
            C c = getter.apply(e);
            if(c != null)
            {
                ctx.getOrCreateTargetGroup(targetGroup).addTargets(c);
            }
        }
    }
    
    private static record VariableLink<E extends Event, C>(Function<E, C> getter, BiConsumer<E, C> setter,
                                                           Supplier<CtxVarType<C>> varType, String varName)
    {
        public void toContext(SpellContext ctx, E e)
        {
            C c = getter.apply(e);
            if(c != null)
            {
                ctx.setCtxVar(varType.get(), varName, c);
            }
        }
        
        public void fromContext(SpellContext ctx, E e)
        {
            ctx.getCtxVar(varType.get(), varName).ifPresent(c ->
            {
                setter.accept(e, c);
            });
        }
    }
}
