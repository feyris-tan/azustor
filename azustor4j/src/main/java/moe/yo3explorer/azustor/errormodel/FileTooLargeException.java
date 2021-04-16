package moe.yo3explorer.azustor.errormodel;

/**
 * This exception gets thrown when you try to store an object, but it's too large to fit into a single volume file.
 */
public class FileTooLargeException extends AzustorException {
    /**
     * Generates this exception.
     */
    public FileTooLargeException()
    {
        super("The supplied file is too large!");
    }
}
