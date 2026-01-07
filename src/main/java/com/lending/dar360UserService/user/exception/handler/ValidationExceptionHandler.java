/**
 * Aladdin Online Lending Application
 */
package com.lending.dar360UserService.user.exception.handler;

import com.lending.dar360UserService.user.exception.AbstractException;
import com.lending.dar360UserService.user.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Locale;

/**
 * Handle exception of validation
 */
@ControllerAdvice
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class ValidationExceptionHandler extends AbstractExceptionHandler {

    private final MessageSource validationMessages;

    @Autowired
    public ValidationExceptionHandler(MessageSource validationMessages) {
        this.validationMessages = validationMessages;
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseBody
    public ResponseEntity<Object> handleValidationException(AbstractException ex, Locale locale) {
        return super.handle(ex, locale);
    }

    @Override
    protected MessageSource getMessageSource() {
        return this.validationMessages;
    }

}
