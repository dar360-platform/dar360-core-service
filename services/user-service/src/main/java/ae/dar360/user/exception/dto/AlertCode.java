/**
 * Aladdin Online Lending Application
 */
package ae.dar360.user.exception.dto;

/**
 * alert code
 *
 * @author HieuNQ7
 * <p>
 * Nov 30, 2020
 */
public class AlertCode {

    private String code;
    private String label;

    public AlertCode(final String code, final String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

}
