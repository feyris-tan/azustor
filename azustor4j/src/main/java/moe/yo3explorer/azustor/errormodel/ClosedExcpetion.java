package moe.yo3explorer.azustor.errormodel;

public class ClosedExcpetion extends AzustorException {
    public ClosedExcpetion() {
        super("A bucket has been closed.");
    }
}
