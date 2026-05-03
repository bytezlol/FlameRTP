package de.flamesmp.api;

import de.flamesmp.enums.FailReason;
import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class RTPResult {

    private final boolean success;
    private final @Nullable Location location;
    private final @Nullable FailReason failReason;
    private final long durationMillis;

    private RTPResult(final boolean success, final @Nullable Location location, final @Nullable FailReason failReason, final long durationMillis) {
        this.success = success;
        this.location = location;
        this.failReason = failReason;
        this.durationMillis = durationMillis;
    }

    public static @NotNull RTPResult success(final @NotNull Location location, final long durationMillis) {
        return new RTPResult(true, location, null, durationMillis);
    }

    public static @NotNull RTPResult failure(final @NotNull FailReason reason, final long durationMillis) {
        return new RTPResult(false, null, reason, durationMillis);
    }
}