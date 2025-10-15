package com.Flyway.server.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.mail.from}")
    private String fromEmail;
    
    @Value("${app.mail.invitation.subject}")
    private String invitationSubject;
    
    @Value("${app.mail.base-url}")
    private String baseUrl;
    
    /**
     * Sends an invitation email to a new user with their temporary password
     * 
     * @param toEmail The recipient's email address
     * @param firstName The recipient's first name
     * @param lastName The recipient's last name
     * @param organizationName The name of the organization they're being invited to
     * @param temporaryPassword The temporary password for first login
     * @param invitationToken The invitation token for tracking
     */
    public void sendInvitationEmail(
            String toEmail,
            String firstName,
            String lastName,
            String organizationName,
            String temporaryPassword,
            String invitationToken
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(invitationSubject);
            
            String htmlContent = buildInvitationEmailContent(
                    firstName,
                    lastName,
                    organizationName,
                    temporaryPassword,
                    invitationToken
            );
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            log.info("Invitation email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send invitation email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send invitation email", e);
        }
    }
    
    /**
     * Sends a resend invitation email to a user with their new temporary password
     * 
     * @param toEmail The recipient's email address
     * @param firstName The recipient's first name
     * @param lastName The recipient's last name
     * @param organizationName The name of the organization
     * @param temporaryPassword The new temporary password
     * @param invitationToken The new invitation token
     */
    public void sendResendInvitationEmail(
            String toEmail,
            String firstName,
            String lastName,
            String organizationName,
            String temporaryPassword,
            String invitationToken
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Invitation Resent: " + invitationSubject);
            
            String htmlContent = buildResendInvitationEmailContent(
                    firstName,
                    lastName,
                    organizationName,
                    temporaryPassword,
                    invitationToken
            );
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            log.info("Resend invitation email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send resend invitation email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send resend invitation email", e);
        }
    }
    
    private String buildInvitationEmailContent(
            String firstName,
            String lastName,
            String organizationName,
            String temporaryPassword,
            String invitationToken
    ) {
        String acceptUrl = baseUrl + "/invitations/accept?token=" + invitationToken;
        String rejectUrl = baseUrl + "/invitations/reject?token=" + invitationToken;
        
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background-color: #4CAF50;
                            color: white;
                            padding: 20px;
                            text-align: center;
                            border-radius: 5px 5px 0 0;
                        }
                        .content {
                            background-color: #f9f9f9;
                            padding: 30px;
                            border: 1px solid #ddd;
                        }
                        .credentials {
                            background-color: #fff;
                            border: 2px solid #4CAF50;
                            border-radius: 5px;
                            padding: 15px;
                            margin: 20px 0;
                        }
                        .credentials p {
                            margin: 10px 0;
                        }
                        .password {
                            font-family: monospace;
                            font-size: 18px;
                            font-weight: bold;
                            color: #4CAF50;
                            background-color: #f0f0f0;
                            padding: 10px;
                            border-radius: 3px;
                            display: inline-block;
                        }
                        .button {
                            display: inline-block;
                            padding: 12px 30px;
                            margin: 10px 5px;
                            text-decoration: none;
                            border-radius: 5px;
                            font-weight: bold;
                        }
                        .button-accept {
                            background-color: #4CAF50;
                            color: white;
                        }
                        .button-reject {
                            background-color: #f44336;
                            color: white;
                        }
                        .footer {
                            text-align: center;
                            padding: 20px;
                            color: #666;
                            font-size: 12px;
                        }
                        .warning {
                            background-color: #fff3cd;
                            border-left: 4px solid #ffc107;
                            padding: 15px;
                            margin: 20px 0;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>You've Been Invited!</h1>
                        </div>
                        <div class="content">
                            <h2>Hello %s %s,</h2>
                            <p>You've been invited to join <strong>%s</strong>.</p>
                            
                            <div class="credentials">
                                <h3>Your Login Credentials:</h3>
                                <p><strong>Email:</strong> Your email address</p>
                                <p><strong>Temporary Password:</strong></p>
                                <div class="password">%s</div>
                            </div>
                            
                            <div class="warning">
                                <strong>⚠️ Important:</strong> This is a temporary password. You will be required to change it after your first login.
                            </div>
                            
                            <p>Please click one of the buttons below to respond to this invitation:</p>
                            
                            <div style="text-align: center; margin: 30px 0;">
                                <a href="%s" class="button button-accept">Accept Invitation</a>
                                <a href="%s" class="button button-reject">Reject Invitation</a>
                            </div>
                            
                            <p><strong>Note:</strong> This invitation will expire in 7 days.</p>
                        </div>
                        <div class="footer">
                            <p>This is an automated email. Please do not reply to this message.</p>
                            <p>If you didn't expect this invitation, you can safely ignore this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(firstName, lastName, organizationName, temporaryPassword, acceptUrl, rejectUrl);
    }
    
    private String buildResendInvitationEmailContent(
            String firstName,
            String lastName,
            String organizationName,
            String temporaryPassword,
            String invitationToken
    ) {
        String acceptUrl = baseUrl + "/invitations/accept?token=" + invitationToken;
        String rejectUrl = baseUrl + "/invitations/reject?token=" + invitationToken;
        
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background-color: #2196F3;
                            color: white;
                            padding: 20px;
                            text-align: center;
                            border-radius: 5px 5px 0 0;
                        }
                        .content {
                            background-color: #f9f9f9;
                            padding: 30px;
                            border: 1px solid #ddd;
                        }
                        .credentials {
                            background-color: #fff;
                            border: 2px solid #2196F3;
                            border-radius: 5px;
                            padding: 15px;
                            margin: 20px 0;
                        }
                        .credentials p {
                            margin: 10px 0;
                        }
                        .password {
                            font-family: monospace;
                            font-size: 18px;
                            font-weight: bold;
                            color: #2196F3;
                            background-color: #f0f0f0;
                            padding: 10px;
                            border-radius: 3px;
                            display: inline-block;
                        }
                        .button {
                            display: inline-block;
                            padding: 12px 30px;
                            margin: 10px 5px;
                            text-decoration: none;
                            border-radius: 5px;
                            font-weight: bold;
                        }
                        .button-accept {
                            background-color: #4CAF50;
                            color: white;
                        }
                        .button-reject {
                            background-color: #f44336;
                            color: white;
                        }
                        .footer {
                            text-align: center;
                            padding: 20px;
                            color: #666;
                            font-size: 12px;
                        }
                        .warning {
                            background-color: #fff3cd;
                            border-left: 4px solid #ffc107;
                            padding: 15px;
                            margin: 20px 0;
                        }
                        .info {
                            background-color: #d1ecf1;
                            border-left: 4px solid #0c5460;
                            padding: 15px;
                            margin: 20px 0;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Invitation Resent</h1>
                        </div>
                        <div class="content">
                            <h2>Hello %s %s,</h2>
                            <p>Your invitation to join <strong>%s</strong> has been resent with new credentials.</p>
                            
                            <div class="info">
                                <strong>ℹ️ Note:</strong> Your previous temporary password and invitation link are no longer valid. Please use the new credentials below.
                            </div>
                            
                            <div class="credentials">
                                <h3>Your New Login Credentials:</h3>
                                <p><strong>Email:</strong> Your email address</p>
                                <p><strong>New Temporary Password:</strong></p>
                                <div class="password">%s</div>
                            </div>
                            
                            <div class="warning">
                                <strong>⚠️ Important:</strong> This is a temporary password. You will be required to change it after your first login.
                            </div>
                            
                            <p>Please click one of the buttons below to respond to this invitation:</p>
                            
                            <div style="text-align: center; margin: 30px 0;">
                                <a href="%s" class="button button-accept">Accept Invitation</a>
                                <a href="%s" class="button button-reject">Reject Invitation</a>
                            </div>
                            
                            <p><strong>Note:</strong> This invitation will expire in 7 days.</p>
                        </div>
                        <div class="footer">
                            <p>This is an automated email. Please do not reply to this message.</p>
                            <p>If you didn't expect this invitation, you can safely ignore this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(firstName, lastName, organizationName, temporaryPassword, acceptUrl, rejectUrl);
    }
}

