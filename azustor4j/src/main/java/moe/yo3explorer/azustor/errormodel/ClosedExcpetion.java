package moe.yo3explorer.azustor.errormodel;

/**
 * This exception is thrown when you try to use an Azustor bucket which has been close()-d before.
 */
public class ClosedExcpetion extends AzustorException {
    /**
     * Generates this exception.
     */
    public ClosedExcpetion() {
        super("A bucket has been closed.");
    }
}
