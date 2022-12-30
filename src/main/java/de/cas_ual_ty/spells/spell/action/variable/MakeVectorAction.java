package de.cas_ual_ty.spells.spell.action.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import net.minecraft.world.phys.Vec3;

public class MakeVectorAction extends SpellAction
{
    public static Codec<MakeVectorAction> makeCodec(SpellActionType<MakeVectorAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf("x").forGetter(MakeVectorAction::getX),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf("y").forGetter(MakeVectorAction::getY),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf("z").forGetter(MakeVectorAction::getZ),
                Codec.STRING.fieldOf("result").forGetter(MakeVectorAction::getResult)
        ).apply(instance, (activation, x, y, z, result) -> new MakeVectorAction(type, activation, x, y, z, result)));
    }
    
    protected DynamicCtxVar<Double> x;
    protected DynamicCtxVar<Double> y;
    protected DynamicCtxVar<Double> z;
    protected String result;
    
    public MakeVectorAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public MakeVectorAction(SpellActionType<?> type, String activation, DynamicCtxVar<Double> x, DynamicCtxVar<Double> y, DynamicCtxVar<Double> z, String result)
    {
        super(type, activation);
        this.x = x;
        this.y = y;
        this.z = z;
        this.result = result;
    }
    
    public DynamicCtxVar<Double> getX()
    {
        return x;
    }
    
    public DynamicCtxVar<Double> getY()
    {
        return y;
    }
    
    public DynamicCtxVar<Double> getZ()
    {
        return z;
    }
    
    public String getResult()
    {
        return result;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        x.getValue(ctx).ifPresent(x ->
        {
            y.getValue(ctx).ifPresent(y ->
            {
                z.getValue(ctx).ifPresent(z ->
                {
                    ctx.setCtxVar(CtxVarTypes.VEC3.get(), result, new Vec3(x, y, z));
                });
            });
        });
    }
}
