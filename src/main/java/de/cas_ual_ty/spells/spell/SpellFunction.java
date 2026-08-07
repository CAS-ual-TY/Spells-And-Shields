package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.variable.CtxVar;

import java.util.LinkedList;
import java.util.List;

/**
 * A reusable, named sequence of {@link SpellAction}s, callable from a spell (or another function) via
 * {@code call_function}. Unlike {@link Spell}, a function has no icon/title/mana cost/tooltip/events of its own -
 * it is purely a body of actions, entered directly at whatever activation the caller was on when it called in.
 * <p>
 * {@link #parameters} are default variable values initialized right as the function starts - after the calling
 * {@code call_function}'s input maps have already renamed whatever the caller passed in, and before any of the
 * function's own actions run. Being applied after (not before) that rename step means a parameter's default
 * unconditionally overwrites anything the caller's rename-in already placed under that same internal name - the
 * function's own declared starting values always win for its own internal names, so its working variables are
 * never silently influenced by whatever the caller mapped in. They are declared purely in terms of the function's
 * own internal names (there is nothing to translate on the way in), but are ordinary context variables like any
 * other once the function is running, so they carry back out through the caller's output maps exactly the same
 * as anything else the function's actions touched.
 */
public class SpellFunction
{
    protected List<CtxVar<?>> parameters;
    protected List<SpellAction> actions;

    public SpellFunction(List<CtxVar<?>> parameters, List<SpellAction> actions)
    {
        this.parameters = parameters;
        this.actions = actions;
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
}
