package MobileAppDev.past_finder.Controller;

import MobileAppDev.past_finder.DTO.ListInfo;
import MobileAppDev.past_finder.Domain.Contents;
import MobileAppDev.past_finder.Service.ContentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/diary")
public class ContentsController {

    @Autowired
    ContentsService contentsService;

    @PostMapping("/write/{id}")
    public void write(@PathVariable("id") Long id, @RequestBody Contents contents){
       contentsService.write(id,contents);
    }

    @GetMapping("/read/{article_id}")
    @ResponseBody
    public Contents read(@PathVariable("article_id") Long id){
        return contentsService.get(id);
    }

    @GetMapping("/list/{id}")
    @ResponseBody
    public List<ListInfo> diarylist(@PathVariable("id") Long id){
        return contentsService.diarylist(id);
    }
}