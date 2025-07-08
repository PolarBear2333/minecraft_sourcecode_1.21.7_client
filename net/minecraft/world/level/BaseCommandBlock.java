/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public abstract class BaseCommandBlock
implements CommandSource {
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final Component DEFAULT_NAME = Component.literal("@");
    private static final int NO_LAST_EXECUTION = -1;
    private long lastExecution = -1L;
    private boolean updateLastExecution = true;
    private int successCount;
    private boolean trackOutput = true;
    @Nullable
    private Component lastOutput;
    private String command = "";
    @Nullable
    private Component customName;

    public int getSuccessCount() {
        return this.successCount;
    }

    public void setSuccessCount(int n) {
        this.successCount = n;
    }

    public Component getLastOutput() {
        return this.lastOutput == null ? CommonComponents.EMPTY : this.lastOutput;
    }

    public void save(ValueOutput valueOutput) {
        valueOutput.putString("Command", this.command);
        valueOutput.putInt("SuccessCount", this.successCount);
        valueOutput.storeNullable("CustomName", ComponentSerialization.CODEC, this.customName);
        valueOutput.putBoolean("TrackOutput", this.trackOutput);
        if (this.trackOutput) {
            valueOutput.storeNullable("LastOutput", ComponentSerialization.CODEC, this.lastOutput);
        }
        valueOutput.putBoolean("UpdateLastExecution", this.updateLastExecution);
        if (this.updateLastExecution && this.lastExecution != -1L) {
            valueOutput.putLong("LastExecution", this.lastExecution);
        }
    }

    public void load(ValueInput valueInput) {
        this.command = valueInput.getStringOr("Command", "");
        this.successCount = valueInput.getIntOr("SuccessCount", 0);
        this.setCustomName(BlockEntity.parseCustomNameSafe(valueInput, "CustomName"));
        this.trackOutput = valueInput.getBooleanOr("TrackOutput", true);
        this.lastOutput = this.trackOutput ? BlockEntity.parseCustomNameSafe(valueInput, "LastOutput") : null;
        this.updateLastExecution = valueInput.getBooleanOr("UpdateLastExecution", true);
        this.lastExecution = this.updateLastExecution ? valueInput.getLongOr("LastExecution", -1L) : -1L;
    }

    public void setCommand(String string) {
        this.command = string;
        this.successCount = 0;
    }

    public String getCommand() {
        return this.command;
    }

    public boolean performCommand(Level level) {
        if (level.isClientSide || level.getGameTime() == this.lastExecution) {
            return false;
        }
        if ("Searge".equalsIgnoreCase(this.command)) {
            this.lastOutput = Component.literal("#itzlipofutzli");
            this.successCount = 1;
            return true;
        }
        this.successCount = 0;
        MinecraftServer minecraftServer = this.getLevel().getServer();
        if (minecraftServer.isCommandBlockEnabled() && !StringUtil.isNullOrEmpty(this.command)) {
            try {
                this.lastOutput = null;
                CommandSourceStack commandSourceStack = this.createCommandSourceStack().withCallback((bl, n) -> {
                    if (bl) {
                        ++this.successCount;
                    }
                });
                minecraftServer.getCommands().performPrefixedCommand(commandSourceStack, this.command);
            }
            catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Executing command block");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Command to be executed");
                crashReportCategory.setDetail("Command", this::getCommand);
                crashReportCategory.setDetail("Name", () -> this.getName().getString());
                throw new ReportedException(crashReport);
            }
        }
        this.lastExecution = this.updateLastExecution ? level.getGameTime() : -1L;
        return true;
    }

    public Component getName() {
        return this.customName != null ? this.customName : DEFAULT_NAME;
    }

    @Nullable
    public Component getCustomName() {
        return this.customName;
    }

    public void setCustomName(@Nullable Component component) {
        this.customName = component;
    }

    @Override
    public void sendSystemMessage(Component component) {
        if (this.trackOutput) {
            this.lastOutput = Component.literal("[" + TIME_FORMAT.format(new Date()) + "] ").append(component);
            this.onUpdated();
        }
    }

    public abstract ServerLevel getLevel();

    public abstract void onUpdated();

    public void setLastOutput(@Nullable Component component) {
        this.lastOutput = component;
    }

    public void setTrackOutput(boolean bl) {
        this.trackOutput = bl;
    }

    public boolean isTrackOutput() {
        return this.trackOutput;
    }

    public InteractionResult usedBy(Player player) {
        if (!player.canUseGameMasterBlocks()) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide) {
            player.openMinecartCommandBlock(this);
        }
        return InteractionResult.SUCCESS;
    }

    public abstract Vec3 getPosition();

    public abstract CommandSourceStack createCommandSourceStack();

    @Override
    public boolean acceptsSuccess() {
        return this.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK) && this.trackOutput;
    }

    @Override
    public boolean acceptsFailure() {
        return this.trackOutput;
    }

    @Override
    public boolean shouldInformAdmins() {
        return this.getLevel().getGameRules().getBoolean(GameRules.RULE_COMMANDBLOCKOUTPUT);
    }

    public abstract boolean isValid();
}

