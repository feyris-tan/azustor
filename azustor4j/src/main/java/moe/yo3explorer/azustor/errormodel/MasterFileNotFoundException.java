package moe.yo3explorer.azustor.errormodel;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class MasterFileNotFoundException extends RuntimeException{
    public MasterFileNotFoundException(@NotNull File masterFile) {
        super(String.format("File not found: " + masterFile.getAbsolutePath()));
    }
}
