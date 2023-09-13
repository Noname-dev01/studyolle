package com.studyolle.studyolle.modules.main;

import com.studyolle.studyolle.modules.account.CurrentUser;
import com.studyolle.studyolle.modules.account.Account;
import com.studyolle.studyolle.modules.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model){
        if (account != null){
            model.addAttribute(account);
        }

        return "index";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

}
