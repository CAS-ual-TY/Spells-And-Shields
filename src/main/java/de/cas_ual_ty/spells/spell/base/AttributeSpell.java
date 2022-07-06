package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class AttributeSpell extends Spell implements IEquipSpell
{
    public AttributeSpell()
    {
        super(0.0F);
    }
    
    @Override
    public boolean canActivate(ManaHolder manaHolder)
    {
        return false;
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
    }
    
    @Override
    public void onEquip(SpellHolder spellHolder, int slot)
    {
        Player player = spellHolder.getPlayer();
        AttributeInstance ai = player.getAttribute(Attributes.MAX_HEALTH);
        
        if(ai != null && !spellHolder.getPlayer().level.isClientSide)
        {
            ai.addPermanentModifier(new AttributeModifier(SpellsUtil.generateUUIDFromName("health_boost:" + slot), "hb", 2.0D, AttributeModifier.Operation.ADDITION));
        }
    }
    
    @Override
    public void onUnequip(SpellHolder spellHolder, int slot)
    {
        Player player = spellHolder.getPlayer();
        AttributeInstance ai = player.getAttribute(Attributes.MAX_HEALTH);
        
        if(ai != null && !spellHolder.getPlayer().level.isClientSide)
        {
            ai.removePermanentModifier(SpellsUtil.generateUUIDFromName("health_boost:" + slot));
        }
    }
}
