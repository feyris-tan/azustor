package moe.yo3explorer.azustor;

import moe.yo3explorer.azustor.errormodel.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.UUID;

import static java.nio.file.StandardOpenOption.*;

public class AzustorBucket implements Closeable, AutoCloseable{
    private AzustorBucket() {}

    private static final long MASTER_MAGIC = 5571901898163051073L;
    static final long VOLUME_MAGIC = 5283671522011339329L;
    static final int RUNNING_VERSION = 1;

    private int creatorVersion;
    private long volumeTargetSize;
    private UUID serialNumber;
    private Date dateCreated;
    private File sourceDirectory;
    private boolean lowMemoryMode;
    private HighMemoryObject[][][][][][][][][][][][][][][][] highMemoryObjects;
    private boolean closed;

    @NotNull
    public static AzustorBucket createBucket(@NotNull File targetDirectory, long volumeTargetSize, boolean lowMemoryMode)
    {
        if (!targetDirectory.isDirectory())
            targetDirectory.mkdirs();

        ByteBuffer masterBuffer = ByteBuffer.allocate(44);
        masterBuffer.order(ByteOrder.LITTLE_ENDIAN);
        masterBuffer.putLong(MASTER_MAGIC);
        masterBuffer.putInt(RUNNING_VERSION);
        masterBuffer.putLong(volumeTargetSize);
        masterBuffer.put(UuidUtils.getBytesFromUUID(UUID.randomUUID()));
        masterBuffer.putLong(System.currentTimeMillis());
        byte[] masterFile = masterBuffer.array();

        String masterFilename = targetDirectory.getAbsolutePath() + File.separator + "master.cnf";
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(masterFilename);
            fos.write(masterFile);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            throw new FailedToCreateMasterFileException(e);
        }

