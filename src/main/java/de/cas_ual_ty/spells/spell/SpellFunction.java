package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.variable.CtxVar;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A reusable, named sequence of {@link SpellAction}s, callable from a spell (or another function) via
 * {@code call_function}. Unlike {@link Spell}, a function has no icon/title/mana cost/tooltip/events of its own -
 * it is purely a body of actions, entered directly at whatever activation the caller was on when it called in.
 * <p>
 * {@link #parameters} are default variable values initialized right as the function starts, via
 * {@code initCtxVarIfAbsent} (see {@link de.cas_ual_ty.spells.spell.context.SpellContext#runNestedActions}) - so
 * a default only takes effect where nothing is already present under that name (eg. an ambient builtin the
 * caller never needed to map in, or a name the caller's own rename-in already populated). A caller's explicit
 * {@code call_function} {@code parameters} override still always wins, since that's applied afterward and
 * unconditionally.
 * <p>
 * {@link #defaultActivations}/{@link #defaultVariables}/{@link #defaultTargets} are fallback translation maps,
 * used by {@code call_function} in place of its own (caller-supplied) map of the same kind whenever the caller
 * left that particular map empty - each of the three is resolved independently. They exist so a function can
 * declare a sensible default correspondence for names it conventionally shares with any host (eg. the
 * {@code owner} target group), without every caller having to spell that mapping out explicitly. A function's
 * own purely-internal names (with no host-side equivalent to translate) don't need an entry here at all - they
 * simply aren't in any map, on either side.
 */
public class SpellFunction
{
    protected List<CtxVar<?>> parameters;
    protected List<SpellAction> actions;
    protected Map<String, String> defaultActivations;
    protected Map<String, String> defaultVariables;
    protected Map<String, String> defaultTargets;

    public SpellFunction(List<CtxVar<?>> parameters, List<SpellAction> actions, Map<String, String> defaultActivations, Map<String, String> defaultVariables, Map<String, String> defaultTargets)
    {
        this.parameters = parameters;
        this.actions = actions;
        this.defaultActivations = defaultActivations;
        this.defaultVariables = defaultVariables;
        this.defaultTargets = defaultTargets;
    }

    public SpellFunction(List<CtxVar<?>> parameters, List<SpellAction> actions)
    {
        this(parameters, actions, Map.of(), Map.of(), Map.of());
    }

    public SpellFunction(List<SpellAction> actions)
    {
        this(new LinkedList<>(), actions);
    }

    public List<CtxVar<?>> getParameters()
    {
        return parameters;
    }

    public List<SpellAction> getActions()
    {
        return actions;
    }

    public Map<String, String> getDefaultActivations()
    {
        return defaultActivations;
    }

    public Map<String, String> getDefaultVariables()
    {
        return defaultVariables;
    }

    public Map<String, String> getDefaultTargets()
    {
        return defaultTargets;
    }
}
