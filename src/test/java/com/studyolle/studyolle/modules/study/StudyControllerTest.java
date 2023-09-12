package com.studyolle.studyolle.modules.study;

import com.studyolle.studyolle.infra.AbstractContainerBaseTest;
import com.studyolle.studyolle.infra.MockMvcTest;
import com.studyolle.studyolle.modules.account.Account;
import com.studyolle.studyolle.modules.account.AccountFactory;
import com.studyolle.studyolle.modules.account.AccountRepository;
import com.studyolle.studyolle.modules.account.AccountService;
import com.studyolle.studyolle.modules.account.form.SignUpForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
public class StudyControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired StudyService studyService;
    @Autowired StudyRepository studyRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountFactory accountFactory;
    @Autowired StudyFactory studyFactory;
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
    @DisplayName("스터디 개설 폼 조회")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createStudyForm() throws Exception {
        mockMvc.perform(get("/new-study")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));
    }

    @Test
    @DisplayName("스터디 개설 - 완료")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createStudy_success() throws Exception {
        mockMvc.perform(post("/new-study")
                .param("path", "test-path")
                .param("title", "study title")
                .param("shortDescription", "short description of a study")
                .param("fullDescription", "full description of a study")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/test-path"));

        Study study = studyRepository.findByPath("test-path");
        assertNotNull(study);
        Account account = accountRepository.findByNickname("admin");
        assertTrue(study.getManagers().contains(account));
    }

    @Test
    @DisplayName("스터디 개설 - 실패")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createStudy_fail() throws Exception {
        mockMvc.perform(post("/new-study")
                .param("path", "wrong path")
                .param("title", "study title")
                .param("shortDescription", "short description of a study")
                .param("fullDescription", "full description of a study")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(model().attributeExists("account"));

        Study study = studyRepository.findByPath("test-path");
        assertNull(study);
    }

    @Test
    @DisplayName("스터디 조회")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void viewStudy() throws Exception {
        Study study = new Study();
        study.setPath("test-path");
        study.setTitle("test study");
        study.setShortDescription("short description");
        study.setFullDescription("<p>full description</p>");

        Account account = accountRepository.findByNickname("admin");
        studyService.createNewStudy(study, account);

        mockMvc.perform(get("/study/test-path"))
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @Test
    @DisplayName("스터디 가입")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void joinStudy() throws Exception {
        Account test = accountFactory.createAccount("test");
        Study study = studyFactory.createStudy("test-study", test);

        mockMvc.perform(get("/study/" + study.getPath() + "/join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

        Account account = accountRepository.findByNickname("admin");
        assertTrue(study.getMembers().contains(account));
    }

    @Test
    @DisplayName("스터디 탈퇴")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void leaveStudy() throws Exception {
        Account test = accountFactory.createAccount("test");
        Study study = studyFactory.createStudy("test-study", test);
        Account account = accountRepository.findByNickname("admin");
        studyService.addMember(study, account);

        mockMvc.perform(get("/study/" + study.getPath() + "/leave"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

        assertFalse(study.getMembers().contains(account));
    }





}