/**
 * Aladdin Online Lending Application
 */
package ae.dar360.user.exception.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;
import java.util.Objects;

/**
 * Alert message dto
 *
 * @author HieuNQ7
 * <p>
 * Nov 27, 2020
 */
public class AlertMessages {

    private final IAlertCode alertCode;

    private final Object[] args;

    private AlertMessages(IAlertCode alertCode) {
        this(alertCode, "");
    }

    private AlertMessages(IAlertCode alertCode, Object... args) {
        this.alertCode = alertCode;
        this.args = args.clone();
    }

    /**
     * Create new AlertMessages code with AlertCode
     *
     * @param alertCode The alert code
     * @return instance of AlertMessages
     * @throws IllegalArgumentException if errorCode is <code>null</code>
     */
    public static AlertMessages alert(IAlertCode alertCode) {
        if (Objects.isNull(alertCode)) {
            throw new IllegalArgumentException("The AlertCode must not be null.");
        }

        return new AlertMessages(alertCode);
    }

    /**
     * Create new AlertMessages with AlertCode and Args
     *
     * @param alertCode The alert code
     * @param args
     * @return instance of Error
     * @throws IllegalArgumentException if alertCode is <code>null</code>
     */
    public static AlertMessages alert(IAlertCode alertCode, Object... args) {
        if (Objects.isNull(alertCode)) {
            throw new IllegalArgumentException("The ErrorCode must not be null.");
        }

        return new AlertMessages(alertCode, args);
    }

    public IAlertCode getAlertCode() {
        return alertCode;
    }

    public Object[] getArgs() {
        return args.clone();
    }

    @Override
    public String toString() {
        return "AlertMessages [alertCode=" + alertCode + ", args=" + Arrays.toString(args) + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, true);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

}
