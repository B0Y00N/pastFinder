package MobileAppDev.past_finder.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Contents {

    @Id @GeneratedValue
    @Column(name = "article_id")
    private Long id; //일기 ID

    private String title; //제목

    private String review; //총평

    @ManyToOne
    @JoinColumn(name="member_id")
    private Member member; //작성 유저

    private String date; //일기 시간

    @OneToMany(mappedBy="contents")
    private List<Location> map = new ArrayList<>(); //장소 정보
}
