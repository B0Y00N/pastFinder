package MobileAppDev.past_finder.Controller;

import MobileAppDev.past_finder.Domain.Reminder;
import MobileAppDev.past_finder.Service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/reminder")
public class ReminderController {

    @Autowired
    ReminderService reminderService;

    @PostMapping("/write/{id}")
    public void write(@PathVariable("id") Long id, @RequestBody Reminder reminder){
        reminderService.save(id, reminder);
    }

    @PostMapping("/delete/{reminder_id}")
    public void delete(@PathVariable("reminder_id") Long reminder_id){
        reminderService.delete(reminder_id);
    }

    @GetMapping("/get/{id}")
    @ResponseBody
    public List<Reminder> get(@PathVariable("id") Long id){
        return reminderService.get(id);
    }
}
