package moe.yo3explorer.azustor.errormodel;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * This exception gets thrown if the master file does not exist when you try to open a bucket.
 */
public class MasterFileNotFoundException extends RuntimeException{
    public MasterFileNotFoundException(@NotNull File masterFile) {
        super(String.format("File not found: " + masterFile.getAbsolutePath()));
    }
}
