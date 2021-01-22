package pauljabines.exam.isr.computers;

import lombok.Getter;

/**
 * @author Paul Benedict Jabines
 */
public class ComputerRequest {

    public ComputerRequestBody computer;

    public Computer toComputer() {
        Computer computer = new Computer();
        computer.setType(this.computer.type);
        computer.setMaker(this.computer.maker);
        computer.setModel(this.computer.model);
        computer.setLanguage(this.computer.language);
        computer.addColor(Color.fromName(this.computer.color));

        return computer;
    }

    public Status validate() {
        if (computer == null) {
            return Status.NULL_VALUES_ENCOUNTERED;
        }

        if (computer.type == null ||
                computer.maker == null ||
                computer.model == null ||
                computer.language == null ||
                computer.color == null) {
            return Status.NULL_VALUES_ENCOUNTERED;
        }

        try {
            Color.fromName(computer.color);
        } catch (IllegalArgumentException e) {
            return Status.COLOR_NOT_SUPPORTED;
        }

        return Status.OK;
    }

    public static class ComputerRequestBody {
        public String type;

        public String maker;

        public String model;

        public String language;

        public String color;
    }

    public enum Status {
        COLOR_NOT_SUPPORTED("Color not supported!"),
        NULL_VALUES_ENCOUNTERED("Null values encountered"),
        OK("Ok");

        @Getter
        private String description;

        Status(String description) {
            this.description = description;
        }
    }
}
