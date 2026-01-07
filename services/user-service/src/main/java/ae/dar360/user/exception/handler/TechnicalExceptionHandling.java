/**
 * Aladdin Online Lending Application
 */
package ae.dar360.user.exception.handler;

import ae.dar360.user.exception.AbstractException;
import ae.dar360.user.exception.TechnicalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Locale;

/**
 * Handler business exception
 *
 * @author HaiTD7
 * <p>
 * Nov 20, 2020
 */
@ControllerAdvice
public class TechnicalExceptionHandling extends AbstractExceptionHandler {

    private final MessageSource technicalMessages;

    @Autowired
    public TechnicalExceptionHandling(MessageSource technicalMessages) {
        this.technicalMessages = technicalMessages;
    }

    @ExceptionHandler(TechnicalException.class)
    @ResponseBody
    public ResponseEntity<Object> handleValidationException(AbstractException ex, Locale locale) {
        return super.handle(ex, locale);
    }

    @Override
    protected MessageSource getMessageSource() {
        return this.technicalMessages;
    }
}
