/**
 * Aladdin Online Lending Application
 */
package ae.dar360.user.exception.handler;

import ae.dar360.user.exception.AbstractException;
import ae.dar360.user.exception.dto.AlertMessages;
import ae.dar360.user.swagger.generated.dto.Error;
import ae.dar360.user.swagger.generated.dto.ErrorDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract class for handling exception
 *
 * @author HieuNQ7
 * <p>
 * Nov 30, 2020
 */
public abstract class AbstractExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExceptionHandler.class);

    private static final String TECHNICAL = "TECHNICAL";

    protected static ErrorDetail generateUnhandledError(Exception ex) {
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setCode(TECHNICAL);
        errorDetail.setMessage(ex.getMessage());
        return errorDetail;
    }

    /**
     * Handle the Exception in RestController during implementation
     *
     * @param exception
     * @param locale
     * @return
     * @throws Throwable
     */
    public ResponseEntity<Object> handle(AbstractException exception, Locale locale) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info(exception.getMessage(), exception);
        }

        List<ErrorDetail> errorDetails = new ArrayList<>();
        List<AlertMessages> alertMessages = exception.getAlertCodes();
        if (CollectionUtils.isEmpty(alertMessages)) {
            errorDetails.add(generateUnhandledError(exception));
        } else {
            try {
                errorDetails.addAll(alertMessages.stream().map((AlertMessages ex) -> {
                    ErrorDetail alert = new ErrorDetail();
                    alert.setCode(ex.getAlertCode().getCode());
                    alert.setMessage(
                            this.getMessageSource().getMessage(
                                    ex.getAlertCode().getLabel(),
                                    ex.getArgs(),
                                    ex.getAlertCode().getLabel(),
                                    locale));
                    return alert;
                }).collect(Collectors.toList()));
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                errorDetails.add(generateUnhandledError(e));
            }

        }
        HttpStatus httpStatus = exception.getStatus();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        Set<ErrorDetail> setAlert = new HashSet<>();
        setAlert.addAll(errorDetails);
        Error error = new Error();
        error.setAlerts(errorDetails);
        return new ResponseEntity<>(error, headers, httpStatus);
    }

    /**
     * The {@link ResourceBundle} object to get the error message from error code that defined in
     * the message bundle files.
     *
     * @return
     */
    protected abstract MessageSource getMessageSource();

}
