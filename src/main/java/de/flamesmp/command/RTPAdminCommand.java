package de.flamesmp.command;

import de.flamesmp.FlameRTP;
import de.flamesmp.api.FlameRTPAPI;
import de.flamesmp.api.RTPRequest;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RTPAdminCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUB_COMMANDS = Arrays.asList("clearcd", "clearcache", "force");

    public RTPAdminCommand() {
        Objects.requireNonNull(FlameRTP.getInstance().getCommand("rtpadmin")).setExecutor(this);
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final @NotNull String[] args) {
        if (!sender.hasPermission("flamertp.admin")) {
            FlameRTP.getInstance().getMessageManager().send(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sender.sendRichMessage("<gold>/rtpadmin clearcd <player> <world>");
            sender.sendRichMessage("<gold>/rtpadmin clearcache <world>");
            sender.sendRichMessage("<gold>/rtpadmin force <player> [world]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "clearcd" -> handleClearCooldown(sender, args);
            case "clearcache" -> handleClearCache(sender, args);
            case "force" -> handleForce(sender, args);
            default -> sender.sendRichMessage("<red>Unknown subcommand: " + args[0]);
        }

        return true;
    }

    private void handleClearCooldown(final @NotNull CommandSender sender, final @NotNull String[] args) {
        if (args.length < 3) {
            sender.sendRichMessage("<red>Usage: /rtpadmin clearcd <player> <world>");
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            FlameRTP.getInstance().getMessageManager().send(sender, "admin-player-not-found");
            return;
        }

        FlameRTP.getInstance().getCooldownManager().clear(target.getUniqueId(), args[2]);
        FlameRTP.getInstance().getMessageManager().send(sender, "admin-cooldown-cleared", "player", target.getName());
    }

    private void handleClearCache(final @NotNull CommandSender sender, final @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendRichMessage("<red>Usage: /rtpadmin clearcache <world>");
            return;
        }

        final String worldName = args[1].toLowerCase();
        FlameRTP.getInstance().getRtpManager().getCachedLocations().remove(worldName);
        FlameRTP.getInstance().getLocationCacheStorage().clearCachedLocations(worldName);

        FlameRTP.getInstance().getMessageManager().send(sender, "admin-cache-cleared", "world", worldName);
    }

    private void handleForce(final @NotNull CommandSender sender, final @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendRichMessage("<red>Usage: /rtpadmin force <player> [world]");
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            FlameRTP.getInstance().getMessageManager().send(sender, "admin-player-not-found");
            return;
        }

        final String worldName = args.length >= 3 ? args[2] : target.getWorld().getName();

        FlameRTP.getInstance().getApi().teleport(RTPRequest.builder()
                .player(target)
                .worldName(worldName)
                .bypassCooldown(true)
                .bypassCost(true)
                .bypassCountdown(true)
                .reason("admin-force")
                .build());
    }

    @Override
    public @NotNull List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final @NotNull String[] args) {
        if (!sender.hasPermission("flamertp.admin")) return Collections.emptyList();

        if (args.length == 1) {
            return filterStartingWith(SUB_COMMANDS, args[0]);
        }

        return switch (args[0].toLowerCase()) {
            case "clearcd", "force" -> {
                if (args.length == 2) yield filterStartingWith(onlinePlayerNames(), args[1]);
                if (args.length == 3) yield filterStartingWith(worldNames(), args[2]);
                yield Collections.emptyList();
            }
            case "clearcache" -> args.length == 2 ? filterStartingWith(worldNames(), args[1]) : Collections.emptyList();
            default -> Collections.emptyList();
        };
    }

    private @NotNull List<String> onlinePlayerNames() {
        final List<String> names = new ArrayList<>();
        for (final Player player : Bukkit.getOnlinePlayers()) names.add(player.getName());
        return names;
    }

    private @NotNull List<String> worldNames() {
        return new ArrayList<>(FlameRTP.getInstance().getRtpConfig().getProfiles().keySet());
    }

    private @NotNull List<String> filterStartingWith(final @NotNull List<String> source, final @NotNull String partial) {
        final String lower = partial.toLowerCase();
        final List<String> result = new ArrayList<>();
        for (final String s : source) if (s.toLowerCase().startsWith(lower)) result.add(s);
        return result;
    }
}
