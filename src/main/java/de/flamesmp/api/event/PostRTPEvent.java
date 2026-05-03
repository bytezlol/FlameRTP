package de.flamesmp.api.event;

import de.flamesmp.api.RTPRequest;
import de.flamesmp.api.RTPResult;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class PostRTPEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final @NotNull RTPRequest request;
    private final @NotNull RTPResult result;

    public PostRTPEvent(final @NotNull RTPRequest request, final @NotNull RTPResult result) {
        super(true);
        this.request = request;
        this.result = result;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}