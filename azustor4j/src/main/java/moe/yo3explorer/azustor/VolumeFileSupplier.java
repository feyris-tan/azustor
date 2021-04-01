package moe.yo3explorer.azustor;

import java.io.File;
import java.util.function.Supplier;

class VolumeFileSupplier implements Supplier<File> {
    private int interator = 0;
    private File directory;

    VolumeFileSupplier(File directory) {
        this.directory = directory;
    }

    @Override
    public File get() {
        String candidateFilename = String.format("volume%02d.vol",++interator);
        candidateFilename = directory.getAbsolutePath() + File.separator + candidateFilename;
        File candidateFile = new File(candidateFilename);
        return candidateFile;
    }
}
