package com.studyolle.studyolle.modules.study.event;

import com.studyolle.studyolle.modules.study.Study;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;

@Getter
@RequiredArgsConstructor
public class StudyCreatedEvent {

    private final Study study;

}
