/**
 * Aladdin Online Lending Application
 */
package ae.dar360.user.mapper;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.UUID;

/**
 * Data type mapper
 *
 * @author HaiTD7
 * <p>
 * Nov 20, 2020
 */
public interface DataTypeMapper {

    /**
     * Mapping string to uuid
     *
     * @param uuid The string uuid
     * @return uuid
     */
    default UUID mapToUUID(String uuid) {
        if (StringUtils.isEmpty(uuid)) {
            return null;
        }
        return UUID.fromString(uuid);
    }

    /**
     * Mapping uuid to string
     *
     * @param uuid The uuid
     * @return string uuid
     */
    default String mapToString(UUID uuid) {
        if (Objects.isNull(uuid)) {
            return StringUtils.EMPTY;
        }

        return uuid.toString();
    }
}
