/**
 * Aladdin Online Lending Application
 */
package com.lending.dar360UserService.user.exception;

import com.lending.dar360UserService.user.exception.dto.AlertMessages;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * The technical exception
 *
 * @author HaiTD7
 * <p>
 * Dec 18, 2020
 */
public class TechnicalException extends AbstractException {

    private static final long serialVersionUID = 2583670934544854739L;

    public TechnicalException(Throwable cause) {
        super(cause);
    }

    public TechnicalException(AlertMessages alert, Throwable cause) {
        super(alert, cause);
    }

    public TechnicalException(AlertMessages alert) {
        super(alert);
    }

    public TechnicalException(List<AlertMessages> alerts, Throwable cause) {
        super(alerts, cause);
    }

    public TechnicalException(List<AlertMessages> alerts) {
        super(alerts);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
