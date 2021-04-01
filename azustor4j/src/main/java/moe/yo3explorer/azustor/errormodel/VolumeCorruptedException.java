package moe.yo3explorer.azustor.errormodel;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class VolumeCorruptedException extends AzustorException {
    public VolumeCorruptedException(@NotNull File candidateFile) {
        super(String.format("Volume file corrupted: %s",candidateFile.getName()));
    }
}
