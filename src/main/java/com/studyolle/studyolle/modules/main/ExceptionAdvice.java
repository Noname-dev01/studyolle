package com.studyolle.studyolle.modules.main;

import com.studyolle.studyolle.modules.account.Account;
import com.studyolle.studyolle.modules.account.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler
    public String handleRuntimeException(@CurrentUser Account account, HttpServletRequest req, RuntimeException e){
        if (account != null){
            log.info("'{}' requested '{}'", account.getNickname(), req.getRequestURI());
        }else {
            log.info("requested '{}'", req.getRequestURI());
        }
        log.error("bad request", e);
        return "error";
    }
}
