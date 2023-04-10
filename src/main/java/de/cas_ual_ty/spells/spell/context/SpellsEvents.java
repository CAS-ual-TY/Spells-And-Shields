package de.cas_ual_ty.spells.spell.context;

import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SpellsEvents
{
    public static final String PLAYER_BREAK_SPEED = "player_break_speed";
    
    public static final Map<String, RegisteredEvent<?>> NAME_TO_ENTRY = new HashMap<>();
    
    public static void registerEvents()
    {
        register(PLAYER_BREAK_SPEED, PlayerEvent.BreakSpeed.class, true)
                .addTargetLink(e -> e.getPosition().map(pos -> Target.of(e.getEntity().level, pos)).orElse(null), "block_position")
                .addVariableLink(e -> (double) e.getOriginalSpeed(), CtxVarTypes.DOUBLE, "original_speed")
                .addVariableLink(e -> (double) e.getNewSpeed(), (e, c) -> e.setNewSpeed(c.floatValue()), CtxVarTypes.DOUBLE, "new_speed");
    }
    
    public static <E extends EntityEvent> RegisteredEvent<E> register(String eventId, Class<E> eventClass, boolean includeClient)
    {
        RegisteredEvent<E> registeredEvent = new RegisteredEvent<>(eventId, eventClass);
        NAME_TO_ENTRY.put(eventId, registeredEvent);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, true, eventClass, event ->
        {
            /*DelayedSpellHolder.getHolder(event.getEntity()).ifPresent(delayedSpellHolder ->
            {
            
            });*/
            
            if(event.getEntity() instanceof Player player && (includeClient || !event.getEntity().level.isClientSide))
            {
                SpellHolder.getSpellHolder(player).ifPresent(spellHolder ->
                {
                    for(int i = 0; i < spellHolder.getSlots(); i++)
                    {
                        SpellInstance spell = spellHolder.getSpell(i);
                        if(spell != null)
                        {
                            spell.runEvent(player, eventId, ctx ->
                            {
                                if(event.isCancelable())
                                {
                                    ctx.setCtxVar(CtxVarTypes.BOOLEAN.get(), BuiltinVariables.EVENT_IS_CANCELED.name, event.isCanceled());
                                }
                                registeredEvent.getTargetLinks().forEach(link -> link.toContext(ctx, event));
                                registeredEvent.getVariableLinks().forEach(link -> link.toContext(ctx, event));
                            }, ctx ->
                            {
                                if(event.isCancelable())
                                {
                                    ctx.getCtxVar(CtxVarTypes.BOOLEAN.get(), BuiltinVariables.EVENT_IS_CANCELED.name).ifPresent(event::setCanceled);
                                }
                                registeredEvent.getVariableLinks().forEach(link -> link.fromContext(ctx, event));
                            });
                        }
                    }
                });
            }
        });
        return registeredEvent;
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
            this.targetLinks = new LinkedList<>();
            this.variableLinks = new LinkedList<>();
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
