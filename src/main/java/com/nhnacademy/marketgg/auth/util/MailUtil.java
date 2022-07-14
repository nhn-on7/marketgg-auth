package com.nhnacademy.marketgg.auth.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Component
public class MailUtil {

    @Value("${mail.username}")
    private String fromEmail;

    @Value("${mail.password}")
    private String fromEmailpassword;

    private final Session session;

    public MailUtil() {

        Properties prop =  new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", 465);
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        prop.put("mail.smtp.starttls.enable", "true");

        this.session = Session.getDefaultInstance(prop, new Authenticator() {

            @Override
            public PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(fromEmail, fromEmailpassword);
            }

        });
    }

    //TODO : 이메일 발송 후 처리를 비동기로 처리해야함.
    public boolean sendCheckMail(String email) {

        try {

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("[Market GG] 인증코드 전송");

            message.setContent(
                    "<h1>[이메일 인증]</h1> <p>아래 버튼을 클릭하시면 이메일 인증이 완료됩니다.</p> " +
                            "<form action=\"http://localhost:7070/auth/use/email\" method=\"post\">\n" +
                            "    <button type=\"submit\">인증하기</button>\n" +
                            "</form>"
                    ,"text/html;charset=euc-kr"
            );

            Transport.send(message);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
