package net.trackme.telegramservice;

import lombok.AllArgsConstructor;
import net.trackme.telegramservice.configuration.NotificationBot;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@AllArgsConstructor
@SpringBootApplication
public class TelegramServiceApplication {
    private final NotificationBot notificationBot;

    public static void main(String[] args) {
        SpringApplication.run(TelegramServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner notificationBotRegister() {
        return args -> {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            try {
                telegramBotsApi.registerBot(notificationBot);
            } catch (TelegramApiRequestException e) {
                System.out.println("Telegram bot API could not register notification bot.");
            }
        };
    }
}