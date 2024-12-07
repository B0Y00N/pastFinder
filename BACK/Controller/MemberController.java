package MobileAppDev.past_finder.Controller;

import MobileAppDev.past_finder.Domain.Member;
import MobileAppDev.past_finder.Service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/members")
public class MemberController {

    @Autowired
    MemberService memberService;

    @GetMapping("/login")
    @ResponseBody
    public long login(@RequestBody Member member){
        return memberService.login(member.getName(),member.getPassword());
    }

    @PostMapping("/register")
    @ResponseBody
    public long register(@RequestBody Member member){
       return memberService.register(member.getName(),member.getPassword());
    }
}
