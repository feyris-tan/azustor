package moe.yo3explorer.azustor.errormodel;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ObjectNotFoundException extends AzustorException {
    public ObjectNotFoundException(@NotNull UUID uuid) {
        super(String.format("This entry was not found in any volume: %s",uuid.toString()));
    }
}
