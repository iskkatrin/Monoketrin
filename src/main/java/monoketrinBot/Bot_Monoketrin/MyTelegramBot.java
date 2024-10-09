package monoketrinBot.Bot_Monoketrin;

import monoketrinBot.Bot_Monoketrin.entity.Feedback;
import monoketrinBot.Bot_Monoketrin.entity.UserContact;
import monoketrinBot.Bot_Monoketrin.repository.FeedbackRepository;
import monoketrinBot.Bot_Monoketrin.repository.UserContactRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


@EnableScheduling // Включаем планировщик задач
public class MyTelegramBot extends TelegramWebhookBot {

    private static final Logger logger = LoggerFactory.getLogger(MyTelegramBot.class);
    private final String botToken;
    private final String botUsername;
    private final String botPath;
    private final UserContactRepository userContactRepository;
    private final FeedbackRepository feedbackRepository;
    private Set<Long> userAwaitingFeedback = new HashSet<>();

    // ID владельца бота
    private final Long ownerId = 457785510L;

    // Хранение времени последнего действия пользователей
    private final Map<Long, Long> lastActionTimes = new HashMap<>();

    public MyTelegramBot(String botToken, String botUsername, String botPath, UserContactRepository userContactRepository, FeedbackRepository feedbackRepository) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.botPath = botPath;
        this.userContactRepository = userContactRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        SendMessage message = new SendMessage();

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long userId = update.getMessage().getFrom().getId();
            String firstName = update.getMessage().getFrom().getFirstName();
            message.setChatId(update.getMessage().getChatId().toString());

