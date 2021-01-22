package pauljabines.exam.isr.computers;

import lombok.Getter;

/**
 * @author Paul Benedict Jabines
 */
public enum Color {
    BLACK("black"),
    BLUE("blue"),
    SILVER("silver"),
    WHITE("white");

    @Getter
    private String name;

    Color(String name) {
        this.name = name;
    }

    public static Color fromName(String name) {
        for (Color color : Color.values()) {
            if (color.name.equalsIgnoreCase(name)) {
                return color;
            }
        }

        throw new IllegalArgumentException("Color Enum not found!");
    }
}
