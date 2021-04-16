package moe.yo3explorer.azustor.errormodel;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteOrder;

/**
 * This gets thrown when an internal buffer has the wrong byte order.
 * If this gets thrown, it's a bug in Azustor and not your fault.
 */
public class UnexpectedByteOrderException extends AzustorException {
    /**
     * Generates this exception.
     * @param wanted The ByteOrder required in the internal buffer
     * @param got The actual ByteOrder in the internal buffer.
     */
    public UnexpectedByteOrderException(@NotNull ByteOrder wanted, @NotNull ByteOrder got) {
        super(String.format("UUID in unexpected byte order: Wanted %s, got %s",wanted.toString(),got.toString()));
    }
}
