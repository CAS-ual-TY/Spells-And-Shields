package de.cas_ual_ty.spells.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.registers.SpellTrees;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spelltree.SpellNode;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SpellCommand
{
    public static final String SPELLS_PROGRESSION_LEARN_SINGLE = "spells.progression.learn.success.single";
    public static final String SPELLS_PROGRESSION_LEARN_SINGLE_FAILED = "spells.progression.learn.failed.single";
    public static final String SPELLS_PROGRESSION_LEARN_MULTIPLE = "spells.progression.learn.success.multiple";
    public static final String SPELLS_PROGRESSION_LEARN_TREE_SINGLE = "spells.progression.learn_tree.success.single";
    public static final String SPELLS_PROGRESSION_LEARN_TREE_SINGLE_FAILED = "spells.progression.learn_tree.failed.single";
    public static final String SPELLS_PROGRESSION_LEARN_TREE_MULTIPLE = "spells.progression.learn_tree.success.multiple";
    public static final String SPELLS_PROGRESSION_LEARN_ALL_SINGLE = "spells.progression.learn_all.success.single";
    public static final String SPELLS_PROGRESSION_LEARN_ALL_SINGLE_FAILED = "spells.progression.learn_all.failed.single";
    public static final String SPELLS_PROGRESSION_LEARN_ALL_MULTIPLE = "spells.progression.learn_all.success.multiple";
    public static final String SPELLS_PROGRESSION_FORGET_SINGLE = "spells.progression.forget.success.single";
    public static final String SPELLS_PROGRESSION_FORGET_SINGLE_FAILED = "spells.progression.forget.failed.single";
    public static final String SPELLS_PROGRESSION_FORGET_MULTIPLE = "spells.progression.forget.success.multiple";
    public static final String SPELLS_PROGRESSION_FORGET_TREE_SINGLE = "spells.progression.forget_tree.success.single";
    public static final String SPELLS_PROGRESSION_FORGET_TREE_SINGLE_FAILED = "spells.progression.forget_tree.failed.single";
    public static final String SPELLS_PROGRESSION_FORGET_TREE_MULTIPLE = "spells.progression.forget_tree.success.multiple";
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
    public static final String ARG_SPELL_TREE = "spell_tree";
    public static final String ARG_NODE_ID = "node_id";
    public static final String ARG_SLOT = "slot";
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext cbx)
    {
        dispatcher.register(Commands.literal("spells").requires(css -> css.hasPermission(2))
                .then(Commands.literal("progression")
                        .then(Commands.literal("learn")
                                .then(Commands.argument(ARG_TARGETS, EntityArgument.players())
                                        .then(Commands.argument(ARG_SPELL_TREE, SpellTreeArgument.spellTree(cbx))
                                                .then(Commands.argument(ARG_NODE_ID, IntegerArgumentType.integer(1))
                                                        .executes(SpellCommand::spellsProgressionLearn)
                                                )
                                                .then(Commands.argument("all", StringArgumentType.string())
                                                        .executes(SpellCommand::spellsProgressionLearnTree)
                                                )
                                        )
                                        .then(Commands.argument("all", StringArgumentType.string())
                                                .executes(SpellCommand::spellsProgressionLearnAll)
                                        )
                                )
                        )
                        .then(Commands.literal("forget")
                                .then(Commands.argument(ARG_TARGETS, EntityArgument.players())
                                        .then(Commands.argument(ARG_SPELL_TREE, SpellTreeArgument.spellTree(cbx))
                                                .then(Commands.argument(ARG_SPELL, SpellArgument.spell(cbx))
                                                        .executes(SpellCommand::spellsProgressionForget)
                                                )
                                                .then(Commands.argument("all", StringArgumentType.string())
                                                        .executes(SpellCommand::spellsProgressionForgetTree)
                                                )
                                        )
                                        .then(Commands.argument("all", StringArgumentType.string())
                                                .executes(SpellCommand::spellsProgressionForgetAll)
                                        )
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
    
    private static int setOneOfTree(CommandContext<CommandSourceStack> context, SpellStatus status, String singleKey, String singleFailedKey, String multipleKey) throws CommandSyntaxException
    {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, ARG_TARGETS);
        SpellTree spellTree = SpellTreeArgument.getSpellTree(context, ARG_SPELL_TREE);
        int nodeId = IntegerArgumentType.getInteger(context, ARG_NODE_ID);
        
        SpellNode node = spellTree.findNode(nodeId);
        
        if(node == null)
        {
            return 0; //TODO message
        }
        
        boolean single = players.size() == 1;
        AtomicBoolean changed = new AtomicBoolean(false);
        
        players.stream().map(SpellProgressionHolder::getSpellProgressionHolder).forEach(lazyOptional ->
        {
            lazyOptional.ifPresent(spellProgressionHolder ->
            {
                if(spellProgressionHolder.getSpellStatus(node.getId()) != status)
                {
                    changed.set(true);
                }
                
                spellProgressionHolder.setSpellStatus(node.getId(), status);
            });
        });
        
        if(players.size() == 1)
        {
            if(changed.get())
            {
                context.getSource().sendSuccess(Component.translatable(singleKey, node.getSpellDirect().getTitle(), players.iterator().next().getDisplayName()), true);
            }
            else
            {
                context.getSource().sendFailure(Component.translatable(singleFailedKey, node.getSpellDirect().getTitle(), players.iterator().next().getDisplayName()));
            }
        }
        else
        {
            context.getSource().sendSuccess(Component.translatable(multipleKey, node.getSpellDirect().getTitle(), players.size()), true);
        }
        
        return players.size();
    }
    
    private static int setAllOfTree(CommandContext<CommandSourceStack> context, SpellStatus status, String singleKey, String singleFailedKey, String multipleKey) throws CommandSyntaxException
    {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, ARG_TARGETS);
        SpellTree spellTree = SpellTreeArgument.getSpellTree(context, ARG_SPELL_TREE);
        
        if(players.size() == 0)
        {
            return 0;
        }
        
        boolean single = players.size() == 1;
        AtomicInteger changed = new AtomicInteger(0);
        
        players.stream().map(SpellProgressionHolder::getSpellProgressionHolder).forEach(lazyOptional ->
        {
            lazyOptional.ifPresent(spellProgressionHolder ->
            {
                spellTree.forEach(node ->
                {
                    if(single && spellProgressionHolder.getSpellStatus(node.getId()) != status)
                    {
                        changed.getAndIncrement();
                    }
                    
                    spellProgressionHolder.setSpellStatus(node.getId(), status);
                });
            });
        });
        
        if(players.size() == 1)
        {
            if(changed.get() > 0)
            {
                context.getSource().sendSuccess(Component.translatable(singleKey, spellTree.getTitle(), players.iterator().next().getDisplayName()), true);
            }
            else
            {
                context.getSource().sendFailure(Component.translatable(singleFailedKey, spellTree.getTitle(), players.iterator().next().getDisplayName()));
            }
        }
        else
        {
            context.getSource().sendSuccess(Component.translatable(multipleKey, spellTree.getTitle(), players.size()), true);
        }
        
        return players.size();
    }
    
    private static int setForAllTrees(CommandContext<CommandSourceStack> context, SpellStatus status, String singleKey, String singleFailedKey, String multipleKey) throws CommandSyntaxException
    {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, ARG_TARGETS);
        Registry<SpellTree> registry = SpellTrees.getRegistry(context.getSource().getLevel());
        
        int totalTrees = registry.size();
        AtomicInteger totalSpells = new AtomicInteger(0);
        registry.forEach(tree -> tree.forEach(node -> totalSpells.getAndIncrement()));
        
        boolean single = players.size() == 1;
        AtomicInteger learned = new AtomicInteger(0);
        
        players.stream().map(SpellProgressionHolder::getSpellProgressionHolder).forEach(lazyOptional ->
        {
            lazyOptional.ifPresent(spellProgressionHolder ->
            {
                registry.forEach(spellTree ->
                {
                    spellTree.forEach(spellNode ->
                    {
                        if(single && spellProgressionHolder.getSpellStatus(spellNode.getId()) != status)
                        {
                            learned.getAndIncrement();
                        }
                        
                        spellProgressionHolder.setSpellStatus(spellNode.getId(), status);
                    });
                });
            });
        });
        
        if(single)
        {
            if(learned.get() > 0)
            {
                context.getSource().sendSuccess(Component.translatable(singleKey, learned.get(), totalTrees, players.iterator().next().getDisplayName()), true);
            }
            else
            {
                context.getSource().sendFailure(Component.translatable(singleFailedKey, players.iterator().next().getDisplayName()));
            }
        }
        else
        {
            context.getSource().sendSuccess(Component.translatable(multipleKey, totalSpells, totalTrees, players.size()), true);
        }
        
        return players.size();
    }
    
    private static int spellsProgressionLearn(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        return setOneOfTree(context, SpellStatus.LEARNED, SPELLS_PROGRESSION_LEARN_SINGLE, SPELLS_PROGRESSION_LEARN_SINGLE_FAILED, SPELLS_PROGRESSION_LEARN_MULTIPLE);
    }
    
    private static int spellsProgressionLearnTree(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        return setAllOfTree(context, SpellStatus.LEARNED, SPELLS_PROGRESSION_LEARN_TREE_SINGLE, SPELLS_PROGRESSION_LEARN_TREE_SINGLE_FAILED, SPELLS_PROGRESSION_LEARN_TREE_MULTIPLE);
    }
    
    private static int spellsProgressionLearnAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        return setForAllTrees(context, SpellStatus.LEARNED, SPELLS_PROGRESSION_LEARN_ALL_SINGLE, SPELLS_PROGRESSION_LEARN_ALL_SINGLE_FAILED, SPELLS_PROGRESSION_LEARN_ALL_MULTIPLE);
    }
    
    private static int spellsProgressionForget(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        return setOneOfTree(context, SpellStatus.FORGOTTEN, SPELLS_PROGRESSION_FORGET_SINGLE, SPELLS_PROGRESSION_FORGET_SINGLE_FAILED, SPELLS_PROGRESSION_FORGET_MULTIPLE);
    }
    
    private static int spellsProgressionForgetTree(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        return setAllOfTree(context, SpellStatus.FORGOTTEN, SPELLS_PROGRESSION_FORGET_TREE_SINGLE, SPELLS_PROGRESSION_FORGET_TREE_SINGLE_FAILED, SPELLS_PROGRESSION_FORGET_TREE_MULTIPLE);
    }
    
    private static int spellsProgressionForgetAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        return setForAllTrees(context, SpellStatus.FORGOTTEN, SPELLS_PROGRESSION_FORGET_ALL_SINGLE, SPELLS_PROGRESSION_FORGET_ALL_SINGLE_FAILED, SPELLS_PROGRESSION_FORGET_ALL_MULTIPLE);
    }
    
    private static int spellsProgressionReset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, ARG_TARGETS);
        
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
        Spell spell = SpellArgument.getSpell(context, ARG_SPELL);
        
        if(players.size() == 0)
        {
            return 0;
        }
        
        players.stream().map(SpellHolder::getSpellHolder).forEach(lazyOptional ->
        {
            lazyOptional.ifPresent(spellHolder ->
            {
                spellHolder.setSpell(slot, new SpellInstance(Holder.direct(spell)));
                spellHolder.sendSync();
            });
        });
        
        if(players.size() == 1)
        {
            context.getSource().sendSuccess(Component.translatable(SPELLS_SLOT_SET_SINGLE, slot, players.iterator().next().getDisplayName(), spell.getTitle()), true);
        }
        else
        {
            context.getSource().sendSuccess(Component.translatable(SPELLS_SLOT_SET_MULTIPLE, slot, players.size(), spell.getTitle()), true);
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
