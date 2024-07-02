package com.monthlyib.server.event;

import com.monthlyib.server.auth.service.VerifyNumService;
import com.monthlyib.server.domain.user.service.UserService;
import com.monthlyib.server.mail.service.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailSendException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.Random;

@EnableAsync
@Configuration
@Component
@Slf4j
@RequiredArgsConstructor
public class UserMailEventListener {

    @Value("${mail.subject.user.registration}")
    private String registrationSubject;

    @Value("${mail.subject.user.verification}")
    private String verificationSubject;


    @Value("${mail.template.name.user.join}")
    private String registrationTemplateName;

    private final EmailSender emailSender;

    private final VerifyNumService verifyNumService;

    @Async
    @EventListener
    public void verification(UserVerificationEvent event) throws Exception {
        try {
            String[] to = new String[]{event.getEmail()};
            String verificationNum = numberGen(6,2);
            String email = event.getEmail();
            String message =  email + "님, 인증번호는 "+verificationNum+" 입니다";
            verifyNumService.createNum(email, verificationNum);
            log.info(to[0]);
            log.info(message);
            // TODO: 템플릿 변수 할당 및 변경
            emailSender.sendEmail(to, verificationSubject, message, registrationTemplateName);
        } catch (MailSendException e) {
            e.printStackTrace();
            log.error("MailSendException: Rollback for User Verification:");
        }
    }

    @Async
    @EventListener
    public void registration(UserRegistrationEvent event) throws Exception {
        try {
            String[] to = new String[]{event.getEmail()};
            String message = event.getEmail() + "님, 회원 가입이 성공적으로 완료되었습니다.";
            log.info(to[0]);
            log.info(message);
            emailSender.sendEmail(to, registrationSubject, message, registrationTemplateName);
        } catch (MailSendException e) {
            e.printStackTrace();
            log.error("MailSendException: Rollback for User Registration:");
        }
    }

    @Async
    @EventListener
    public void tutoringConfirm(UserTutoringConfirmEvent event) throws Exception {
        try {
            String[] to = new String[]{event.getEmail()};
            String message = event.getEmail() + "님, 신청하신 튜텨링이 승인 되었습니다.";
            log.info(to[0]);
            log.info(message);
            emailSender.sendEmail(to, registrationSubject, message, registrationTemplateName);
        } catch (MailSendException e) {
            e.printStackTrace();
            log.error("MailSendException: Rollback for User tutoringConfirm:");
        }
    }

    @Async
    @EventListener
    public void tutoringConfirm(UserQuestionConfirmEvent event) throws Exception {
        try {
            String[] to = new String[]{event.getEmail()};
            String message = event.getEmail() + "님, 질문에 대한 답변이 작성되었습니다.";
            log.info(to[0]);
            log.info(message);
            emailSender.sendEmail(to, registrationSubject, message, registrationTemplateName);
        } catch (MailSendException e) {
            e.printStackTrace();
            log.error("MailSendException: Rollback for User tutoringConfirm:");
        }
    }

    private static String numberGen(int len, int dupCd ) {

        Random rand = new Random();
        String numStr = ""; //난수가 저장될 변수

        for(int i=0;i<len;i++) {

            String ran = Integer.toString(rand.nextInt(10));

            if(dupCd==1) {
                numStr += ran;
            }else if(dupCd==2) {
                if(!numStr.contains(ran)) {
                    numStr += ran;
                }else {
                    i-=1;
                }
            }
        }
        return numStr;
    }
}
