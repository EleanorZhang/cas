package org.apereo.cas.support.inwebo.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.inwebo.service.InweboService;
import org.apereo.cas.support.inwebo.web.flow.InweboMultifactorAuthenticationWebflowEventResolver;
import org.apereo.cas.support.inwebo.web.flow.InweboMultifactorTrustWebflowConfigurer;
import org.apereo.cas.support.inwebo.web.flow.InweboMultifactorWebflowConfigurer;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboCheckAuthenticationAction;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboCheckUserAction;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboMustEnrollAction;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboPushAuthenticateAction;
import org.apereo.cas.trusted.config.ConditionalOnMultifactorTrustedDevicesEnabled;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.StaticEventExecutionAction;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * The Inwebo MFA webflow configuration.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Configuration(value = "inweboWebflowConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
public class InweboWebflowConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Bean
    @Autowired
    public FlowDefinitionRegistry inweboFlowRegistry(
        @Qualifier("flowBuilder")
        final FlowBuilder flowBuilder,
        @Qualifier("flowBuilderServices")
        final FlowBuilderServices flowBuilderServices,
        final ConfigurableApplicationContext applicationContext) {
        val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
        builder.addFlowBuilder(flowBuilder, InweboMultifactorWebflowConfigurer.MFA_INWEBO_EVENT_ID);
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "inweboMultifactorWebflowConfigurer")
    @Bean
    @RefreshScope
    @Autowired
    public CasWebflowConfigurer inweboMultifactorWebflowConfigurer(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("loginFlowRegistry")
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        @Qualifier("inweboFlowRegistry")
        final FlowDefinitionRegistry inweboFlowRegistry,
        @Qualifier("flowBuilderServices")
        final FlowBuilderServices flowBuilderServices) {
        val cfg = new InweboMultifactorWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry,
            inweboFlowRegistry,
            applicationContext,
            casProperties,
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
        cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
        return cfg;
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboCasWebflowExecutionPlanConfigurer")
    @Autowired
    public CasWebflowExecutionPlanConfigurer inweboCasWebflowExecutionPlanConfigurer(
        @Qualifier("inweboMultifactorWebflowConfigurer")
        final CasWebflowConfigurer inweboMultifactorWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(inweboMultifactorWebflowConfigurer);
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboMultifactorAuthenticationWebflowEventResolver")
    @RefreshScope
    @Autowired
    public CasWebflowEventResolver inweboMultifactorAuthenticationWebflowEventResolver(
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new InweboMultifactorAuthenticationWebflowEventResolver(casWebflowConfigurationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboPushAuthenticateAction")
    @RefreshScope
    @Autowired
    public Action inweboPushAuthenticateAction(
        @Qualifier("inweboService")
        final InweboService inweboService) {
        return new InweboPushAuthenticateAction(inweboService);
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboCheckUserAction")
    @RefreshScope
    @Autowired
    public Action inweboCheckUserAction(
        @Qualifier("inweboService")
        final InweboService inweboService,
        final CasConfigurationProperties casProperties) {
        return new InweboCheckUserAction(inweboService, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboMustEnrollAction")
    @RefreshScope
    public Action inweboMustEnrollAction() {
        return new InweboMustEnrollAction();
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboCheckAuthenticationAction")
    @RefreshScope
    @Autowired
    public Action inweboCheckAuthenticationAction(
        @Qualifier("inweboMultifactorAuthenticationWebflowEventResolver")
        final CasWebflowEventResolver inweboMultifactorAuthenticationWebflowEventResolver,
        @Qualifier("inweboService")
        final InweboService inweboService) {
        return new InweboCheckAuthenticationAction(inweboService,
            inweboMultifactorAuthenticationWebflowEventResolver);
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboSuccessAction")
    @RefreshScope
    public Action inweboSuccessAction() {
        return StaticEventExecutionAction.SUCCESS;
    }

    /**
     * The Inwebo multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnMultifactorTrustedDevicesEnabled(prefix = "cas.authn.mfa.inwebo")
    @Configuration(value = "inweboMultifactorTrustConfiguration", proxyBeanMethods = false)
    public static class InweboMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "inweboMultifactorTrustWebflowConfigurer")
        @Bean
        @RefreshScope
        @Autowired
        public CasWebflowConfigurer inweboMultifactorTrustWebflowConfigurer(
            @Qualifier("inweboFlowRegistry")
            final FlowDefinitionRegistry inweboFlowRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("loginFlowRegistry")
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier("flowBuilderServices")
            final FlowBuilderServices flowBuilderServices) {
            val cfg = new InweboMultifactorTrustWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry,
                inweboFlowRegistry,
                applicationContext,
                casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
            return cfg;
        }

        @Bean
        @Autowired
        public CasWebflowExecutionPlanConfigurer inweboMultifactorTrustCasWebflowExecutionPlanConfigurer(
            @Qualifier("inweboMultifactorTrustWebflowConfigurer")
            final CasWebflowConfigurer inweboMultifactorTrustWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(inweboMultifactorTrustWebflowConfigurer);
        }
    }
}