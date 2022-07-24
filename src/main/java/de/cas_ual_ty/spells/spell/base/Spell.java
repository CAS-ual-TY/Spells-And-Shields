package de.cas_ual_ty.spells.spell.base;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.util.ManaTooltipComponent;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.Optional;

public abstract class Spell extends BaseSpell implements IConfigurableSpell
{
    public final float defaultManaCost;
    
    protected float manaCost;
    
    protected Optional<TooltipComponent> tooltipComponent;
    
    public Spell(float manaCost)
    {
        this.defaultManaCost = manaCost;
    }
    
    public float getManaCost()
    {
        return manaCost;
    }
    
    // performed server side only
    public abstract void perform(ManaHolder manaHolder);
    
    @Override
    public boolean activate(ManaHolder manaHolder)
    {
        if(canActivate(manaHolder))
        {
            perform(manaHolder);
            manaHolder.burn(manaCost);
            return true;
        }
        
        return false;
    }
    
    public boolean canActivate(ManaHolder manaHolder)
    {
        return manaHolder.getMana() + manaHolder.getExtraMana() >= manaCost || (manaHolder.getPlayer() instanceof Player player && player.isCreative());
    }
    
    public void burnMana(ManaHolder manaHolder)
    {
        if(!manaHolder.getPlayer().level.isClientSide)
        {
            manaHolder.burn(this.manaCost);
        }
    }
    
    @Override
    public Optional<TooltipComponent> getTooltipComponent()
    {
        return tooltipComponent;
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        JsonObject json = super.makeDefaultConfig();
        json.addProperty("manaCost", this.defaultManaCost);
        return json;
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        super.readFromConfig(json);
        this.manaCost = SpellsFileUtil.jsonFloat(json, "manaCost");
        
        if(manaCost > 0)
        {
            this.tooltipComponent = Optional.of(new ManaTooltipComponent(manaCost));
        }
    }
    
    @Override
    public void applyDefaultConfig()
    {
        super.applyDefaultConfig();
        manaCost = defaultManaCost;
        
        if(manaCost > 0)
        {
            this.tooltipComponent = Optional.of(new ManaTooltipComponent(manaCost));
        }
    }
}
