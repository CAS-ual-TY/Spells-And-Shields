package de.cas_ual_ty.spells.spell.context;

import com.google.common.collect.Lists;
import de.cas_ual_ty.spells.capability.DelayedSpellHolder;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.capability.SpellsCapabilities;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

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
        // LivingIncomingDamageEvent: start of hurt(), before shield, i-frames and all reductions; cancelable
        registerDamageEvent(BuiltinEvents.LIVING_INCOMING_DAMAGE_ATTACKER.event, BuiltinEvents.LIVING_INCOMING_DAMAGE_VICTIM.event, LivingIncomingDamageEvent.class, LivingIncomingDamageEvent::getSource, registeredEvent -> registeredEvent
                .addVariableLink(e -> (double) e.getAmount(), (e, c) -> e.setAmount(c.floatValue()), CtxVarTypes.DOUBLE, "damage_amount")
                .addVariableLink(e -> (double) e.getOriginalAmount(), CtxVarTypes.DOUBLE, "original_damage_amount")
                .addVariableLink(e -> e.getContainer().getPostAttackInvulnerabilityTicks(), (e, c) -> e.setInvulnerabilityTicks(c), CtxVarTypes.INT, "invulnerability_ticks"));

        // LivingDamageEvent.Pre: past shield and i-frames, after armor, enchantment and mob effect reductions, before absorption; not cancelable
        registerDamageEvent(BuiltinEvents.LIVING_DAMAGE_PRE_ATTACKER.event, BuiltinEvents.LIVING_DAMAGE_PRE_VICTIM.event, LivingDamageEvent.Pre.class, LivingDamageEvent.Pre::getSource, registeredEvent ->
        {
            registeredEvent
                    .addVariableLink(e -> (double) e.getNewDamage(), (e, c) -> e.setNewDamage(c.floatValue()), CtxVarTypes.DOUBLE, "damage_amount")
                    .addVariableLink(e -> (double) e.getOriginalDamage(), CtxVarTypes.DOUBLE, "original_damage_amount")
                    .addVariableLink(e -> (double) e.getContainer().getBlockedDamage(), CtxVarTypes.DOUBLE, "blocked_damage");
            for(DamageContainer.Reduction reduction : DamageContainer.Reduction.values())
            {
                registeredEvent.addVariableLink(e -> (double) e.getContainer().getReduction(reduction), CtxVarTypes.DOUBLE, reduction.name().toLowerCase() + "_reduction");
            }
        });

        // LivingDamageEvent.Post: after health has been reduced, final values; read-only
        registerDamageEvent(BuiltinEvents.LIVING_DAMAGE_POST_ATTACKER.event, BuiltinEvents.LIVING_DAMAGE_POST_VICTIM.event, LivingDamageEvent.Post.class, LivingDamageEvent.Post::getSource, registeredEvent ->
        {
            registeredEvent
                    .addVariableLink(e -> (double) e.getNewDamage(), CtxVarTypes.DOUBLE, "damage_amount")
                    .addVariableLink(e -> (double) e.getOriginalDamage(), CtxVarTypes.DOUBLE, "original_damage_amount")
                    .addVariableLink(e -> (double) e.getBlockedDamage(), CtxVarTypes.DOUBLE, "blocked_damage")
                    .addVariableLink(e -> (double) e.getShieldDamage(), CtxVarTypes.DOUBLE, "shield_damage")
                    .addVariableLink(LivingDamageEvent.Post::getPostAttackInvulnerabilityTicks, CtxVarTypes.INT, "invulnerability_ticks");
            for(DamageContainer.Reduction reduction : DamageContainer.Reduction.values())
            {
                registeredEvent.addVariableLink(e -> (double) e.getReduction(reduction), CtxVarTypes.DOUBLE, reduction.name().toLowerCase() + "_reduction");
            }
        });

        register(BuiltinEvents.LIVING_CHANGE_TARGET.event, LivingChangeTargetEvent.class)
                .addTargetLink(e -> e.getNewAboutToBeSetTarget() != null ? Target.of(e.getNewAboutToBeSetTarget()) : null, "new_target");

        registerOwnerLeftDimensionEvent();
        registerHolderUnloadedEvent();
    }

    private static void registerOwnerLeftDimensionEvent()
    {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, LivingDeathEvent.class, event ->
        {
            if(event.isCanceled()) return;
            if(!(event.getEntity() instanceof Player)) return;
            fireOwnerLeftDimension(event.getEntity().level());
        });

        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, PlayerEvent.PlayerLoggedOutEvent.class, event ->
        {
            if(event.getEntity().getServer() == null) return;
            event.getEntity().getServer().getAllLevels().forEach(SpellsEvents::fireOwnerLeftDimension);
        });

        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, PlayerEvent.PlayerChangedDimensionEvent.class, event ->
        {
            if(event.getEntity().getServer() == null) return;
            ServerLevel fromLevel = event.getEntity().getServer().getLevel(event.getFrom());
            if(fromLevel != null) fireOwnerLeftDimension(fromLevel);
        });
    }

    private static void fireOwnerLeftDimension(Level level)
    {
        if(level.isClientSide()) return;
        if(!(level instanceof ServerLevel serverLevel)) return;
        Consumer<SpellContext> toContext = ctx -> {};
        Consumer<SpellContext> fromContext = ctx -> {};
        Lists.newArrayList(serverLevel.getAllEntities()).forEach(entity ->
        {
            if(entity.hasData(SpellsCapabilities.DELAYED_SPELL_HOLDER.get()))
            {
                DelayedSpellHolder.getHolder(entity).ifPresent(holder ->
                        holder.activateEvent(BuiltinEvents.OWNER_LEFT_DIMENSION.event, toContext, fromContext));
            }
        });
    }
    
    private static void registerHolderUnloadedEvent()
    {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, EntityLeaveLevelEvent.class, event ->
        {
            Entity entity = event.getEntity();
            if(entity.getRemovalReason() != Entity.RemovalReason.UNLOADED_TO_CHUNK) return;
            if(entity instanceof Player) return;
            if(!entity.hasData(SpellsCapabilities.DELAYED_SPELL_HOLDER.get())) return;

            Consumer<SpellContext> toContext = ctx -> {};
            Consumer<SpellContext> fromContext = ctx -> {};
            DelayedSpellHolder.getHolder(entity).ifPresent(holder ->
                    holder.activateEvent(BuiltinEvents.HOLDER_UNLOADED.event, toContext, fromContext));
        });
    }

    // registers the _attacker (owner = attacker) and _victim (owner = victim) variant of a damage event with shared links
    private static <E extends LivingEvent> void registerDamageEvent(String attackerEventId, String victimEventId, Class<E> eventClass, Function<E, DamageSource> sourceGetter, Consumer<RegisteredEvent<E>> extraLinks)
    {
        RegisteredEvent<E> attackerEvent = register(attackerEventId, eventClass, e -> Optional.ofNullable(sourceGetter.apply(e).getEntity()))
                .addTargetLink(e -> Target.of(e.getEntity()), "victim");

        RegisteredEvent<E> victimEvent = register(victimEventId, eventClass)
                .addTargetLink(e -> sourceGetter.apply(e).getEntity() != null ? Target.of(sourceGetter.apply(e).getEntity()) : null, "attacker");

        for(RegisteredEvent<E> registeredEvent : List.of(attackerEvent, victimEvent))
        {
            registeredEvent
                    .addTargetLink(e -> sourceGetter.apply(e).getDirectEntity() != null ? Target.of(sourceGetter.apply(e).getDirectEntity()) : null, "direct_attacker")
                    .addVariableLink(e -> sourceGetter.apply(e).typeHolder().unwrapKey().map(key -> key.location().toString()).orElse(null), CtxVarTypes.STRING, "damage_type");
            extraLinks.accept(registeredEvent);
        }
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
                                spell.runEquipped(player, eventId, i, toContext, fromContext);
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
