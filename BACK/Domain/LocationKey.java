package MobileAppDev.past_finder.Domain;


import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
public class LocationKey implements Serializable {

    @ManyToOne
    @JoinColumn(name = "article_id")
    private Contents diary;

    private Integer seq;

}
