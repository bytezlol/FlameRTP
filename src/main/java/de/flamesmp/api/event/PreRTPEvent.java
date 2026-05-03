package de.flamesmp.api.event;

import de.flamesmp.api.RTPRequest;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class PreRTPEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final @NotNull RTPRequest request;

    @Setter
    private boolean cancelled;

    public PreRTPEvent(final @NotNull RTPRequest request) {
        super(true); // async
        this.request = request;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}