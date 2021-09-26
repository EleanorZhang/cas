package org.apereo.cas.config;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.dao.MongoDbYubiKeyAccountRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.net.ssl.SSLContext;

/**
 * This is {@link MongoDbYubiKeyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "mongoDbYubiKeyConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MongoDbYubiKeyConfiguration {

    @RefreshScope
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @RefreshScope
    @Bean
    @Autowired
    public MongoTemplate mongoYubiKeyTemplate(
        @Qualifier("sslContext")
        final SSLContext sslContext,
        final CasConfigurationProperties casProperties) {
        val mongo = casProperties.getAuthn().getMfa().getYubikey().getMongo();
        val factory = new MongoDbConnectionFactory(sslContext);
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @RefreshScope
    @Bean
    @Autowired
    public YubiKeyAccountRegistry yubiKeyAccountRegistry(
        @Qualifier("yubiKeyAccountValidator")
        final YubiKeyAccountValidator yubiKeyAccountValidator,
        @Qualifier("yubikeyAccountCipherExecutor")
        final CipherExecutor yubikeyAccountCipherExecutor,
        @Qualifier("mongoYubiKeyTemplate")
        final MongoTemplate mongoYubiKeyTemplate,
        final CasConfigurationProperties casProperties) {
        val yubi = casProperties.getAuthn().getMfa().getYubikey();
        val registry = new MongoDbYubiKeyAccountRegistry(yubiKeyAccountValidator,
            mongoYubiKeyTemplate,
            yubi.getMongo().getCollection());
        registry.setCipherExecutor(yubikeyAccountCipherExecutor);
        return registry;
    }
}