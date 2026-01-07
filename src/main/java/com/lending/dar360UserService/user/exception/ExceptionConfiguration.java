/**
 * Aladdin Online Lending Application
 */
package com.lending.dar360UserService.user.exception;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Configuration for exception handler
 *
 * @author HieuNQ7
 * <p>
 * Nov 27, 2020
 */
@Configuration
public class ExceptionConfiguration {

    private static final String MESSAGE_VALIDATION_PATH =
        "${validation.message.path:classpath:message/validationExceptionLabels}";

    private static final String MESSAGE_TECHNICAL_PATH = "${technical.message.path:classpath:message/technicalExceptionLabels}";

    private static final String MESSAGE_BUSINESS_PATH = "${business.message.path:classpath:message/businessExceptionLabels}";

    @Value(value = MESSAGE_VALIDATION_PATH)
    private String validationMessagePath;

    @Value(value = MESSAGE_TECHNICAL_PATH)
    private String technicalMessagePath;

    @Value(value = MESSAGE_BUSINESS_PATH)
    private String businessMessagePath;

    public String getValidationMessagePath() {
        return this.validationMessagePath;
    }

    public String getTechnicalMessagePath() {
        return this.technicalMessagePath;
    }

    public String getBusinessMessagePath() {
        return this.businessMessagePath;
    }

    @Bean(name = "validationMessages")
    public MessageSource validationMessages() {
        final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename(this.validationMessagePath);
        messageSource.setAlwaysUseMessageFormat(true);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean(name = "technicalMessages")
    public MessageSource technicalMessages() {
        final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename(this.technicalMessagePath);
        messageSource.setAlwaysUseMessageFormat(true);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean(name ="businessMessages")
    public MessageSource businessMessages() {
        final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename(this.businessMessagePath);
        messageSource.setAlwaysUseMessageFormat(true);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

}
