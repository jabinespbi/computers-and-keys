package pauljabines.exam.isr.computers;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Paul Benedict Jabines
 */
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"type", "maker", "model", "language"})
})
public class Computer implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Getter
    @Column(unique = true)
    private String uid = UUID.randomUUID().toString();

    @Getter
    @Setter
    private String type;

    @Getter
    @Setter
    private String maker;

    @Getter
    @Setter
    private String model;

    @Getter
    @Setter
    private String language;

    @Getter
    @ElementCollection(targetClass = Color.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "computer_colors")
    private final List<Color> colors = new ArrayList<>();

    @Getter
    private Timestamp createdTimestamp = Timestamp.from(Instant.now());

    @Getter
    private Timestamp updatedTimestamp = Timestamp.from(Instant.now());

    public void addColor(Color color) {
        this.colors.add(color);
    }

    public void updateTimestamp() {
        updatedTimestamp = Timestamp.from(Instant.now());
    }
}
