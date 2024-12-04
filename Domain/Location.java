package MobileAppDev.past_finder.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter @Setter
public class Location {

    @EmbeddedId
   private LocationKey locationKey;

    private String name;
    private Double x;
    private Double y;

    @Lob
    @Column(name="images",columnDefinition = "LONGTEXT")
    private String images;
    private String contents;
}
