package de.flamesmp.tasks;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import de.flamesmp.FlameRTP;
import de.flamesmp.manager.MessageManager;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class CountdownTask {

    private final @NotNull Player player;
    private final @NotNull Location startLocation;
    private final int totalSeconds;
    private final @NotNull Runnable onComplete;
    private final @NotNull Runnable onCancel;

    private final AtomicInteger remaining;
    private WrappedTask wrappedTask;
    @Getter
    private volatile boolean cancelled;

    public CountdownTask(final @NotNull Player player, final int totalSeconds, final @NotNull Runnable onComplete, final @NotNull Runnable onCancel) {
        this.player = player;
        this.startLocation = player.getLocation().clone();
        this.totalSeconds = totalSeconds;
        this.remaining = new AtomicInteger(totalSeconds);
        this.onComplete = onComplete;
        this.onCancel = onCancel;
    }

    public void start() {
        this.wrappedTask = FlameRTP.getInstance().getFoliaLib().getScheduler().runAtEntityTimer(player, this::tick, 1L, 20L);
    }

    private void tick() {
        if (cancelled || !player.isOnline()) {
            cleanupAndCancel();
            return;
        }

        final int left = remaining.getAndDecrement();
        if (left <= 0) {
            cleanup();
            onComplete.run();
            return;
        }

        final MessageManager messages = FlameRTP.getInstance().getMessageManager();
        messages.actionBar(player, "countdown", "time", left);
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    public void cancel() {
        if (cancelled) return;
        cancelled = true;
        cleanup();
        onCancel.run();
    }

    private void cleanupAndCancel() {
        cleanup();
        if (!cancelled) {
            cancelled = true;
            onCancel.run();
        }
    }

    private void cleanup() {
        if (wrappedTask != null) wrappedTask.cancel();
    }

    public @NotNull Location getStartLocation() {
        return startLocation;
    }
}