package ae.dar360.notification.controller;

import ae.dar360.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/sms")
    public ResponseEntity<Void> sendSms(@RequestParam String to, @RequestParam String message) {
        notificationService.sendSms(to, message);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email")
    public ResponseEntity<Void> sendEmail(@RequestParam String to, @RequestParam String subject, @RequestParam String body) {
        notificationService.sendEmail(to, subject, body);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/whatsapp")
    public ResponseEntity<Void> sendWhatsApp(@RequestParam String to, @RequestParam String message) {
        notificationService.sendWhatsApp(to, message);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/otp")
    public ResponseEntity<Void> sendOtpSms(@RequestParam String to, @RequestParam String otpCode) {
        notificationService.sendSms(to, "Your OTP is: " + otpCode); // Placeholder
        return ResponseEntity.ok().build();
    }
}
