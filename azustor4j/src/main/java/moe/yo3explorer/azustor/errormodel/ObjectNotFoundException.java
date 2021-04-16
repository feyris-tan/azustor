package moe.yo3explorer.azustor.errormodel;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This gets thrown when you try to retrieve an object which does not exist in the bucket.
 */
public class ObjectNotFoundException extends AzustorException {
    /**
     * Generates this exception.
     * @param uuid The UUID of the object you've been looking for.
     */
    public ObjectNotFoundException(@NotNull UUID uuid) {
        super(String.format("This entry was not found in any volume: %s",uuid.toString()));
    }
}
