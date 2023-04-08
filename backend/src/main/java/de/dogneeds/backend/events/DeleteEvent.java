package de.dogneeds.backend.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DeleteEvent extends ApplicationEvent {

    private final Object object; // will be user / product depending on what is being deleted

    public DeleteEvent(Object source, Object object) {
        super(source);
        this.object = object;
    }
}
