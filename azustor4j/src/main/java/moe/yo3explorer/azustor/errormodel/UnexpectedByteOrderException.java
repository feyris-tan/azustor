package moe.yo3explorer.azustor.errormodel;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteOrder;

public class UnexpectedByteOrderException extends AzustorException {
    public UnexpectedByteOrderException(@NotNull ByteOrder wanted, @NotNull ByteOrder got) {
        super(String.format("UUID in unexpected byte order: Wanted %s, gogt %s",wanted.toString(),got.toString()));
    }
}
