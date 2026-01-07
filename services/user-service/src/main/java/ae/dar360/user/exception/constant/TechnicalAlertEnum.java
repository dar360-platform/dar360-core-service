/**
 * Aladdin Online Lending Application
 */
package ae.dar360.user.exception.constant;

import ae.dar360.user.exception.dto.AlertCode;
import ae.dar360.user.exception.dto.IAlertCode;

import java.util.Optional;

/**
 * Description Common exception of technical
 *
 * @author HieuNQ7
 * <p>
 * Nov 30, 2020
 */
public enum TechnicalAlertEnum implements IAlertCode {
    ;

    private final AlertCode alertCode;

    TechnicalAlertEnum(final String code, final String label) {
        this.alertCode = new AlertCode(code, label);
    }

    /**
     * get ErrorCode by label
     *
     * @param label
     * @return ErrorCode
     */
    public static Optional<TechnicalAlertEnum> findByLabel(String label) {
        for (TechnicalAlertEnum err : TechnicalAlertEnum.values()) {
            if (err.getLabel().equals(label)) {
                return Optional.of(err);
            }
        }
        return Optional.empty();
    }

    @Override
    public String getCode() {
        return this.alertCode.getCode();
    }

    @Override
    public String getLabel() {
        return this.alertCode.getLabel();
    }

}
