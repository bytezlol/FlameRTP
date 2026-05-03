package de.flamesmp.command;

import de.flamesmp.FlameRTP;
import de.flamesmp.api.RTPRequest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RTPCommand implements CommandExecutor, TabCompleter {

    public RTPCommand() {
        Objects.requireNonNull(FlameRTP.getInstance().getCommand("rtp")).setExecutor(this);
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final @NotNull String[] args) {
        if (!(sender instanceof final Player player)) return false;

        if (!player.hasPermission("flamertp.use")) {
            FlameRTP.getInstance().getMessageManager().send(player, "no-permission");
            return true;
        }

        final String worldName = args.length >= 1 ? args[0] : player.getWorld().getName();

        final RTPRequest request = RTPRequest.builder()
                .player(player)
                .worldName(worldName)
                .reason("command")
                .build();

        FlameRTP.getInstance().getApi().teleport(request);
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final @NotNull String[] args) {
        if (args.length != 1) return Collections.emptyList();

        final List<String> completions = new ArrayList<>();
        final String partial = args[0].toLowerCase();
        for (final String worldName : FlameRTP.getInstance().getRtpConfig().getProfiles().keySet()) {
            if (worldName.startsWith(partial)) completions.add(worldName);
        }
        return completions;
    }
}
