package de.cas_ual_ty.spells.spell.context;

public enum BuiltinEvents
{
    ACTIVE("active"),
    ON_EQUIP("on_equip"),
    ON_UNEQUIP("on_unequip"),
    LIVING_INCOMING_DAMAGE_ATTACKER("living_incoming_damage_attacker"),
    LIVING_INCOMING_DAMAGE_VICTIM("living_incoming_damage_victim"),
    LIVING_DAMAGE_PRE_ATTACKER("living_damage_pre_attacker"),
    LIVING_DAMAGE_PRE_VICTIM("living_damage_pre_victim"),
    LIVING_DAMAGE_POST_ATTACKER("living_damage_post_attacker"),
    LIVING_DAMAGE_POST_VICTIM("living_damage_post_victim"),
    LIVING_CHANGE_TARGET("living_change_target"),
    OWNER_LEFT_DIMENSION("owner_left_dimension"),
    HOLDER_UNLOADED("holder_unloaded");

    public final String event;

    BuiltinEvents(String event)
    {
        this.event = event;
    }

    @Override
    public String toString()
    {
        return event;
    }
}
