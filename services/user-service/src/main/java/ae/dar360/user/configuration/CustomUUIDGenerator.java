package ae.dar360.user.configuration;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.UUIDGenerator;

import java.io.Serializable;
import java.util.UUID;

public class CustomUUIDGenerator extends UUIDGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object)
            throws HibernateException {
        Serializable id = (Serializable) session.getEntityPersister(null, object).getClassMetadata()
                .getIdentifier(object, session);
        try {
            UUID.fromString(id + "");
            return id;
        } catch (IllegalArgumentException | NullPointerException exception) {
            return (Serializable) super.generate(session, object);
        }
    }
}
