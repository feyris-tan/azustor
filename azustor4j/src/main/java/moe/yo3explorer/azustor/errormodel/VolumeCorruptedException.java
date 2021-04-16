package moe.yo3explorer.azustor.errormodel;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * This gets thrown if a volume file has been tampered with in any way.
 */
public class VolumeCorruptedException extends AzustorException {
    /**
     * Generates this exception.
     * @param candidateFile Points to the broken volume file.
     */
    public VolumeCorruptedException(@NotNull File candidateFile) {
        super(String.format("Volume file corrupted: %s",candidateFile.getName()));
    }
}
