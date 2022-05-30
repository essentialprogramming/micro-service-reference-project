package com.config;

import com.spring.ApplicationContextFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.configuration.JobRunrConfiguration;
import org.jobrunr.configuration.JobRunrMicroMeterIntegration;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.server.JobActivator;
import org.jobrunr.storage.sql.common.SqlStorageProviderFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static org.jobrunr.server.BackgroundJobServerConfiguration.usingStandardBackgroundJobServerConfiguration;

@Configuration
public class JobRunrConfig {

    final boolean isBackgroundJobServerEnabled = true; // or get it via ENV variables
    final boolean isDashboardEnabled = true; // or get it via ENV variables
    final MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @Bean
    public JobRunrConfiguration.JobRunrConfigurationResult initJobRunner(final JobActivator jobActivator) {
        return JobRunr.configure()
                .useJobActivator(jobActivator)
                .useStorageProvider(SqlStorageProviderFactory
                        .using(ApplicationContextFactory.getBean(DataSource.class)))
                .useBackgroundJobServerIf(isBackgroundJobServerEnabled,
                        usingStandardBackgroundJobServerConfiguration()
                                .andWorkerCount(Runtime.getRuntime().availableProcessors())
                                .andPollIntervalInSeconds(15))
                .useDashboardIf(isDashboardEnabled, 1000)
                .useMicroMeter(new JobRunrMicroMeterIntegration(meterRegistry))
                .useJmxExtensions()
                .initialize();
    }

    @Bean
    public JobScheduler initJobScheduler(JobRunrConfiguration.JobRunrConfigurationResult jobRunrConfigurationResult) {
        return jobRunrConfigurationResult.getJobScheduler();
    }

    @Bean
    public JobRequestScheduler initJobRequestScheduler(JobRunrConfiguration.JobRunrConfigurationResult jobRunrConfigurationResult) {
        return jobRunrConfigurationResult.getJobRequestScheduler();
    }

    @Bean
    public JobActivator jobActivator(final ApplicationContext applicationContext) {
        return applicationContext::getBean;
    }

}