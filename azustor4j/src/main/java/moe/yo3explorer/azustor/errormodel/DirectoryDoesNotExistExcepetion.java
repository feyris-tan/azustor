package moe.yo3explorer.azustor.errormodel;

/**
 * This exception is thrown when you try to open a bucket, but the file system directory does not exist.
 */
public class DirectoryDoesNotExistExcepetion extends AzustorException {
    /**
     * Generate this exception.
     * @param name The path of the directory
     */
    public DirectoryDoesNotExistExcepetion(String name) {
        super(String.format("Directory not found: " + name));
    }
}
