package de.flamesmp.command;

import de.flamesmp.FlameRTP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RTPReloadCommand implements CommandExecutor {

    public RTPReloadCommand() {
        Objects.requireNonNull(FlameRTP.getInstance().getCommand("rtpreload")).setExecutor(this);
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final @NotNull String[] args) {
        if (!sender.hasPermission("flamertp.admin")) {
            FlameRTP.getInstance().getMessageManager().send(sender, "no-permission");
            return true;
        }

        FlameRTP.getInstance().getRtpConfig().load();
        FlameRTP.getInstance().getMessageManager().load();
        FlameRTP.getInstance().getMessageManager().send(sender, "reload-success");
        return true;
    }
}
