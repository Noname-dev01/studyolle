package com.studyolle.studyolle.modules.event;

import com.studyolle.studyolle.infra.AbstractContainerBaseTest;
import com.studyolle.studyolle.infra.MockMvcTest;
import com.studyolle.studyolle.modules.account.*;
import com.studyolle.studyolle.modules.account.form.SignUpForm;
import com.studyolle.studyolle.modules.study.Study;
import com.studyolle.studyolle.modules.study.StudyFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@MockMvcTest
class EventControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired StudyFactory studyFactory;
    @Autowired AccountFactory accountFactory;
    @Autowired EventService eventService;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountService accountService;

    @BeforeEach
    void beforeEach() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("admin");
        signUpForm.setEmail("admin@email.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void newEnrollment_to_FCFS_event_accepted() throws Exception {
        Account test = accountFactory.createAccount("test");
        Study study = studyFactory.createStudy("test-study", test);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, test);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account account = accountRepository.findByNickname("admin");
        isAccepted(account, event);
    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 대기중 (이미 인원이 꽉차서)")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void newEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account test = accountFactory.createAccount("test");
        Study study = studyFactory.createStudy("test-study", test);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, test);

        Account may = accountFactory.createAccount("may");
        Account june = accountFactory.createAccount("june");
        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, june);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account account = accountRepository.findByNickname("admin");
        isNotAccepted(account, event);
    }

    @Test
    @DisplayName("참가신청 확정자가 선착순 모임에 참가 신청을 취소하는 경우, 바로 다음 대기자를 자동으로 신청 확인한다.")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void accepted_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account account = accountRepository.findByNickname("admin");
        Account test = accountFactory.createAccount("test");
        Account may = accountFactory.createAccount("may");
        Study study = studyFactory.createStudy("test-study", test);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, test);

        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, account);
        eventService.newEnrollment(event, test);

        isAccepted(may, event);
        isAccepted(account, event);
        isNotAccepted(test, event);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        isAccepted(may, event);
        isAccepted(test, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, account));
    }

    @Test
    @DisplayName("참가신청 비확정자가 선착순 모임에 참가 신청을 취소하는 경우, 기존 확정자를 그대로 유지하고 새로운 확정자는 없다.")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void not_accepterd_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account admin = accountRepository.findByNickname("admin");
        Account test = accountFactory.createAccount("test");
        Account may = accountFactory.createAccount("may");
        Study study = studyFactory.createStudy("test-study", test);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, test);

        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, test);
        eventService.newEnrollment(event, admin);

        isAccepted(may, event);
        isAccepted(test, event);
        isNotAccepted(admin, event);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        isAccepted(may, event);
        isAccepted(test, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, admin));
    }

    private void isNotAccepted(Account account, Event event) {
        assertFalse(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
    }

    private void isAccepted(Account account, Event event) {
        assertTrue(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
    }

    @Test
    @DisplayName("관리자 확인 모임에 참가 신청 - 대기중")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void newEnrollment_to_CONFIMATIVE_event_not_accepted() throws Exception {
        Account test = accountFactory.createAccount("test");
        Study study = studyFactory.createStudy("test-study", test);
        Event event = createEvent("test-event", EventType.CONFIRMATIVE, 2, study, test);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account admin = accountRepository.findByNickname("admin");
        isNotAccepted(admin, event);
    }

    private Event createEvent(String eventTitle, EventType eventType, int limit, Study study, Account account) {
        Event event = new Event();
        event.setEventType(eventType);
        event.setLimitOfEnrollments(limit);
        event.setTitle(eventTitle);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        return eventService.createEvent(event, study, account);
    }

}