package de.flamesmp.listener;

import de.flamesmp.FlameRTP;
import de.flamesmp.tasks.CountdownTask;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerMoveListener implements Listener {

    public PlayerMoveListener() {
        FlameRTP.getInstance().getServer().getPluginManager().registerEvents(this, FlameRTP.getInstance());
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(final @NotNull PlayerMoveEvent event) {
        if (!FlameRTP.getInstance().getRtpConfig().isCancelOnMove()) return;

        final CountdownTask task = FlameRTP.getInstance().getRtpManager().getActiveCountdowns().get(event.getPlayer().getUniqueId());
        if (task == null) return;
        if (event.getPlayer().hasPermission("flamertp.bypass.move")) return;

        if (hasMoved(task.getStartLocation(), event.getTo())) {
            task.cancel();
        }
    }

    private boolean hasMoved(final @NotNull Location from, final @NotNull Location to) {
        return from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ();
    }
}
