import moe.yo3explorer.azustor.AzustorBucket;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

public class SanityTests {
    @Test
    public void formatSanity()
    {
        String format;

        format = String.format("volume%02d.vol", 3);
        Assertions.assertEquals(format,"volume03.vol");
        format = String.format("volume%02d.vol", 42);
        Assertions.assertEquals(format,"volume42.vol");
        format = String.format("volume%02d.vol", 666);
        Assertions.assertEquals(format,"volume666.vol");
        format = String.format("volume%02d.vol", 9001);
        Assertions.assertEquals(format,"volume9001.vol");
    }

    private static final long cdrom = 680355840L;
    private static final int stressLevel = 100;
    private static final String inLowMem = "C:\\illusion\\AG3\\data\\cap\\keep";
    private static final String inHigMem = "E:\\KISS\\CM3D2\\ScreenShot\\Alpha";
    private static final String outDir = "test";
    private static final String outDi2 = "test2";

    @Test
    public void testAzustor() throws IOException {
        UuidCollection collection = new UuidCollection();
        File inDirLo = new File(inLowMem);
        File inDirHi = new File(inHigMem);
        File outDirs = new File(outDir);
        Assumptions.assumeTrue(inDirLo.isDirectory());
        Assumptions.assumeTrue(inDirHi.isDirectory());
        if (!outDirs.isDirectory())
            outDirs.mkdir();

        AzustorBucket bucket = AzustorBucket.createBucket(outDirs,cdrom,true);
        for (File file : inDirLo.listFiles()) {
            if (!file.isFile())
                continue;

            byte[] bytes = Files.readAllBytes(file.toPath());
            collection.inject(bucket.storeFile(bytes));
            if (collection.size() < stressLevel)
                bucket.retrieveFile(collection.getRandom());
        }
        bucket.close();

        bucket = AzustorBucket.loadBucket(outDirs,false);
        for (File file : inDirHi.listFiles()) {
            if (!file.isFile())
                continue;

            byte[] bytes = Files.readAllBytes(file.toPath());
            collection.inject(bucket.storeFile(bytes));
            if (collection.size() < stressLevel)
                bucket.retrieveFile(collection.getRandom());
        }
        bucket.close();

        bucket = AzustorBucket.loadBucket(outDirs,true);
        for (int i = 0 ; i < stressLevel; i++)
        {
            bucket.retrieveFile(collection.getRandom());
        }
        bucket.close();

        bucket = AzustorBucket.loadBucket(outDirs,false);
        for (int i = 0 ; i < stressLevel; i++)
        {
            bucket.retrieveFile(collection.getRandom());
        }
        bucket.close();

    }

    @Test
    public void testSalty() throws IOException {
        File outDirs2 = new File(outDi2);
        Random random = new Random();
        UuidCollection uuidCollection = new UuidCollection();
        stressIt(outDirs2, random, uuidCollection,true);
        stressIt(outDirs2, random, uuidCollection,false);
        stressIt(outDirs2, random, uuidCollection,true);
        stressIt(outDirs2, random, uuidCollection,false);
        stressIt(outDirs2, random, uuidCollection,true);
        stressIt(outDirs2, random, uuidCollection,false);
    }

    private void stressIt(@NotNull File outDirs2, Random random, UuidCollection uuidCollection, boolean lowMemory) throws IOException {
        AzustorBucket azustorBucket;
        if (outDirs2.isDirectory())
            azustorBucket = AzustorBucket.loadBucket(outDirs2, lowMemory);
        else
            azustorBucket = AzustorBucket.createBucket(outDirs2,cdrom,lowMemory);

        for (int i = 0; i < 1000; i++)
        {
            if (random.nextBoolean())
            {
                byte[] outBuffer = new byte[random.nextInt(Short.MAX_VALUE)];
                random.nextBytes(outBuffer);
                uuidCollection.inject(azustorBucket.storeFile(outBuffer));
            }
            else
            {
                if (uuidCollection.size() == 0)
                    continue;
                azustorBucket.retrieveFile(uuidCollection.getRandom());
            }
        }
        azustorBucket.close();
    }
}
