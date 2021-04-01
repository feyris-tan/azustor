package moe.yo3explorer.azustor.errormodel;

public class DirectoryDoesNotExistExcepetion extends AzustorException {
    public DirectoryDoesNotExistExcepetion(String name) {
        super(String.format("Directory not found: " + name));
    }
}
