package moe.yo3explorer.azustor.errormodel;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ObjectDeletedException extends RuntimeException{
    public ObjectDeletedException(@NotNull UUID uuid) {
        super(String.format("This object was removed: %s",uuid.toString()));
    }
}
