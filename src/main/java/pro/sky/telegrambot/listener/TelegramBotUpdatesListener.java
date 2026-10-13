package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationTaskService notificationTaskService;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {
                logger.info("Processing update: {}", update);

                if (update.message() == null) {
                    return;
                }

                String text = update.message().text();
                Long chatId = update.message().chat().id();

                if ("/start".equals(text)) {
                    SendMessage message = new SendMessage(chatId,
                            "Привет! Я бот-напоминалка. " +
                                    "Отправь мне сообщение в формате: " +
                                    "'01.01.2022 20:00 Сделать домашнюю работу', " +
                                    "и я напомню тебе в назначенное время!");
                    telegramBot.execute(message);
                } else {
                    notificationTaskService.parseAndSave(chatId, text);
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}