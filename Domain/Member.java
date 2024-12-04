package MobileAppDev.past_finder.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="member_id")
    private Long id;

    private String name;

    private String password;

    @OneToMany(mappedBy = "member")
    private List<Contents> diary = new ArrayList<>();
    @OneToMany(mappedBy = "member")
    private List<Reminder> reminder = new ArrayList<>();
}
