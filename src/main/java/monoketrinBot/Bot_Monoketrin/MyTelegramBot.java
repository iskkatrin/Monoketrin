package monoketrinBot.Bot_Monoketrin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class MyTelegramBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(MyTelegramBot.class);

    @Override
    public String getBotUsername() {
        return "MonoketrinBot";
    }

    @Override
    public String getBotToken() {
        return "7341071576:AAFG0FzcLsHMa676-l7pqWFTUk-gRV026Ok";
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Проверяем, пришло ли сообщение и имеет ли оно текст
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            String userName = update.getMessage().getFrom().getUserName();
            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();

            // Логируем информацию о пользователе
            logger.info("Получено сообщение от пользователя: {} {} (username: {}), chatId: {}",
                    firstName, lastName, userName, chatId);

            // Создаём объект для отправки сообщения
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());

            // Обработка команды /start
            if ("/start".equals(messageText)) {
                message.setText("Привет! Нажми кнопку СТАРТ, чтобы запустить бот и забрать подарок.");
                addStartButton(message);
            }
            // Обработка нажатия кнопки "СТАРТ"
            else if ("СТАРТ".equals(messageText)) {
                message.setText("Поздравляю! Ты запустил бота и забрал свой подарок 🎁");
            }
            // Ответ на любые другие сообщения
            else {
                message.setText("Я не понимаю тебя. Нажми кнопку СТАРТ, чтобы начать.");
            }

            // Отправляем сообщение пользователю
            try {
                execute(message);
                // Логируем успешную отправку сообщения
                logger.info("Сообщение отправлено пользователю с chatId: {}", chatId);
            } catch (Exception e) {
                // Логируем ошибку при отправке сообщения
                logger.error("Ошибка при отправке сообщения пользователю с chatId: {}", chatId, e);
            }
        }
    }

    // Метод для добавления клавиатуры с кнопкой "СТАРТ"
    private void addStartButton(SendMessage message) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("СТАРТ"));

        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
    }
}

