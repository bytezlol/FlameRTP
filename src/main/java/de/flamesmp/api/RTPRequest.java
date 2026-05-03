package de.flamesmp.api;

import lombok.Builder;
import lombok.Getter;

import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
public class RTPRequest {

    private final @NotNull Player player;
    private final @NotNull String worldName;
    private final @Nullable String reason;

    private final boolean bypassCooldown;
    private final boolean bypassCost;
    private final boolean bypassCountdown;
}