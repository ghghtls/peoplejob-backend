package com.people.job.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${email.from:noreply@peoplejob.com}")
    private String fromEmail;

    /**
     * 회원가입 인증 메일 발송
     */
    @Async
    public CompletableFuture<Boolean> sendVerificationEmail(String to, String username, String verificationCode) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("verificationCode", verificationCode);
            context.setVariable("verificationLink",
                    "http://localhost:8080/api/email/verify?code=" + verificationCode);

            String htmlContent = templateEngine.process("email/verification", context);

            sendHtmlEmail(to, "[피플잡] 회원가입 인증을 완료해주세요", htmlContent);

            log.info("회원가입 인증 메일 발송 성공: {}", to);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("회원가입 인증 메일 발송 실패: {}", to, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 비밀번호 찾기 메일 발송
     */
    @Async
    public CompletableFuture<Boolean> sendPasswordResetEmail(String to, String username, String resetToken) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("resetToken", resetToken);
            context.setVariable("resetLink",
                    "http://localhost:3000/reset-password?token=" + resetToken);

            String htmlContent = templateEngine.process("email/password-reset", context);

            sendHtmlEmail(to, "[피플잡] 비밀번호 재설정 안내", htmlContent);

            log.info("비밀번호 재설정 메일 발송 성공: {}", to);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("비밀번호 재설정 메일 발송 실패: {}", to, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * HTML 이메일 발송
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    /**
     * 단순 텍스트 이메일 발송
     */
    @Async
    public CompletableFuture<Boolean> sendSimpleEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false);

            mailSender.send(message);

            log.info("이메일 발송 성공: {} - {}", to, subject);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("이메일 발송 실패: {} - {}", to, subject, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 이메일 템플릿 렌더링 (공통 메서드)
     */
    public String renderTemplate(String templateName, Context context) {
        try {
            return templateEngine.process(templateName, context);
        } catch (Exception e) {
            log.error("템플릿 렌더링 실패: {}", templateName, e);
            return null;
        }
    }

    /**
     * 이메일 유효성 검사
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}