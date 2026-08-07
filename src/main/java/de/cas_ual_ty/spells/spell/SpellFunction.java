package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.spell.action.SpellAction;

import java.util.List;

/**
 * A reusable, named sequence of {@link SpellAction}s, callable from a spell (or another function) via
 * {@code call_function}. Unlike {@link Spell}, a function has no icon/title/mana cost/tooltip/events of its own -
 * it is purely a body of actions, entered directly at whatever activation the caller was on when it called in.
 */
public class SpellFunction
{
    protected List<SpellAction> actions;

    public SpellFunction(List<SpellAction> actions)
    {
        this.actions = actions;
    }

    public List<SpellAction> getActions()
    {
        return actions;
    }
}
