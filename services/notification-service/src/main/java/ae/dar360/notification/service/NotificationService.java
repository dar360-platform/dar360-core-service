package ae.dar360.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void sendSms(String to, String message) {
        log.info("Sending SMS to {} with message: {}", to, message);
        // TODO: Implement Twilio integration
    }

    public void sendEmail(String to, String subject, String body) {
        log.info("Sending email to {} with subject: {}", to, subject);
        // TODO: Implement SendGrid integration
    }

    public void sendWhatsApp(String to, String message) {
        log.info("Sending WhatsApp message to {} with message: {}", to, message);
        // TODO: Implement WhatsApp Business API integration
    }
}
