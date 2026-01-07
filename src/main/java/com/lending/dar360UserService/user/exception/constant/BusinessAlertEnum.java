/**
 * Aladdin Online Lending Application
 */
package com.lending.dar360UserService.user.exception.constant;

import com.lending.dar360UserService.user.exception.dto.AlertCode;
import com.lending.dar360UserService.user.exception.dto.IAlertCode;

import java.util.Optional;


/**
 * Description Common exception of Validation
 *
 * @author HieuNQ7
 * <p>
 * Nov 30, 2020
 */
public enum BusinessAlertEnum implements IAlertCode {
    ;

    private final AlertCode alertCode;

    BusinessAlertEnum(final String code, final String label) {
        this.alertCode = new AlertCode(code, label);
    }

    /**
     * get ErrorCode by label
     *
     * @param label
     * @return ErrorCode
     */
    public static Optional<BusinessAlertEnum> findByLabel(final String label) {
        for (final BusinessAlertEnum err : BusinessAlertEnum.values()) {
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
