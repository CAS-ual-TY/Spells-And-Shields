package de.cas_ual_ty.spells.spell.icon;

import de.cas_ual_ty.spells.registers.SpellIconTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;

public abstract class SpellIcon
{
    public final SpellIconType<?> type;

    public SpellIcon(SpellIconType<?> type)
    {
        this.type = type;
    }

    public SpellIconType<?> getType()
    {
        return type;
    }

    public abstract void readFromBuf(RegistryFriendlyByteBuf buf);

    public abstract void writeToBuf(RegistryFriendlyByteBuf buf);

    public static void iconToBuf(RegistryFriendlyByteBuf buf, SpellIcon icon)
    {
        buf.writeById(SpellIconTypes.REGISTRY::getId, icon.getType());
        icon.writeToBuf(buf);
    }

    public static SpellIcon iconFromBuf(RegistryFriendlyByteBuf buf)
    {
        SpellIconType<?> iconType = buf.readById(SpellIconTypes.REGISTRY::byId);
        SpellIcon icon = iconType.makeInstance();
        icon.readFromBuf(buf);
        return icon;
    }
}
