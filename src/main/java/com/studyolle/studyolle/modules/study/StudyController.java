package com.studyolle.studyolle.modules.study;

import com.studyolle.studyolle.modules.account.CurrentUser;
import com.studyolle.studyolle.modules.account.Account;
import com.studyolle.studyolle.modules.study.form.StudyForm;
import com.studyolle.studyolle.modules.study.validator.StudyFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;
    private final ModelMapper modelMapper;
    private final StudyFormValidator studyFormValidator;
    private final StudyRepository studyRepository;

    @InitBinder("studyForm")
    public void studyFormInitBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(studyFormValidator);
    }

    @GetMapping("/new-study")
    public String newStudyForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new StudyForm());

        return "study/form";
    }

    @PostMapping("/new-study")
    public String newStudyForm(@CurrentUser Account account, @Valid StudyForm studyForm, Errors errors, Model model){
        if (errors.hasErrors()){
            model.addAttribute(account);
            return "study/form";
        }

        Study newStudy = studyService.createNewStudy(modelMapper.map(studyForm, Study.class),account);
        return "redirect:/study/"+ URLEncoder.encode(newStudy.getPath(), StandardCharsets.UTF_8);
    }

    @GetMapping("/study/{path}")
    public String viewStudy(@CurrentUser Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);

        return "study/view";
    }

    @GetMapping("/study/{path}/members")
    public String viewStudyMembers(@CurrentUser Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);

        return "study/members";
    }

    //join,leave는 원래 @PostMapping으로 해야됨 html form으로 감싸면 UI가 안맞아서 이렇게 해놓음
    @GetMapping("/study/{path}/join")
    public String joinStudy(@CurrentUser Account account, @PathVariable String path){
        Study study = studyRepository.findStudyWithMembersByPath(path);
        studyService.addMember(study, account);
        return "redirect:/study/" + study.getEncodedPath() + "/members";
    }

    @GetMapping("/study/{path}/leave")
    public String leaveStudy(@CurrentUser Account account, @PathVariable String path){
        Study study = studyRepository.findStudyWithMembersByPath(path);
        studyService.removeMember(study, account);
        return "redirect:/study/"+ study.getEncodedPath() + "/members";
    }

}
