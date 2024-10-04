package monoketrinBot.Bot_Monoketrin;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@EnableScheduling // Включаем планировщик задач
public class MyTelegramBot extends TelegramWebhookBot {

    private static final Logger logger = LoggerFactory.getLogger(MyTelegramBot.class);
    private final String botToken;
    private final String botUsername;
    private final String botPath;
    private final UserContactRepository userContactRepository;

    // ID владельца бота
    private final Long ownerId = 457785510L;

    // Хранение времени последнего действия пользователей
    private final Map<Long, Long> lastActionTimes = new HashMap<>();

    public MyTelegramBot(String botToken, String botUsername, String botPath, UserContactRepository userContactRepository) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.botPath = botPath;
        this.userContactRepository = userContactRepository;
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
        // Проверяем, есть ли текстовое сообщение
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long userId = update.getMessage().getFrom().getId(); // Используем userId
            String firstName = update.getMessage().getFrom().getFirstName();

            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getChatId().toString());

            // Обработка команды /start
            if ("/start".equals(messageText)) {
                message.setText(firstName + ", привет!❤\uFE0F\n\nНа связи Кейт, оставь свой номер телефона. Для этого нажми кнопку ниже:");
                addPhoneNumberRequestButton(message);
                lastActionTimes.put(userId, System.currentTimeMillis()); // Обновляем время последнего действия
            } else if (isOccupation(messageText)) {
                // Если выбрана сфера деятельности
                saveUserOccupation(userId, messageText); // Здесь сохраняем сферу деятельности
                message.setText("Отлично, благодарю тебя за ответ, чтобы получить подарок, кнопку ниже.\n\n" +
                        "\"КАК Я СДЕЛАЛА ЗАПУСК НА 5 МЛН НА OXBATAX 200-300?\", благодаря этому уроку сможешь сейчас собрать заявки на свой продукт/услугу.\n\n" +
                        "Внутри урока я разобрала:\n" +
                        "- Как за несколько сюжетных линий - прогреть аудиторию, даже если уже продавали много раз на аудиторию.\n" +
                        "- Почему сейчас нет продаж с блога?\n" +
                        "- Как продавать, но чтобы было не душно?\n" +
                        "- Почему аудитория не реагирует на ваш прогрев?\n" +
                        "- Каким сторителлингом уже сегодня собрать кучу заявок.\n\n" +
                        "Но это ещё не все подарки... Я подготовила для вас шаблон, который....\n" +
                        "Чтобы забрать, нужно посмотреть урок и найти кодовое слово, а потом написать, вышлю тебе шаблон.");
                addGiftButton(message); // Добавляем кнопку для получения подарка
                lastActionTimes.put(userId, System.currentTimeMillis()); // Обновляем время последнего действия
            } else if ("/userlist".equals(messageText)) {
                sendUserListToOwner();
            } else {
                message.setText("Я не понимаю тебя. Нажми кнопку ниже, чтобы отправить свой номер телефона.");
            }

            return message;
        }

        // Проверка наличия контакта
        if (update.hasMessage() && update.getMessage().hasContact()) {
            Contact contact = update.getMessage().getContact();
            Long userId = update.getMessage().getFrom().getId(); // Используем userId
            String firstName = update.getMessage().getFrom().getFirstName();
            String phoneNumber = contact.getPhoneNumber();

            // Сохраняем контакт с нулевой сферой деятельности
            saveUserContact(userId, firstName, phoneNumber, null); // Сохранение без сферы деятельности

            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getChatId().toString());
            message.setText("Спасибо, " + firstName + "! Теперь давай познакомимся поближе. Выбери свою сферу деятельности, нажав на одну из кнопок ниже.");
            addOccupationButtons(message);
            lastActionTimes.put(userId, System.currentTimeMillis()); // Обновляем время последнего действия

            return message;
        }

        return null;
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
    // todo прочитать про прокси в спринге и как работает transactional
//    @Transactional
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
    @Transactional
    public void saveUserOccupation(Long userId, String occupation) {
        UserContact user = userContactRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setOccupation(occupation); // Устанавливаем новую сферу деятельности
//            userContactRepository.save(user); // Сохраняем изменения
        } else {
            System.out.println("Пользователь не найден для ID: " + userId);
        }
    }
}
