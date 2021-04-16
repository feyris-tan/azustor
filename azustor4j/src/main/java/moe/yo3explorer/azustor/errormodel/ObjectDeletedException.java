package moe.yo3explorer.azustor.errormodel;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This exception gets thrown when you try to retrieve an object, but it's flagged as deleted.
 * This is unused as of now.
 */
public class ObjectDeletedException extends RuntimeException{
    public ObjectDeletedException(@NotNull UUID uuid) {
        super(String.format("This object was removed: %s",uuid.toString()));
    }
}
