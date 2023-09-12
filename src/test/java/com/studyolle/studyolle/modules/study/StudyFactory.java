package com.studyolle.studyolle.modules.study;

import com.studyolle.studyolle.modules.account.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudyFactory {

    @Autowired StudyService studyService;
    @Autowired StudyRepository studyRepository;

    public Study createStudy(String path, Account manager) {
        Study study = new Study();
        study.setPath(path);
        studyService.createNewStudy(study, manager);
        return study;
    }

}
