package com.studyolle.studyolle.modules.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountFactory {

    @Autowired AccountRepository accountRepository;

    public Account createAccount(String nickname) {
        Account account = new Account();
        account.setNickname(nickname);
        account.setEmail(nickname + "@email.com");
        accountRepository.save(account);
        return account;
    }

}
