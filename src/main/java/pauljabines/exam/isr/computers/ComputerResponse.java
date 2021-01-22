package pauljabines.exam.isr.computers;

/**
 * @author Paul Benedict Jabines
 */
public class ComputerResponse {
    public Computer computer;

    public static ComputerResponse toComputerResponse(Computer computer) {
        ComputerResponse computerResponse = new ComputerResponse();
        computerResponse.computer = computer;

        return computerResponse;
    }
}