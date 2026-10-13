package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationTaskService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationTaskService.class);

    private static final Pattern MESSAGE_PATTERN =
            Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2})\\s+(.+)");

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final NotificationTaskRepository notificationTaskRepository;
    private final TelegramBot telegramBot;

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository, TelegramBot telegramBot) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.telegramBot = telegramBot;
    }

    public void parseAndSave(Long chatId, String messageText) {
        logger.info("Was invoked method for parse and save notification task");

        Matcher matcher = MESSAGE_PATTERN.matcher(messageText);

        if (!matcher.matches()) {
            logger.warn("Message does not match pattern: {}", messageText);
            return;
        }

        String dateTimeString = matcher.group(1);
        String reminderText = matcher.group(2);

        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);

        NotificationTask task = new NotificationTask(chatId, reminderText, dateTime);
        notificationTaskRepository.save(task);

        logger.info("Saved notification task: chatId={}, message='{}', dateTime={}",
                chatId, reminderText, dateTime);
    }
    @Scheduled(cron = "0 * * * * *")
    public void sendNotifications() {
        logger.info("Was invoked method for send notifications by scheduler");

        LocalDateTime currentMinute = LocalDateTime.now()
                .withSecond(0)
                .withNano(0);

        logger.debug("Searching notification tasks for: {}", currentMinute);

        List<NotificationTask> tasks = notificationTaskRepository.findByDateTime(currentMinute);

        logger.info("Found {} notification tasks for current minute", tasks.size());

        for (NotificationTask task : tasks) {
            SendMessage message = new SendMessage(task.getChatId(), task.getMessage());
            telegramBot.execute(message);
            logger.info("Sent notification to chatId={}: {}",
                    task.getChatId(), task.getMessage());
        }
    }
}