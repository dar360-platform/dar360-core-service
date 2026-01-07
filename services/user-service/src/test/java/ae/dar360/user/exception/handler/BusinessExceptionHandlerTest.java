package ae.dar360.user.exception.handler;

import ae.dar360.user.exception.BusinessException;
import ae.dar360.user.exception.constant.ValidationAlertEnum;
import ae.dar360.user.exception.dto.AlertMessages;
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
import static org.mockito.Mockito.when;

class BusinessExceptionHandlerTest {

    private BusinessExceptionHandler handler;

    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new BusinessExceptionHandler(messageSource);
    }

    @Test
    void testHandleBusinessException() {
        AlertMessages alertMessages = AlertMessages.alert(ValidationAlertEnum.IEM001);
        BusinessException exception = new BusinessException(alertMessages);

        when(messageSource.getMessage(any(String.class), any(), any(Locale.class)))
                .thenReturn("Business error message");

        ResponseEntity<Object> response = handler.handleValidationException(exception, Locale.ENGLISH);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testGetMessageSource() {
        assertNotNull(handler.getMessageSource());
        assertEquals(messageSource, handler.getMessageSource());
    }
}
