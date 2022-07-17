package de.cas_ual_ty.spells.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.spell.base.ISpell;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class SpellCommand
{
    public static final String SPELLS_PROGRESSION_LEARN_SINGLE = "spells.progression.learn.success.single";
    public static final String SPELLS_PROGRESSION_LEARN_MULTIPLE = "spells.progression.learn.success.multiple";
    public static final String SPELLS_PROGRESSION_LEARN_ALL_SINGLE = "spells.progression.learn_all.success.single";
    public static final String SPELLS_PROGRESSION_LEARN_ALL_SINGLE_FAILED = "spells.progression.learn_all.failed.single";
    public static final String SPELLS_PROGRESSION_LEARN_ALL_MULTIPLE = "spells.progression.learn_all.success.multiple";
    public static final String SPELLS_PROGRESSION_FORGET_SINGLE = "spells.progression.forget.success.single";
    public static final String SPELLS_PROGRESSION_FORGET_MULTIPLE = "spells.progression.forget.success.multiple";
    public static final String SPELLS_PROGRESSION_FORGET_ALL_SINGLE = "spells.progression.forget_all.success.single";
    public static final String SPELLS_PROGRESSION_FORGET_ALL_SINGLE_FAILED = "spells.progression.forget_all.failed.single";
    public static final String SPELLS_PROGRESSION_FORGET_ALL_MULTIPLE = "spells.progression.forget_all.success.multiple";
    public static final String SPELLS_PROGRESSION_RESET_SINGLE = "spells.progression.reset.success.single";
    public static final String SPELLS_PROGRESSION_RESET_MULTIPLE = "spells.progression.reset.success.multiple";
    public static final String SPELLS_SLOT_REMOVE_SINGLE = "spells.slot.remove.success.single";
    public static final String SPELLS_SLOT_REMOVE_MULTIPLE = "spells.slot.remove.success.multiple";
    public static final String SPELLS_SLOT_SET_SINGLE = "spells.slot.set.success.single";
    public static final String SPELLS_SLOT_SET_MULTIPLE = "spells.slot.set.success.multiple";
    public static final String SPELLS_SLOT_CLEAR_SINGLE = "spells.slot.clear.success.single";
    public static final String SPELLS_SLOT_CLEAR_MULTIPLE = "spells.slot.clear.success.multiple";
    
    public static final String ARG_TARGETS = "targets";
    public static final String ARG_SPELL = "spell";
    public static final String ARG_SLOT = "slot";
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext cbx)
    {
        dispatcher.register(Commands.literal("spells").requires(css -> css.hasPermission(2))
                .then(Commands.literal("progression")
                        .then(Commands.literal("learn")
                                .then(Commands.argument(ARG_TARGETS, EntityArgument.players())
                                        .then(Commands.argument(ARG_SPELL, SpellArgument.spell(cbx)).executes(SpellCommand::spellsProgressionLearn))
                                        .then(Commands.argument("all", StringArgumentType.string()).executes(SpellCommand::spellsProgressionLearnAll))
                                )
                        )
                        .then(Commands.literal("forget")
                                .then(Commands.argument(ARG_TARGETS, EntityArgument.players())
                                        .then(Commands.argument(ARG_SPELL, SpellArgument.spell(cbx)).executes(SpellCommand::spellsProgressionForget))
                                        .then(Commands.argument("all", StringArgumentType.string()).executes(SpellCommand::spellsProgressionForgetAll))
                                )
                        )
                        .then(Commands.literal("reset").then(Commands.argument(ARG_TARGETS, EntityArgument.players()).executes(SpellCommand::spellsProgressionReset)))
                )
                .then(Commands.literal("slots")
                        .then(Commands.literal("set").then(Commands.argument(ARG_TARGETS, EntityArgument.players()).then(Commands.argument(ARG_SLOT, IntegerArgumentType.integer(0, SpellHolder.SPELL_SLOTS)).then(Commands.argument(ARG_SPELL, SpellArgument.spell(cbx)).executes(SpellCommand::spellsSlotSet)))))
                        .then(Commands.literal("remove").then(Commands.argument(ARG_TARGETS, EntityArgument.players()).then(Commands.argument(ARG_SLOT, IntegerArgumentType.integer(0, SpellHolder.SPELL_SLOTS)).executes(SpellCommand::spellsSlotRemove))))
                        .then(Commands.literal("clear").then(Commands.argument(ARG_TARGETS, EntityArgument.players()).executes(SpellCommand::spellsSlotClear)))
                )
        );
    }
    
    private static int spellsProgressionLearn(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, ARG_TARGETS);
        ISpell spell = SpellArgument.getSpell(context, ARG_SPELL);
        
        if(players.size() == 0)
        {
            return 0;
        }
        
        players.stream().map(SpellProgressionHolder::getSpellProgressionHolder).forEach(lazyOptional ->
        {
            lazyOptional.ifPresent(spellProgressionHolder ->
            {
                spellProgressionHolder.setSpellStatus(spell, SpellStatus.LEARNED);
            });
        });
        
        if(players.size() == 1)
        {
            context.getSource().sendSuccess(Component.translatable(SPELLS_PROGRESSION_LEARN_SINGLE, spell.getSpellName(), players.iterator().next().getDisplayName()), true);
        }
        else
        {
            context.getSource().sendSuccess(Component.translatable(SPELLS_PROGRESSION_LEARN_MULTIPLE, spell.getSpellName(), players.size()), true);
        }
        
        return players.size();
    }
    
    private static int spellsProgressionLearnAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, ARG_TARGETS);
        
        if(players.size() == 0)
        {
            return 0;
        }
        
        boolean single = players.size() == 1;
        AtomicInteger learned = new AtomicInteger(0);
        
        players.stream().map(SpellProgressionHolder::getSpellProgressionHolder).forEach(lazyOptional ->
        {
            lazyOptional.ifPresent(spellProgressionHolder ->
            {
                SpellsUtil.forEachSpell((key, spell) ->
                {
                    if(single)
                    {
                        if(spellProgressionHolder.getSpellStatus(spell) != SpellStatus.LEARNED)
                        {
                            learned.getAndIncrement();
                        }
                    }
                    
                    spellProgressionHolder.setSpellStatus(spell, SpellStatus.LEARNED);
                });
            });
        });
        
        if(players.size() == 1)
        {
            if(learned.get() > 0)
            {
                context.getSource().sendSuccess(Component.translatable(SPELLS_PROGRESSION_LEARN_ALL_SINGLE, learned.get(), players.iterator().next().getDisplayName()), true);
            }
            else
            {
                context.getSource().sendFailure(Component.translatable(SPELLS_PROGRESSION_LEARN_ALL_SINGLE_FAILED, players.iterator().next().getDisplayName()));
            }
        }
        else
        {
            final int amount = SpellsUtil.getSpellsAmount();
            context.getSource().sendSuccess(Component.translatable(SPELLS_PROGRESSION_LEARN_ALL_MULTIPLE, amount, players.size()), true);
        }
        
        return players.size();
    }
    
    private static int spellsProgressionForget(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, ARG_TARGETS);
        ISpell spell = SpellArgument.getSpell(context, ARG_SPELL);
        
        if(players.size() == 0)
        {
            return 0;
        }
        
        players.stream().map(SpellProgressionHolder::getSpellProgressionHolder).forEach(lazyOptional ->
        {
            lazyOptional.ifPresent(spellProgressionHolder ->
            {
                if(spellProgressionHolder.getSpellStatus(spell) == SpellStatus.LEARNED)
                {
                    spellProgressionHolder.setSpellStatus(spell, SpellStatus.FORGOTTEN);
                }
                
                SpellHolder.getSpellHolder(spellProgressionHolder.getPlayer()).ifPresent(spellHolder ->
                {
                    boolean changed = false;
                    
                    for(int i = 0; i < spellHolder.getSlots(); ++i)
                    {
                        if(spellHolder.getSpell(i) == spell)
                        {
                            spellHolder.setSpell(i, null);
                            changed = true;
                        }
                    }
                    
                    if(changed)
                    {
                        spellHolder.sendSync();
                    }
                });
            });
        });
        
        if(players.size() == 1)
        {
            context.getSource().sendSuccess(Component.translatable(SPELLS_PROGRESSION_FORGET_SINGLE, spell.getSpellName(), players.iterator().next().getDisplayName()), true);
        }
        else
        {
            context.getSource().sendSuccess(Component.translatable(SPELLS_PROGRESSION_FORGET_MULTIPLE, spell.getSpellName(), players.size()), true);
        }
        
        return players.size();
    }
    
    private static int spellsProgressionForgetAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, ARG_TARGETS);
        
        if(players.size() == 0)
        {
            return 0;
        }
        
        boolean single = players.size() == 1;
        AtomicInteger forgotten = new AtomicInteger(0);
        
        players.stream().map(SpellProgressionHolder::getSpellProgressionHolder).forEach(lazyOptional ->
        {
            lazyOptional.ifPresent(spellProgressionHolder ->
            {
                SpellsUtil.forEachSpell((key, spell) ->
                {
                    if(spellProgressionHolder.getSpellStatus(spell) == SpellStatus.LEARNED)
                    {
                        spellProgressionHolder.setSpellStatus(spell, SpellStatus.FORGOTTEN);
                        
                        if(single)
                        {
                            forgotten.getAndIncrement();
                        }
                    }
                });
                
                SpellHolder.getSpellHolder(spellProgressionHolder.getPlayer()).ifPresent(spellHolder ->
                {
                    spellHolder.clear();
                    spellHolder.sendSync();
                });
            });
        });
        
        if(single)
        {
            if(forgotten.get() > 0)
            {
                context.getSource().sendSuccess(Component.translatable(SPELLS_PROGRESSION_FORGET_ALL_SINGLE, forgotten.get(), players.iterator().next().getDisplayName()), true);
            }
            else
            {
                context.getSource().sendFailure(Component.translatable(SPELLS_PROGRESSION_FORGET_ALL_SINGLE_FAILED, players.iterator().next().getDisplayName()));
            }
        }
        else
        {
            final int amount = SpellsUtil.getSpellsAmount();
            context.getSource().sendSuccess(Component.translatable(SPELLS_PROGRESSION_FORGET_ALL_MULTIPLE, amount, players.size()), true);
        }
        
        return players.size();
    }
    
    private static int spellsProgressionReset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, ARG_TARGETS);
        
        if(players.size() == 0)
        {
            return 0;
        }
        
        players.stream().map(SpellProgressionHolder::getSpellProgressionHolder).forEach(lazyOptional ->
        {
            lazyOptional.ifPresent(spellProgressionHolder ->
            {
                spellProgressionHolder.getProgression().clear();
                
                SpellHolder.getSpellHolder(spellProgressionHolder.getPlayer()).ifPresent(spellHolder ->
                {
                    spellHolder.clear();
                    spellHolder.sendSync();
                });
            });
        });
        
        if(players.size() == 1)
        {
            context.getSource().sendSuccess(Component.translatable(SPELLS_PROGRESSION_RESET_SINGLE, players.iterator().next().getDisplayName()), true);
        }
        else
        {
            context.getSource().sendSuccess(Component.translatable(SPELLS_PROGRESSION_RESET_MULTIPLE, players.size()), true);
        }
        
        return 0;
    }
    
    private static int spellsSlotSet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, ARG_TARGETS);
        int slot = IntegerArgumentType.getInteger(context, ARG_SLOT);
        ISpell spell = SpellArgument.getSpell(context, ARG_SPELL);
        
        if(players.size() == 0)
        {
            return 0;
        }
        
        players.stream().map(SpellHolder::getSpellHolder).forEach(lazyOptional ->
        {
            lazyOptional.ifPresent(spellHolder ->
            {
                spellHolder.setSpell(slot, spell);
                spellHolder.sendSync();
            });
        });
        
        if(players.size() == 1)
        {
            context.getSource().sendSuccess(Component.translatable(SPELLS_SLOT_SET_SINGLE, slot, players.iterator().next().getDisplayName(), spell.getSpellName()), true);
        }
        else
        {
            context.getSource().sendSuccess(Component.translatable(SPELLS_SLOT_SET_MULTIPLE, slot, players.size(), spell.getSpellName()), true);
        }
        
        return players.size();
    }
    
    private static int spellsSlotRemove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, ARG_TARGETS);
        int slot = IntegerArgumentType.getInteger(context, ARG_SLOT);
        
        if(players.size() == 0)
        {
            return 0;
        }
        
        players.stream().map(SpellHolder::getSpellHolder).forEach(lazyOptional ->
        {
            lazyOptional.ifPresent(spellHolder ->
            {
                spellHolder.setSpell(slot, null);
                spellHolder.sendSync();
            });
        });
        
        if(players.size() == 1)
        {
            context.getSource().sendSuccess(Component.translatable(SPELLS_SLOT_REMOVE_SINGLE, slot, players.iterator().next().getDisplayName()), true);
        }
        else
        {
            context.getSource().sendSuccess(Component.translatable(SPELLS_SLOT_REMOVE_MULTIPLE, slot, players.size()), true);
        }
        
        return players.size();
    }
    
    private static int spellsSlotClear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, ARG_TARGETS);
        
        if(players.size() == 0)
        {
            return 0;
        }
        
        players.stream().map(SpellHolder::getSpellHolder).forEach(lazyOptional ->
        {
            lazyOptional.ifPresent(spellHolder ->
            {
                spellHolder.clear();
                spellHolder.sendSync();
            });
        });
        
        if(players.size() == 1)
        {
            context.getSource().sendSuccess(Component.translatable(SPELLS_SLOT_CLEAR_SINGLE, players.iterator().next().getDisplayName()), true);
        }
        else
        {
            context.getSource().sendSuccess(Component.translatable(SPELLS_SLOT_CLEAR_MULTIPLE, players.size()), true);
        }
        
        return players.size();
    }
}
