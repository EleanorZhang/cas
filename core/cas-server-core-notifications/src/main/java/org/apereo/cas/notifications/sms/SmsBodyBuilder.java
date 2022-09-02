package org.apereo.cas.notifications.sms;

import org.apereo.cas.configuration.model.support.sms.SmsProperties;

import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.util.ResourceUtils;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This is {@link SmsBodyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@SuperBuilder
public class SmsBodyBuilder implements Supplier<String> {
    @NonNull
    private final SmsProperties properties;

    @Builder.Default
    private final Map<String, Object> parameters = new LinkedHashMap<>();

    @Override
    public String get() {
        if (StringUtils.isBlank(properties.getText())) {
            LOGGER.warn("No SMS text is defined");
            return StringUtils.EMPTY;
        }
        try {
            val templateFile = ResourceUtils.getFile(properties.getText());
            val contents = FileUtils.readFileToString(templateFile, StandardCharsets.UTF_8);
            return formatSmsBody(contents);
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return formatSmsBody(properties.getText());
    }

    protected String formatSmsBody(final String contents) {
        val sub = new StringSubstitutor(this.parameters, "${", "}");
        return sub.replace(contents);
    }
}
