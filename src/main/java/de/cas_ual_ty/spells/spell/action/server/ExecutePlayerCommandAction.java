package de.cas_ual_ty.spells.spell.action.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class ExecutePlayerCommandAction extends AffectTypeAction<PlayerTarget>
{
    public static final int MAX_PERMISSION_LEVEL = 4;
    
    public static Codec<ExecutePlayerCommandAction> makeCodec(SpellActionType<ExecutePlayerCommandAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("command")).forGetter(ExecutePlayerCommandAction::getCommand),
                Codec.optionalField(ParamNames.paramIntImm("permission_level"), Codec.INT).xmap(o -> o.orElse(MAX_PERMISSION_LEVEL), Optional::ofNullable).forGetter(ExecutePlayerCommandAction::getPermissionLevel)
        ).apply(instance, (activation, multiTargets, command, permissionLevel) -> new ExecutePlayerCommandAction(type, activation, multiTargets, command, permissionLevel)));
    }
    
    public static ExecutePlayerCommandAction make(Object activation, Object multiTargets, DynamicCtxVar<String> command, int permissionLevel)
    {
        return new ExecutePlayerCommandAction(SpellActionTypes.EXECUTE_PLAYER_COMMAND.get(), activation.toString(), multiTargets.toString(), command, permissionLevel);
    }
    
    protected DynamicCtxVar<String> command;
    protected int permissionLevel;
    
    public ExecutePlayerCommandAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ExecutePlayerCommandAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<String> command, int permissionLevel)
    {
        super(type, activation, multiTargets);
        this.command = command;
        this.permissionLevel = permissionLevel;
    }
    
    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }
    
    public DynamicCtxVar<String> getCommand()
    {
        return command;
    }
    
    public int getPermissionLevel()
    {
        return permissionLevel;
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, PlayerTarget target)
    {
        if(target.getPlayer() instanceof ServerPlayer player && ctx.level instanceof ServerLevel level)
        {
            command.getValue(ctx).ifPresent(command ->
            {
                CommandSourceStack css = player.createCommandSourceStack().withPermission(permissionLevel);
                if(permissionLevel != -1)
                {
                    css = css.withPermission(permissionLevel);
                }
                level.getServer().getCommands().performCommand(css, command);
            });
        }
    }
}