        return loadBucket(targetDirectory,lowMemoryMode);
    }

    @NotNull
    public static AzustorBucket loadBucket(@NotNull File targetDirectory, boolean lowMemoryMode)
    {
        if (!targetDirectory.isDirectory())
            throw new DirectoryDoesNotExistExcepetion(targetDirectory.getName());

        String masterFilename = targetDirectory.getAbsolutePath() + File.separator + "master.cnf";
        File masterFile = new File(masterFilename);
        if (!masterFile.isFile())
            throw new MasterFileNotFoundException(masterFile);

        ByteBuffer masterBuffer;
        try {
            FileChannel masterChannel = FileChannel.open(masterFile.toPath(),READ);
            masterBuffer = ByteBuffer.allocate((int)masterChannel.size());
            masterChannel.read(masterBuffer);
            masterChannel.close();
        } catch (IOException e) {
            throw new MasterFileUnreadableException(e);
        }

        masterBuffer.position(0);
        masterBuffer.order(ByteOrder.LITTLE_ENDIAN);
        if (masterBuffer.getLong() != MASTER_MAGIC)
            throw new MasterFileInvalidMagicException();

        AzustorBucket result = new AzustorBucket();
        result.creatorVersion = masterBuffer.getInt();
        if (result.creatorVersion > RUNNING_VERSION)
            throw new MasterFileVersionMismatchException(result.creatorVersion,RUNNING_VERSION);
        result.volumeTargetSize = masterBuffer.getLong();
        result.serialNumber = UuidUtils.getUUIDFromBuffer(masterBuffer);
        result.dateCreated = new Date(masterBuffer.getLong());
        result.lowMemoryMode = lowMemoryMode;
        result.sourceDirectory = targetDirectory;

        if (!lowMemoryMode)
            result.scanHighMemory();

        return result;
    }

    @NotNull
    private FileChannelWrapper findVolumeWithEnoughFreeSpace(long requiredSpace)
    {
        VolumeFileSupplier filenameSupplier = new VolumeFileSupplier(sourceDirectory);
        while(true)
        {
            File candidateFile = filenameSupplier.get();
            if (!candidateFile.isFile())
            {
                try {
                    return new FileChannelWrapper(FileChannel.open(candidateFile.toPath(),CREATE_NEW,WRITE),candidateFile);
                } catch (IOException e) {
                    throw new VolumeMountFailedException(e);
                }
            }

            long volumeLength = candidateFile.length();
            long volumeFree = volumeTargetSize - volumeLength;
            if (requiredSpace > volumeFree)
                continue;

            try {
                FileChannel open = FileChannel.open(candidateFile.toPath(), WRITE);
                open.position(open.size());
                FileChannelWrapper result = new FileChannelWrapper(open,candidateFile);
                return result;
            }
            catch (IOException e) {
                throw new VolumeMountFailedException(e);
            }
        }
    }

    public UUID storeFile(byte[] buffer)
    {
        if (closed)
            throw new ClosedExcpetion();

        if (lowMemoryMode)
            return storeFileLowMemory(buffer);
        else
            return storeFileHighMemory(buffer);
    }

    public byte[] retrieveFile(UUID uuid)
    {
        if (closed)
            throw new ClosedExcpetion();

        if (lowMemoryMode)
            return retrieveFileLowMemory(uuid);
        else
            return retrieveHighMemory(uuid);
    }

    ///<editor-fold>Low Memory
    @NotNull
    private UUID storeFileLowMemory(@NotNull byte[] buffer)
    {
        UUID generatedUuid = UUID.randomUUID();

        ByteBuffer headerBuffer = ByteBuffer.allocate(32);
        headerBuffer.order(ByteOrder.LITTLE_ENDIAN);
        headerBuffer.putLong(VOLUME_MAGIC);
        headerBuffer.put(UuidUtils.getBytesFromUUID(generatedUuid));
        headerBuffer.putInt(buffer.length);
        headerBuffer.putInt(0);

        long requiredLength = buffer.length;
        requiredLength += headerBuffer.limit();

        if (requiredLength > volumeTargetSize)
            throw new FileTooLargeException();

        FileChannel outputChannel = findVolumeWithEnoughFreeSpace(requiredLength);
        try {
            headerBuffer.position(0);
            outputChannel.write(headerBuffer);
            outputChannel.write(ByteBuffer.wrap(buffer));
            outputChannel.force(true);
            outputChannel.close();
        } catch (IOException e) {
            throw new VolumeWriteFailedException(e);
        }

        return generatedUuid;
    }

    @NotNull
    private byte[] retrieveFileLowMemory(UUID uuid)
    {
        VolumeFileSupplier filenameSupplier = new VolumeFileSupplier(sourceDirectory);
        while(true) {
            File candidateFile = filenameSupplier.get();
            if (!candidateFile.isFile())
                throw new ObjectNotFoundException(uuid);

            try {
                FileChannel inputChannel = FileChannel.open(candidateFile.toPath(), READ);
                for (inputChannel.position(0); inputChannel.position() < inputChannel.size(); ) {
                    ByteBuffer headerBuffer = ByteBuffer.allocate(32);
                    inputChannel.read(headerBuffer);
                    headerBuffer.position(0);
                    headerBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    if (headerBuffer.getLong() != VOLUME_MAGIC)
                        throw new VolumeCorruptedException(candidateFile);
                    UUID possibleEntry = UuidUtils.getUUIDFromBuffer(headerBuffer);
                    int length = headerBuffer.getInt();
                    int flags = headerBuffer.getInt();
                    if (possibleEntry.equals(uuid)) {
                        if ((flags & 0x01) != 0) {
                            inputChannel.close();
                            throw new ObjectDeletedException(uuid);
                        }
                        ByteBuffer objectBuffer = ByteBuffer.allocate(length);
                        inputChannel.read(objectBuffer);
                        inputChannel.close();
                        return objectBuffer.array();
                    }
                    else
                    {
                        inputChannel.position(inputChannel.position() + length);
                    }
                }
                inputChannel.close();
            }
            catch (IOException ioe)
            {
                throw new VolumeReadException(ioe);
            }
        }
    }
    ///</editor-fold>

    ///<editor-fold>High Memory
    private void scanHighMemory()
    {
        VolumeFileSupplier filenameSupplier = new VolumeFileSupplier(sourceDirectory);
        File candidateFile = filenameSupplier.get();
        while (candidateFile.isFile())
        {
            try {
                FileChannel inputChannel = FileChannel.open(candidateFile.toPath(), READ);
                for (inputChannel.position(0); inputChannel.position() < inputChannel.size(); ) {
                    ByteBuffer headerBuffer = ByteBuffer.allocate(32);
                    inputChannel.read(headerBuffer);

                    headerBuffer.position(0);
                    headerBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    if (headerBuffer.getLong() != VOLUME_MAGIC)
                        throw new VolumeCorruptedException(candidateFile);
                    headerBuffer.position(headerBuffer.position() + 16);
                    int length = headerBuffer.getInt();
                    int flags = headerBuffer.getInt();
                    if ((flags & 0x01) != 0) {
                        inputChannel.position(inputChannel.position() + length);
                        continue;
                    }

                    headerBuffer.position(headerBuffer.position() - 24);
                    ByteBuffer uuidBuffer = ByteBuffer.allocate(32);
                    for (int i = 0; i < 16; i++)
                    {
                        uuidBuffer.put(headerBuffer.get());
                        uuidBuffer.put((byte)0);
                    }
                    uuidBuffer.position(0);
                    uuidBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    ShortBuffer shortBuffer = uuidBuffer.asShortBuffer();

                    if (highMemoryObjects == null)
                        highMemoryObjects = new HighMemoryObject[256][][][][][][][][][][][][][][][];

                    short sa = shortBuffer.get();
                    if (highMemoryObjects[sa] == null)
                        highMemoryObjects[sa] = new HighMemoryObject[256][][][][][][][][][][][][][][];

                    short sb = shortBuffer.get();
                    if (highMemoryObjects[sa][sb] == null)
                        highMemoryObjects[sa][sb] = new HighMemoryObject[256][][][][][][][][][][][][][];

                    short sc = shortBuffer.get();
                    if (highMemoryObjects[sa][sb][sc] == null)
                        highMemoryObjects[sa][sb][sc] = new HighMemoryObject[256][][][][][][][][][][][][];

                    short sd = shortBuffer.get();
                    if (highMemoryObjects[sa][sb][sc][sd] == null)
                        highMemoryObjects[sa][sb][sc][sd] = new HighMemoryObject[256][][][][][][][][][][][];

                    short se = shortBuffer.get();
                    if (highMemoryObjects[sa][sb][sc][sd][se] == null)
                        highMemoryObjects[sa][sb][sc][sd][se] = new HighMemoryObject[256][][][][][][][][][][];

                    short sf = shortBuffer.get();
                    if (highMemoryObjects[sa][sb][sc][sd][se][sf] == null)
                        highMemoryObjects[sa][sb][sc][sd][se][sf] = new HighMemoryObject[256][][][][][][][][][];

                    short sg = shortBuffer.get();
                    if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg] == null)
                        highMemoryObjects[sa][sb][sc][sd][se][sf][sg] = new HighMemoryObject[256][][][][][][][][];

                    short sh = shortBuffer.get();
                    if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh] == null)
                        highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh] = new HighMemoryObject[256][][][][][][][];

                    short si = shortBuffer.get();
                    if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si] == null)
                        highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si] = new HighMemoryObject[256][][][][][][];

                    short sj = shortBuffer.get();
                    if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj] == null)
                        highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj] = new HighMemoryObject[256][][][][][];

                    short sk = shortBuffer.get();
                    if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk] == null)
                        highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk] = new HighMemoryObject[256][][][][];

                    short sl = shortBuffer.get();
                    if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl] == null)
                        highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl] = new HighMemoryObject[256][][][];

                    short sm = shortBuffer.get();
                    if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl][sm] == null)
                        highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl][sm] = new HighMemoryObject[256][][];

                    short sn = shortBuffer.get();
                    if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl][sm][sn] == null)
                        highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl][sm][sn] = new HighMemoryObject[256][];

                    short so = shortBuffer.get();
                    if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl][sm][sn][so] == null)
                        highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl][sm][sn][so] = new HighMemoryObject[256];

                    HighMemoryObject hmo = new HighMemoryObject();
                    hmo.volume = candidateFile;
                    hmo.offset = inputChannel.position();
                    hmo.size = length;
                    short sp = shortBuffer.get();
                    highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl][sm][sn][so][sp] = hmo;
                    inputChannel.position(inputChannel.position() + length);
                }
            }
            catch (IOException ioe)
            {
                throw new VolumeReadException(ioe);
            }
            candidateFile = filenameSupplier.get();
        }
    }

    @NotNull
    private UUID storeFileHighMemory(@NotNull byte[] buffer)
    {
        UUID generatedUuid = UUID.randomUUID();
        HighMemoryObject hmo = new HighMemoryObject();

        ByteBuffer headerBuffer = ByteBuffer.allocate(32);
        headerBuffer.order(ByteOrder.LITTLE_ENDIAN);
        headerBuffer.putLong(VOLUME_MAGIC);
        headerBuffer.put(UuidUtils.getBytesFromUUID(generatedUuid));
        headerBuffer.putInt(buffer.length);
        headerBuffer.putInt(0);
        headerBuffer.position(0);

        long requiredLength = buffer.length;
        requiredLength += headerBuffer.limit();

        if (requiredLength > volumeTargetSize)
            throw new FileTooLargeException();

        FileChannelWrapper outputChannel = findVolumeWithEnoughFreeSpace(requiredLength);
        try {
            outputChannel.write(headerBuffer);
            hmo.offset = outputChannel.position();
            hmo.volume = outputChannel.getCandidateFile();
            hmo.size = buffer.length;

            outputChannel.write(ByteBuffer.wrap(buffer));
            outputChannel.force(true);
            outputChannel.close();
        } catch (IOException e) {
            throw new VolumeWriteFailedException(e);
        }

        headerBuffer.position(8);
        ByteBuffer uuidBuffer = ByteBuffer.allocate(32);
        for (int i = 0; i < 16; i++)
        {
            uuidBuffer.put(headerBuffer.get());
            uuidBuffer.put((byte)0);
        }
        uuidBuffer.position(0);
        uuidBuffer.order(ByteOrder.LITTLE_ENDIAN);
        ShortBuffer shortBuffer = uuidBuffer.asShortBuffer();

        if (highMemoryObjects == null)
            highMemoryObjects = new HighMemoryObject[256][][][][][][][][][][][][][][][];

        short sa = shortBuffer.get();
        if (highMemoryObjects[sa] == null)
            highMemoryObjects[sa] = new HighMemoryObject[256][][][][][][][][][][][][][][];

        short sb = shortBuffer.get();
        if (highMemoryObjects[sa][sb] == null)
            highMemoryObjects[sa][sb] = new HighMemoryObject[256][][][][][][][][][][][][][];

        short sc = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc] == null)
            highMemoryObjects[sa][sb][sc] = new HighMemoryObject[256][][][][][][][][][][][][];

        short sd = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd] == null)
            highMemoryObjects[sa][sb][sc][sd] = new HighMemoryObject[256][][][][][][][][][][][];

        short se = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se] == null)
            highMemoryObjects[sa][sb][sc][sd][se] = new HighMemoryObject[256][][][][][][][][][][];

        short sf = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf] == null)
            highMemoryObjects[sa][sb][sc][sd][se][sf] = new HighMemoryObject[256][][][][][][][][][];

        short sg = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg] == null)
            highMemoryObjects[sa][sb][sc][sd][se][sf][sg] = new HighMemoryObject[256][][][][][][][][];

        short sh = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh] == null)
            highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh] = new HighMemoryObject[256][][][][][][][];

        short si = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si] == null)
            highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si] = new HighMemoryObject[256][][][][][][];

        short sj = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj] == null)
            highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj] = new HighMemoryObject[256][][][][][];

        short sk = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk] == null)
            highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk] = new HighMemoryObject[256][][][][];

        short sl = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl] == null)
            highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl] = new HighMemoryObject[256][][][];

        short sm = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl][sm] == null)
            highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl][sm] = new HighMemoryObject[256][][];

        short sn = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl][sm][sn] == null)
            highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl][sm][sn] = new HighMemoryObject[256][];

        short so = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl][sm][sn][so] == null)
            highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl][sm][sn][so] = new HighMemoryObject[256];


        short sp = shortBuffer.get();
        highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl][sm][sn][so][sp] = hmo;

        return generatedUuid;
    }

    @NotNull
    private byte[] retrieveHighMemory(UUID uuid)
    {
        ByteBuffer headerBuffer = UuidUtils.getBufferFromUUID(uuid);
        ByteBuffer uuidBuffer = ByteBuffer.allocate(32);
        for (int i = 0; i < 16; i++)
        {
            uuidBuffer.put(headerBuffer.get());
            uuidBuffer.put((byte)0);
        }
        uuidBuffer.position(0);
        uuidBuffer.order(ByteOrder.LITTLE_ENDIAN);
        ShortBuffer shortBuffer = uuidBuffer.asShortBuffer();

        if (highMemoryObjects == null)
            throw new ObjectNotFoundException(uuid);

        short sa = shortBuffer.get();
        if (highMemoryObjects[sa] == null)
            throw new ObjectNotFoundException(uuid);

        short sb = shortBuffer.get();
        if (highMemoryObjects[sa][sb] == null)
            throw new ObjectNotFoundException(uuid);

        short sc = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc] == null)
            throw new ObjectNotFoundException(uuid);

        short sd = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd] == null)
            throw new ObjectNotFoundException(uuid);

        short se = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se] == null)
            throw new ObjectNotFoundException(uuid);

        short sf = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf] == null)
            throw new ObjectNotFoundException(uuid);

        short sg = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg] == null)
            throw new ObjectNotFoundException(uuid);

        short sh = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh] == null)
            throw new ObjectNotFoundException(uuid);

        short si = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si] == null)
            throw new ObjectNotFoundException(uuid);

        short sj = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj] == null)
            throw new ObjectNotFoundException(uuid);

        short sk = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk] == null)
            throw new ObjectNotFoundException(uuid);

        short sl = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl] == null)
            throw new ObjectNotFoundException(uuid);

        short sm = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl][sm] == null)
            throw new ObjectNotFoundException(uuid);

        short sn = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl][sm][sn] == null)
            throw new ObjectNotFoundException(uuid);

        short so = shortBuffer.get();
        if (highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl][sm][sn][so] == null)
            throw new ObjectNotFoundException(uuid);


        short sp = shortBuffer.get();
        HighMemoryObject hmo = highMemoryObjects[sa][sb][sc][sd][se][sf][sg][sh][si][sj][sk][sl][sm][sn][so][sp];

        try {
            FileChannel open = FileChannel.open(hmo.volume.toPath());
            open.position(hmo.offset);

            ByteBuffer resultBuffer = ByteBuffer.allocate(hmo.size);
            open.read(resultBuffer);
            open.close();

            byte[] result = resultBuffer.array();
            return result;
        } catch (IOException e) {
            throw new VolumeReadException(e);
        }
    }

    @Override
    public void close() throws IOException {
        closed = true;
        highMemoryObjects = null;
        lowMemoryMode = true;
        sourceDirectory = null;
        dateCreated = null;
        serialNumber = null;
    }

    ///</editor-fold>
}
