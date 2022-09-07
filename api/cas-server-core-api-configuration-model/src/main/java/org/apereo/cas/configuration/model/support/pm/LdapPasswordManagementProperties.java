package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link LdapPasswordManagementProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-pm-ldap")
@Getter
@Setter
@JsonFilter("LdapPasswordManagementProperties")
@Accessors(chain = true)
public class LdapPasswordManagementProperties extends AbstractLdapSearchProperties {
    @Serial
    private static final long serialVersionUID = -2610186056194686825L;

    /**
     * Collection of attribute names that indicate security questions answers.
     * This is done via a key-value structure where the key is the attribute name
     * for the security question and the value is the attribute name for the answer linked to the question.
     */
    private Map<String, String> securityQuestionsAttributes = new LinkedHashMap<>(0);

    /**
     * Name of LDAP attribute that represents the account locked status.
     * The value of the attribute is set to {@code "true"} if the account is
     * ever updated to indicated a locked status.
     */
    @RequiredProperty
    private String accountLockedAttribute = "pwdLockout";

    /**
     * The specific variant of LDAP
     * based on which update operations will be constructed.
     * Accepted values are:
     * * <ul>
     * <li>{@code AD}</li>
     * <li>{@code GENERIC}</li>
     * <li>{@code FreeIPA}</li>
     * <li>{@code EDirectory}</li>
     * </ul>
     */
    @RequiredProperty
    private LdapType type = LdapType.AD;

    /**
     * Username attribute required by LDAP.
     */
    @RequiredProperty
    private String usernameAttribute = "uid";
}
