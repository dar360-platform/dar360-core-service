/**
 * Aladdin Online Lending Application
 */
package ae.dar360.user.exception;

import ae.dar360.user.exception.dto.AlertMessages;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Exception of validation
 *
 * @author HieuNQ7
 * <p>
 * Nov 27, 2020
 */
public class ValidationException extends AbstractException {

    private static final long serialVersionUID = 6575296346073553904L;

    public ValidationException(final AlertMessages alert, final Throwable cause) {
        super(alert, cause);
    }

    public ValidationException(final AlertMessages alert) {
        super(alert);
    }

    public ValidationException(final List<AlertMessages> alerts, final Throwable cause) {
        super(alerts, cause);
    }

    public ValidationException(final List<AlertMessages> alerts) {
        super(alerts);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