            if ("/start".equals(messageText)) {
                message.setText(firstName + ", привет!❤\uFE0F\n\nНа связи Кейт, оставь свой номер телефона. Для этого нажми кнопку ниже:");
                addPhoneNumberRequestButton(message);
                lastActionTimes.put(userId, System.currentTimeMillis());

            } else if ("/userlist".equals(messageText)) {
                sendUserListToOwner();
            } else if (isOccupation(messageText)) {
                saveUserOccupation(userId, messageText);
                message.setText("Отлично, благодарю тебя за ответ, чтобы получить подарок, нажми кнопку ниже.\n\n" +
                        "\"КАК Я СДЕЛАЛА ЗАПУСК НА 5 МЛН НА OXBATAX 200-300?\", благодаря этому уроку сможешь сейчас собрать заявки на свой продукт/услугу.\n\n");
                addGiftButton(message);
                lastActionTimes.put(userId, System.currentTimeMillis());
            } else if ("ЗАБИРАЙ УРОК".equals(messageText)) {
                sendLessonLink(userId);
                message.setText("Но это ещё не все подарки... Я подготовила для вас шаблон. Чтобы его забрать, нужно посмотреть урок, после чего я отправлю материал.");
                sendReminderIn10Minutes(userId);
            } else if ("Да".equals(messageText)) {
                // Отправляем шаблон с текстом и добавляем пользователя в список ожидания обратной связи
                sendTemplateLink(userId);
                message.setText("Супер! Ты сделал шаг к следующему подарку от меня\n\n" +
                        "Лови шаблон, который поможет всего за 15 сторис собрать заявки на любой ваш продукт. Я сама благодаря нему собрала с охватов 200-22 заявки за один вечер на личную работу.\n\n" +
                        "После просмотра урока и прочтения шаблона возвращайся и поделись, пожалуйста, обратной связью\uD83D\uDC97\n\n" +
                        "Я делюсь тем, за что обычно люди берут большие деньги или продают это на консультациях. Для меня важно быть ВКЛАДОМ в каждого человека, который попал ко мне в блог!\n\n" +
                        "Поэтому давай сделаем равноценный обмен: с меня подарки и полезный контент, с тебя — обратная связь!\n\n" +
                        "Напиши, что вынесешь для себя полезного из этих материалов.");
                userAwaitingFeedback.add(userId); // Добавляем пользователя в список ожидания обратной связи
                lastActionTimes.put(userId, System.currentTimeMillis());
            } else if (userAwaitingFeedback.contains(userId) && !Arrays.asList("Да", "Нет").contains(messageText)) {
                // Если пользователь в списке ожидания и отправил текст, считаем это обратной связью
                String feedbackText = update.getMessage().getText();
                String userName = update.getMessage().getFrom().getFirstName();
                saveFeedback(userId, userName, feedbackText); // Сохраняем обратную связь

                message.setText("Спасибо за обратную связь!❤\uFE0F Мы ценим твоё мнение.");
                userAwaitingFeedback.remove(userId); // Убираем пользователя из списка ожидания обратной связи
            } else if ("Нет".equals(messageText)) {
                message.setText("Сколько времени тебе нужно на просмотр?");
                message.setReplyMarkup(createTimeOptionsKeyboard());
            } else if (Arrays.asList("30 минут", "5 часов", "1 день").contains(messageText)) {
                long delay = getDelayForTimeOption(messageText);
                scheduleReminder(userId, delay);
                message.setText("Хорошо, напомню через " + messageText.toLowerCase() + ".❤\uFE0F");
            } else if ("/feedback".equalsIgnoreCase(messageText)) {
                // Команда для получения всех отзывов
                String allFeedbacks = getAllFeedbacks();
                message.setText(allFeedbacks);
            } else {
                message.setText("Я не понимаю тебя. Нажми кнопку ниже, чтобы отправить свой номер телефона.");
            }
        } else if (update.hasMessage() && update.getMessage().hasContact()) {
            Contact contact = update.getMessage().getContact();
            Long userId = update.getMessage().getFrom().getId();
            String firstName = update.getMessage().getFrom().getFirstName();
            String phoneNumber = contact.getPhoneNumber();

            saveUserContact(userId, firstName, phoneNumber, null);
            message.setChatId(update.getMessage().getChatId().toString());
            message.setText("Спасибо, " + firstName + "! Теперь давай познакомимся поближе. Выбери свою сферу деятельности, нажав на одну из кнопок ниже.");
            addOccupationButtons(message);
            lastActionTimes.put(userId, System.currentTimeMillis());
        }

        return message;
    }


    // Запланированное задание для проверки бездействия пользователей
    @Scheduled(fixedRate = 60000) // Проверяем каждую минуту
    public void checkInactiveUsers() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<Long, Long> entry : lastActionTimes.entrySet()) {
            Long userId = entry.getKey();
            Long lastActionTime = entry.getValue();

            // Если прошло больше 30 минут с последнего действия
            if (currentTime - lastActionTime > 30 * 60 * 1000) {
                sendReminderMessage(userId);
                lastActionTimes.remove(userId); // Удаляем пользователя из списка, чтобы не отправлять повторное сообщение
            }
        }
    }

    // Метод для отправки напоминалки
    private void sendReminderMessage(Long userId) {
        SendMessage message = new SendMessage();
        message.setChatId(userId.toString());
        message.setText("Понимаю, бывает такое, что можешь отвлечься\n\n" +
                "Ты уже в шаге от того, чтобы получить подарок, а именно урок без сухой теории, а с применимыми инструментами и примерами моих сторителлингов. " +
                "С этой информацией сможешь уже сейчас собрать заявки на свой продукт/услугу\n\n" +
                "Нажимай на кнопку выше, чтобы я понимала, в чем могу быть полезна для тебя.");
        try {
            execute(message);
        } catch (Exception e) {
            logger.error("Ошибка при отправке напоминания пользователю с ID: " + userId, e);
        }
    }

    // Вспомогательный метод для проверки, является ли текст выбором сферы деятельности
    private boolean isOccupation(String messageText) {
        return messageText.equals("Я эксперт") || messageText.equals("Я специалист") ||
                messageText.equals("Я продюсер") || messageText.equals("Я работаю в найме") ||
                messageText.equals("Я не работаю, ищу себя");
    }


    // Метод для отправки списка пользователей владельцу
    private void sendUserListToOwner() {
        List<UserContact> users = userContactRepository.findAll(); // Получаем всех пользователей
        StringBuilder userList = new StringBuilder("Список пользователей:\n");

        for (UserContact user : users) {
            userList.append("Имя: ").append(user.getFirstName())
                    .append(", Номер: ").append(user.getPhoneNumber())
                    .append(", Сфера деятельности: ").append(user.getOccupation())
                    .append("\n");
        }

        // Отправляем сообщение владельцу
        SendMessage ownerMessage = new SendMessage();
        ownerMessage.setChatId(ownerId.toString());
        ownerMessage.setText(userList.toString());

        try {
            execute(ownerMessage); // Метод для отправки сообщения
        } catch (Exception e) {
            e.printStackTrace(); // Обработка ошибок
        }
    }

    // Метод для получения сферы деятельности пользователя
    public String getOccupationForUser(Long userId) {
        UserContact userContact = userContactRepository.findById(userId).orElse(null);
        if (userContact != null) {
            return userContact.getOccupation(); // Возвращаем сферу деятельности, если пользователь найден
        }
        return null; // Если пользователь не найден, возвращаем null
    }

    // Метод для добавления кнопки запроса номера телефона
    private void addPhoneNumberRequestButton(SendMessage message) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardButton contactButton = new KeyboardButton();
        contactButton.setText("Отправить номер телефона");
        contactButton.setRequestContact(true);

        KeyboardRow row = new KeyboardRow();
        row.add(contactButton);

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
    }

    // Метод для добавления кнопок выбора сферы деятельности
    private void addOccupationButtons(SendMessage message) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Я эксперт"));
        row1.add(new KeyboardButton("Я специалист"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Я продюсер"));
        row2.add(new KeyboardButton("Я работаю в найме"));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("Я не работаю, ищу себя"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
    }

    // Метод для добавления кнопки получения подарка
    private void addGiftButton(SendMessage message) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardButton giftButton = new KeyboardButton();
        giftButton.setText("ЗАБИРАЙ УРОК");

        KeyboardRow row = new KeyboardRow();
        row.add(giftButton);

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
    }

    // Метод для сохранения контакта пользователя
    public void saveUserContact(Long userId, String firstName, String phoneNumber, String occupation) {
        UserContact userContact = userContactRepository.findById(userId)
                .orElse(new UserContact(userId, firstName, phoneNumber, occupation)); // Создаем нового пользователя, если не найден

        userContact.setFirstName(firstName);
        userContact.setPhoneNumber(phoneNumber);

        // Если передана сфера деятельности, обновляем ее
        if (occupation != null) {
            userContact.setOccupation(occupation);
        }

        userContactRepository.save(userContact); // Сохраняем пользователя
//        log.info("contact saved")
    }

    // Метод для сохранения сферы деятельности пользователя
    public void saveUserOccupation(Long userId, String occupation) {
        UserContact user = userContactRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setOccupation(occupation); // Устанавливаем новую сферу деятельности
            userContactRepository.save(user); // Сохраняем изменения
        } else {
            System.out.println("Пользователь не найден для ID: " + userId);
        }
    }

    private void sendReminderIn10Minutes(Long userId) {
        new Thread(() -> {
            try {
                Thread.sleep(10 * 60 * 1000);
                SendMessage reminder = new SendMessage();
                reminder.setChatId(userId.toString());
                reminder.setText("Удалось посмотреть урок?");
                addYesNoButtons(reminder);
                execute(reminder);
            } catch (Exception e) {
                logger.error("Ошибка при отправке напоминания через 10 минут пользователю с ID: " + userId, e);
            }
        }).start();
    }

    private void addYesNoButtons(SendMessage message) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("Да"));
        row.add(new KeyboardButton("Нет"));

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
    }

    // Метод для создания клавиатуры с вариантами времени
    private ReplyKeyboardMarkup createTimeOptionsKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("30 минут"));
        row.add(new KeyboardButton("5 часов"));
        row.add(new KeyboardButton("1 день"));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        return keyboardMarkup;
    }

    // Метод для создания клавиатуры с вариантами "Да" и "Нет"
    private ReplyKeyboardMarkup createYesNoKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("Да"));
        row.add(new KeyboardButton("Нет"));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        return keyboardMarkup;
    }

    // Метод для определения задержки в миллисекундах в зависимости от выбора пользователя
    private long getDelayForTimeOption(String option) {
        switch (option) {
            case "30 минут":
                return 30 * 60 * 1000; // 30 минут в миллисекундах
            case "5 часов":
                return 5 * 60 * 60 * 1000; // 5 часов в миллисекундах
            case "1 день":
                return 24 * 60 * 60 * 1000; // 1 день в миллисекундах
            default:
                return 0;
        }
    }

    // Метод для планирования отправки напоминания
    private void scheduleReminder(Long userId, long delay) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SendMessage reminder = new SendMessage();
                reminder.setChatId(userId.toString()); // Используем userId в качестве chatId
                reminder.setText("Удалось ли посмотреть урок?");
                reminder.setReplyMarkup(createYesNoKeyboard());
                executeMessage(reminder);
            }
        }, delay);
    }

    // Метод для отправки сообщения пользователю
    private void executeMessage(SendMessage message) {
        try {
            // Вызов API для отправки сообщения
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Метод для отправки ссылки на видеоурок
    private void sendLessonLink(Long userId) {
        SendMessage message = new SendMessage();
        message.setChatId(userId.toString());
        message.setText("https://youtu.be/HcQzxIQPqAo?feature=shared");
        executeMessage(message);
    }

    // Метод для отправки ссылки на шаблон
    private void sendTemplateLink(Long userId) {
        SendMessage message = new SendMessage();
        message.setChatId(userId.toString());
        message.setText("https://docs.google.com/document/d/1bGS6FxBN3f-Xuqc6EkiijVU6qpGN2gIv5IeBYWV3dhk/edit?usp=sharing");
        executeMessage(message);
    }

    // Метод для сохранения обратной связи
    private void saveFeedback(Long userId, String userName, String feedbackText) {
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setUserName(userName);
        feedback.setFeedback(feedbackText);
        feedbackRepository.save(feedback);
    }

    // Метод для отправки обратной связи
    private String getAllFeedbacks() {
        StringBuilder feedbacksBuilder = new StringBuilder("Обратная связь от пользователей:\n\n");
        feedbackRepository.findAll().forEach(feedback ->
                feedbacksBuilder.append(feedback.getUserName())
                        .append(": ")
                        .append(feedback.getFeedback())
                        .append("\n\n")
        );
        return feedbacksBuilder.toString();
    }
}
