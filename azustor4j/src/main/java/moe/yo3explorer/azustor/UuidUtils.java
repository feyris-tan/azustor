package moe.yo3explorer.azustor;

import moe.yo3explorer.azustor.errormodel.UnexpectedByteOrderException;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

class UuidUtils {
    @NotNull
    public static byte[] getBytesFromUUID(@NotNull UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return bb.array();
    }

    @NotNull
    public static ByteBuffer getBufferFromUUID(@NotNull UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        bb.position(0);
        return bb;
    }

    @NotNull
    public static UUID getUUIDFromBytes(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        Long high = bb.getLong();
        Long low = bb.getLong();

        return new UUID(high, low);
    }

    @NotNull
    public static UUID getUUIDFromBuffer(@NotNull ByteBuffer buffer)
    {
        if (buffer.order() != ByteOrder.LITTLE_ENDIAN)
            throw new UnexpectedByteOrderException(ByteOrder.LITTLE_ENDIAN,buffer.order());

        long high = buffer.getLong();
        long low = buffer.getLong();
        return new UUID(high,low);
    }
}
