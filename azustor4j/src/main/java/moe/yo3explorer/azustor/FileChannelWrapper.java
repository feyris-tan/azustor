package moe.yo3explorer.azustor;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

class FileChannelWrapper extends FileChannel {
    private final FileChannel open;
    private final File candidateFile;

    public FileChannelWrapper(FileChannel open, File candidateFile) {
        this.open = open;
        this.candidateFile = candidateFile;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return open.read(dst);
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        return open.read(dsts, offset, length);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return open.write(src);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        return open.write(srcs, offset, length);
    }

    @Override
    public long position() throws IOException {
        return open.position();
    }

    @Override
    public FileChannel position(long newPosition) throws IOException {
        return open.position(newPosition);
    }

    @Override
    public long size() throws IOException {
        return open.size();
    }

    @Override
    public FileChannel truncate(long size) throws IOException {
        return open.truncate(size);
    }

    @Override
    public void force(boolean metaData) throws IOException {
        open.force(metaData);
    }

    @Override
    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        return open.transferTo(position,count,target);
    }

    @Override
    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        return open.transferFrom(src,position,count);
    }

    @Override
    public int read(ByteBuffer dst, long position) throws IOException {
        return open.read(dst,position);
    }

    @Override
    public int write(ByteBuffer src, long position) throws IOException {
        return open.write(src,position);
    }

    @Override
    public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
        return open.map(mode,position,size);
    }

    @Override
    public FileLock lock(long position, long size, boolean shared) throws IOException {
        return open.lock(position, size, shared);
    }

    @Override
    public FileLock tryLock(long position, long size, boolean shared) throws IOException {
        return open.tryLock(position, size, shared);
    }

    @Override
    protected void implCloseChannel() throws IOException {
        open.close();
    }

    public FileChannel getWrappedFileChannel() {
        return open;
    }

    public File getCandidateFile() {
        return candidateFile;
    }
}
