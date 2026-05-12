package com.company.vzvod.bot.config;

import com.company.vzvod.bot.BotApiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BotApiProperties.class)
public class BotApiConfiguration {
}
