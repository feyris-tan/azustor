package moe.yo3explorer.azustor.errormodel;

public class FileTooLargeException extends AzustorException {
    public FileTooLargeException()
    {
        super("The supplied file is too large!");
    }
}
