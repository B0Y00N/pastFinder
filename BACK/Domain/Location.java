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

    @ElementCollection
    @CollectionTable(name = "images", joinColumns = {@JoinColumn(name = "diary",referencedColumnName = "article_id"),
            @JoinColumn(name="seq",referencedColumnName = "seq")})
    @Column(name = "image",columnDefinition = "LONGTEXT")
    @Lob
    private List<String> images;
    private String contents;
}
