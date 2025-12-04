package common.config;

import common.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowflakeConfig {

    @Value("${snowflake.datacenter-id:1}")
    private long datacenterId;

    @Value("${snowflake.worker-id:1}")
    private long workerId;

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        return new SnowflakeIdGenerator(datacenterId, workerId);
    }
}
