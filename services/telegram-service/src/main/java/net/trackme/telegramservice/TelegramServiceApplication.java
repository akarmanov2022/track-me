package net.trackme.telegramservice;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.trackme.telegramservice.configuration.NotificationBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@AllArgsConstructor
@SpringBootApplication
public class TelegramServiceApplication {
    public static void main(String[] args) throws TelegramApiException {
        var context = SpringApplication.run(TelegramServiceApplication.class, args);
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(context.getBean("notificationBot", NotificationBot.class));
        } catch (TelegramApiException e) {
            log.error("Telegram bot API could not register notification bot.");
        }
    }
}