package ae.dar360.user.configuration;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

public class UUIDType implements UserType<UUID>, EnhancedUserType<UUID> {

    @Override
    public int getSqlType() {
        return Types.VARCHAR;
    }

    @Override
    public Class<UUID> returnedClass() {
        return UUID.class;
    }

    @Override
    public boolean equals(UUID x, UUID y) {
        if (x == null && y == null) return true;
        if (x == null || y == null) return false;
        return x.equals(y);
    }

    @Override
    public int hashCode(UUID x) {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public UUID nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) 
            throws SQLException {
        String value = rs.getString(position);
        return value == null ? null : UUID.fromString(value);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, UUID value, int index, SharedSessionContractImplementor session) 
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            st.setString(index, value.toString());
        }
    }

    @Override
    public UUID deepCopy(UUID value) {
        return value; // UUID is immutable
    }

    @Override
    public boolean isMutable() {
        return false; // UUID is immutable
    }

    @Override
    public Serializable disassemble(UUID value) {
        return value;
    }

    @Override
    public UUID assemble(Serializable cached, Object owner) {
        return (UUID) cached;
    }

    @Override
    public UUID replace(UUID original, UUID target, Object owner) {
        return original;
    }

    @Override
    public String toSqlLiteral(UUID value) {
        return value == null ? "NULL" : "'" + value.toString() + "'";
    }

    @Override
    public String toString(UUID value) {
        return value == null ? null : value.toString();
    }

    @Override
    public UUID fromStringValue(CharSequence sequence) {
        return sequence == null ? null : UUID.fromString(sequence.toString());
    }
}