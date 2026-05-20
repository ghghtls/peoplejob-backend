package com.people.job.peoplejob_backend.email.service;

import com.people.job.email.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@peoplejob.com");
    }

    @Test
    void testIsValidEmail() {
        // Valid emails
        assertTrue(emailService.isValidEmail("test@example.com"));
        assertTrue(emailService.isValidEmail("user.name@domain.co.kr"));
        assertTrue(emailService.isValidEmail("test123@test-domain.com"));

        // Invalid emails
        assertFalse(emailService.isValidEmail(""));
        assertFalse(emailService.isValidEmail(null));
        assertFalse(emailService.isValidEmail("invalid-email"));
        assertFalse(emailService.isValidEmail("@domain.com"));
        assertFalse(emailService.isValidEmail("test@"));
        assertFalse(emailService.isValidEmail("test@.com"));
    }

    @Test
    void testSendVerificationEmail() throws Exception {
        // Given
        String to = "test@example.com";
        String username = "testuser";
        String verificationCode = "123456";
        String htmlContent = "<html><body>Test Email</body></html>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(htmlContent);

        // When
        CompletableFuture<Boolean> result = emailService.sendVerificationEmail(to, username, verificationCode);

        // Then
        assertNotNull(result);
        verify(templateEngine).process(eq("email/verification"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendPasswordResetEmail() throws Exception {
        // Given
        String to = "test@example.com";
        String username = "testuser";
        String resetToken = "reset-token-123";
        String htmlContent = "<html><body>Password Reset Email</body></html>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(htmlContent);

        // When
        CompletableFuture<Boolean> result = emailService.sendPasswordResetEmail(to, username, resetToken);

        // Then
        assertNotNull(result);
        verify(templateEngine).process(eq("email/password-reset"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendSimpleEmail() throws Exception {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String content = "Test Content";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        CompletableFuture<Boolean> result = emailService.sendSimpleEmail(to, subject, content);

        // Then
        assertNotNull(result);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testRenderTemplate() {
        // Given
        String templateName = "test-template";
        Context context = new Context();
        String expectedHtml = "<html><body>Rendered Template</body></html>";

        when(templateEngine.process(templateName, context)).thenReturn(expectedHtml);

        // When
        String result = emailService.renderTemplate(templateName, context);

        // Then
        assertEquals(expectedHtml, result);
        verify(templateEngine).process(templateName, context);
    }

    @Test
    void testRenderTemplateWithException() {
        // Given
        String templateName = "invalid-template";
        Context context = new Context();

        when(templateEngine.process(templateName, context))
                .thenThrow(new RuntimeException("Template not found"));

        // When
        String result = emailService.renderTemplate(templateName, context);

        // Then
        assertNull(result);
        verify(templateEngine).process(templateName, context);
    }
}