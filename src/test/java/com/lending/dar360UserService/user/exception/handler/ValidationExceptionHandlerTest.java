package com.lending.dar360UserService.user.exception.handler;

import com.lending.dar360UserService.user.exception.ValidationException;
import com.lending.dar360UserService.user.exception.constant.ValidationAlertEnum;
import com.lending.dar360UserService.user.exception.dto.AlertMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ValidationExceptionHandlerTest {

    private ValidationExceptionHandler handler;

    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new ValidationExceptionHandler(messageSource);
    }

    @Test
    void testHandleValidationException() {
        AlertMessages alertMessages = AlertMessages.alert(ValidationAlertEnum.IEM001);
        ValidationException exception = new ValidationException(alertMessages);

        when(messageSource.getMessage(any(String.class), any(), any(Locale.class)))
                .thenReturn("Validation error message");

        ResponseEntity<Object> response = handler.handleValidationException(exception, Locale.ENGLISH);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testHandleValidationExceptionWithDifferentLocale() {
        AlertMessages alertMessages = AlertMessages.alert(ValidationAlertEnum.IEM001);
        ValidationException exception = new ValidationException(alertMessages);

        when(messageSource.getMessage(any(String.class), any(), eq(Locale.FRENCH)))
                .thenReturn("Message d'erreur de validation");

        ResponseEntity<Object> response = handler.handleValidationException(exception, Locale.FRENCH);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetMessageSource() {
        assertNotNull(handler.getMessageSource());
        assertEquals(messageSource, handler.getMessageSource());
    }
}
