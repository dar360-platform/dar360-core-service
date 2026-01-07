package ae.dar360.user.configuration;

import ae.dar360.user.constant.CommonConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "dateTimeProvider")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement
@Profile({"!test"})
@Slf4j
public class JpaAuditConfig {

    @Component("auditorAware")
    @RequiredArgsConstructor
    class AuditorAwareImpl implements AuditorAware<String> {

        private final HttpServletRequest request;

        @Override
        public Optional<String> getCurrentAuditor() {
            try {
                if (request != null) {
                    String username = request.getHeader("x-username");
                    log.info("Header x-username: {}", username);
                    if (username != null && !username.isEmpty() && !CommonConstants.NOBODY.equalsIgnoreCase(username)) {
                        return Optional.of(username);
                    }
                } else {
                    log.warn("HttpServletRequest is null!");
                }
            } catch (Exception e) {
                log.error("Failed to get current auditor from request", e);
            }

            String fallbackUser = System.getProperty("user.name", CommonConstants.DEFAULT_USER);
            if (CommonConstants.NOBODY.equalsIgnoreCase(fallbackUser)) {
                fallbackUser = CommonConstants.DEFAULT_USER;
            }
            log.info("Fallback user: {}", fallbackUser);
            return Optional.ofNullable(fallbackUser);
        }
    }

    @Component("dateTimeProvider")
    class DefaultDateTimeProvider implements DateTimeProvider {

        @Override
        public Optional<TemporalAccessor> getNow() {
            return Optional.of(OffsetDateTime.now());
        }
    }
}
