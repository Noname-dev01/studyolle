package com.studyolle.studyolle.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.studyolle.account.AccountRepository;
import com.studyolle.studyolle.account.AccountService;
import com.studyolle.studyolle.account.SignUpForm;
import com.studyolle.studyolle.domain.Account;
import com.studyolle.studyolle.domain.Tag;
import com.studyolle.studyolle.settings.form.TagForm;
import com.studyolle.studyolle.tag.TagRepository;
import org.aspectj.lang.annotation.After;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {


    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    TagRepository tagRepository;

    @BeforeEach
    void beforEach(){
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
    @DisplayName("프로필 수정 폼")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateProfileForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @Test
    @DisplayName("프로필 수정 하기 - 입력값 정상")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateProfile() throws Exception {
        String bio = "짧은 소개를 수정하는 경우.";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                        .param("bio",bio)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname("admin");
        assertEquals(bio, account.getBio());
    }

    @Test
    @DisplayName("프로필 수정 하기 - 입력값 에러")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateProfile_error() throws Exception {
        String bio = "짧은 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 너무나도 길게 소개를 수정하는 경우. ";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                        .param("bio",bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account account = accountRepository.findByNickname("admin");
        assertNull(account.getBio());
    }

    @Test
    @DisplayName("패스워드 수정 폼")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updatePasswordForm() throws Exception {
        mockMvc.perform(get("/settings/password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @Test
    @DisplayName("패스워드 수정 하기 - 입력값 정상")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updatePassword() throws Exception {
        mockMvc.perform(post("/settings/password")
                        .param("newPassword","123456789")
                        .param("newPasswordConfirm","123456789")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/password"))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname("admin");
        assertTrue(passwordEncoder.matches("123456789",account.getPassword()));
    }

    @Test
    @DisplayName("패스워드 수정 하기 - 입력값 에러")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updatePassword_fail() throws Exception {
        mockMvc.perform(post("/settings/password")
                        .param("newPassword","123456789")
                        .param("newPasswordConfirm","111111111")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().hasErrors());

        Account account = accountRepository.findByNickname("admin");
        assertFalse(passwordEncoder.matches("123456789",account.getPassword()));
    }

    @Test
    @DisplayName("태그 수정 폼")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateTagsForm() throws Exception {
        mockMvc.perform(get("/settings/tags"))
                .andExpect(view().name("settings/tags"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @Test
    @DisplayName("계정에 태그 추가")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void addTag() throws Exception{
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/settings/tags/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        assertTrue(accountRepository.findByNickname("admin").getTags().contains(newTag));
    }

    @Test
    @DisplayName("계정에 태그 삭제")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void removeTag() throws Exception{
        Account account = accountRepository.findByNickname("admin");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(account,newTag);

        assertTrue(account.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/settings/tags/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(account.getTags().contains(newTag));
    }
}