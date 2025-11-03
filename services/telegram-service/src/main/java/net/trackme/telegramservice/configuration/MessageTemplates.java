package net.trackme.telegramservice.configuration;

public class MessageTemplates {
    public static String startMessageTemplate = """
                                  Вас приветствует бот оповещений TrackMe.
                                  Я буду присылать вам оповещения о пропущенных встречах.
                                  """;

    public static String meetingNotHappenedMessageTemplate = """
                                               Назначенная встреча не состоялась!
            
                                               Ваша команда {teamCardName} с потока {streamName} пропустила назначенную встречу.
                                               Вы можете узнать информацию о пропущенной встрече, перейдя по ссылке ниже:
                                               {meetingLink}
                                               """;
}
