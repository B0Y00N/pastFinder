package MobileAppDev.past_finder.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Reminder {

    @Id @GeneratedValue
    @Column(name="reminder_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name="member_id")
    private Member member;

    private String start_datetime;
    private String end_datetime;

    private String contents;
}
