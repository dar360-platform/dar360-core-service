/**
 * Aladdin Online Lending Application
 */
package ae.dar360.user.exception;

import ae.dar360.user.exception.dto.AlertMessages;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * The technical exception
 *
 * @author HaiTD7
 * <p>
 * Dec 18, 2020
 */
public class BusinessException extends AbstractException {

    private static final long serialVersionUID = 2583670934544854739L;

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public BusinessException(AlertMessages alert, Throwable cause) {
        super(alert, cause);
    }

    public BusinessException(AlertMessages alert) {
        super(alert);
    }

    public BusinessException(List<AlertMessages> alerts, Throwable cause) {
        super(alerts, cause);
    }

    public BusinessException(List<AlertMessages> alerts) {
        super(alerts);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
