import java.util.*;

public class UuidCollection
{
    public UuidCollection()
    {
        uuidList = new ArrayList<>();
        random = new Random();
    }

    private ArrayList<UUID> uuidList;
    private Random random;

    public void inject(UUID uuid)
    {
        uuidList.add(uuid);
    }

    public UUID getRandom()
    {
        int i = random.nextInt(uuidList.size());
        return uuidList.get(i);
    }

    public int size()
    {
        return uuidList.size();
    }
}
