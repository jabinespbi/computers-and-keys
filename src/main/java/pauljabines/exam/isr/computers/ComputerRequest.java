package pauljabines.exam.isr.computers;

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

    public static class ComputerRequestBody {
        public String type;

        public String maker;

        public String model;

        public String language;

        public String color;
    }
}
