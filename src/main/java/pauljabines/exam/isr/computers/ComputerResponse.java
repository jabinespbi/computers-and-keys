package pauljabines.exam.isr.computers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Paul Benedict Jabines
 */
public class ComputerResponse {
    public ComputerResponseBody computer;

    public static ComputerResponse toComputerResponse(Computer computer) {
        ComputerResponse computerResponse = new ComputerResponse();
        computerResponse.computer = new ComputerResponseBody();
        computerResponse.computer.type = computer.getType();
        computerResponse.computer.maker = computer.getMaker();
        computerResponse.computer.model = computer.getModel();
        computerResponse.computer.language = computer.getLanguage();

        // TODO:
        //  Fix ArrayIndexOutOfBounds when using stream API
        //  Produces a java.lang.ArrayIndexOutOfBounds due to asm incompatibility to jdk version
        computerResponse.computer.colors = computer.getColors().stream().map(Color::getName).collect(Collectors.toList());

        return computerResponse;
    }

    public static class ComputerResponseBody {
        public String type;
        public String maker;
        public String model;
        public String language;
        public List<String> colors = new ArrayList<>();
    }
}