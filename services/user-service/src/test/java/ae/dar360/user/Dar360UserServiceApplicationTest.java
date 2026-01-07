package ae.dar360.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.TimeZone;

class Dar360UserServiceApplicationTest {

  @Test
  void init_setsDefaultTimezone() {
    TimeZone previous = TimeZone.getDefault();
    try {
      Dar360UserServiceApplication app = new Dar360UserServiceApplication();
      org.springframework.test.util.ReflectionTestUtils.setField(app, "timeZone", "UTC");

      app.init();

      Assertions.assertEquals("UTC", TimeZone.getDefault().getID());
    } finally {
      TimeZone.setDefault(previous);
    }
  }

  @Test
  void applySecretDecryptionOverrides_doesNothingWhenDisabled() {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("app-config.secret-encryption.encryption-key", "key");
    env.setProperty("app-config.secret-encryption.enabled", "false");

    Dar360UserServiceApplication.applySecretDecryptionOverrides(env);

    Assertions.assertNull(env.getPropertySources().get("customDataSourceProperties"));
  }

  @Test
  void applySecretDecryptionOverrides_doesNothingWhenEnabledButVariablesMissing() {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("app-config.secret-encryption.encryption-key", "key");
    env.setProperty("app-config.secret-encryption.enabled", "true");

    Dar360UserServiceApplication.applySecretDecryptionOverrides(env);

    Assertions.assertNull(env.getPropertySources().get("customDataSourceProperties"));
  }

  @Test
  void applySecretDecryptionOverrides_doesNothingWhenEnabledButKeyMissing() {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("app-config.secret-encryption.enabled", "true");
    env.setProperty("app-config.secret-encryption.variables", "db.password");

    Dar360UserServiceApplication.applySecretDecryptionOverrides(env);

    Assertions.assertNull(env.getPropertySources().get("customDataSourceProperties"));
  }

  @Test
  void applySecretDecryptionOverrides_decryptsConfiguredVariables() {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("app-config.secret-encryption.encryption-key", "key");
    env.setProperty("app-config.secret-encryption.enabled", "true");
    env.setProperty("app-config.secret-encryption.variables", "db.password,missing");
    env.setProperty("db.password", "ENCRYPTED");

    Dar360UserServiceApplication.applySecretDecryptionOverrides(
        env, (encryptedText, secretKey) -> "DECRYPTED");

    Object source = env.getPropertySources().get("customDataSourceProperties").getSource();
    @SuppressWarnings("unchecked")
    java.util.Map<String, Object> map = (java.util.Map<String, Object>) source;
    Assertions.assertEquals("DECRYPTED", map.get("db.password"));
    Assertions.assertFalse(map.containsKey("missing"));
  }

  @Test
  void applySecretDecryptionOverrides_swallowDecryptErrors() {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("app-config.secret-encryption.encryption-key", "key");
    env.setProperty("app-config.secret-encryption.enabled", "true");
    env.setProperty("app-config.secret-encryption.variables", "db.password");
    env.setProperty("db.password", "ENCRYPTED");

    Assertions.assertDoesNotThrow(
        () ->
            Dar360UserServiceApplication.applySecretDecryptionOverrides(
                env,
                (encryptedText, secretKey) -> {
                  throw new Dar360UserServiceApplication.SecretDecryptionException(
                      "boom", new RuntimeException("boom"));
                }));
  }
}
