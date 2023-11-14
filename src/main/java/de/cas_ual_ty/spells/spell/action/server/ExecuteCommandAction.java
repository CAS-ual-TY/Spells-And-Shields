package de.cas_ual_ty.spells.spell.action.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;

public class ExecuteCommandAction extends SpellAction
{
    public static Codec<ExecuteCommandAction> makeCodec(SpellActionType<ExecuteCommandAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("command")).forGetter(ExecuteCommandAction::getCommand)
        ).apply(instance, (activation, command) -> new ExecuteCommandAction(type, activation, command)));
    }
    
    public static ExecuteCommandAction make(Object activation, DynamicCtxVar<String> command)
    {
        return new ExecuteCommandAction(SpellActionTypes.EXECUTE_COMMAND.get(), activation.toString(), command);
    }
    
    protected DynamicCtxVar<String> command;
    
    public ExecuteCommandAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ExecuteCommandAction(SpellActionType<?> type, String activation, DynamicCtxVar<String> command)
    {
        super(type, activation);
        this.command = command;
    }
    
    public DynamicCtxVar<String> getCommand()
    {
        return command;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        if(ctx.level instanceof ServerLevel level)
        {
            command.getValue(ctx).ifPresent(command ->
            {
                CommandSourceStack css = level.getServer().createCommandSourceStack();
                level.getServer().getCommands().performPrefixedCommand(css, command);
            });
        }
    }
}
