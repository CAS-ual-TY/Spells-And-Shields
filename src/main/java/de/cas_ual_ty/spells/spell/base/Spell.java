package de.cas_ual_ty.spells.spell.base;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.SpellsFileUtil;
import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class Spell extends ForgeRegistryEntry<ISpell> implements ISpell, IConfigurableSpell
{
    public final float defaultManaCost;
    
    protected float manaCost;
    
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
    public JsonObject makeDefaultConfig()
    {
        JsonObject json = new JsonObject();
        json.addProperty("manaCost", this.defaultManaCost);
        return json;
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        this.manaCost = SpellsFileUtil.jsonFloat(json, "manaCost");
    }
    
    @Override
    public void applyDefaultConfig()
    {
        manaCost = defaultManaCost;
    }
    
    @Override
    public boolean equals(Object o)
    {
        return this == o;
    }
    
    @Override
    public int hashCode()
    {
        return this.getRegistryName().hashCode();
    }
}
