package com.studyolle.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

    private final AccountRepository accountRepository;

    //SignUpForm 클래스를 검사를 할거다
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(SignUpForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignUpForm signUpForm = (SignUpForm) errors;
        if (accountRepository.existsByEmail(signUpForm.getEmail())){
            errors.rejectValue("email","invalid.email", new Object[]{signUpForm.getEmail()},"이미 사용중인 이메일입니다.");
        }

        if (accountRepository.existsByNickname(signUpForm.getNickname())){
            errors.rejectValue("nickname","invalid.nickname", new Object[]{signUpForm.getNickname()},"이미 사용중인 닉네임입니다.");
        }
    }
}
