package xyz.jiangsier.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings("unused")
public class RedissonClientConfig {
    @Value("${redis.datasource.url}")
    private String nodeAddress;
    @Value("${redis.datasource.password}")
    private String password;
    @Value("${redis.datasource.timeout}")
    private Integer timeout;

    @Bean(destroyMethod="shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();

        config.useClusterServers()
                .addNodeAddress(nodeAddress)
                .setPassword(password)
                .setTimeout(timeout);
        return Redisson.create(config);
    }
}
