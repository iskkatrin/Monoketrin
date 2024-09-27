package monoketrinBot.Bot_Monoketrin;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.util.ArrayList;
import java.util.List;

@Component
public class MyTelegramBot extends TelegramWebhookBot {

    private String botToken;
    private String botUsername;
    private String botPath;

    public MyTelegramBot(String botToken, String botUsername, String botPath) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.botPath = botPath;
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
        // Обработка обновления
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            // Создание сообщения
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());

            // Обработка команд
            if ("/start".equals(messageText)) {
                message.setText("Привет! Нажми кнопку СТАРТ, чтобы запустить бот и забрать подарок.");
                addStartButton(message);
            } else if ("СТАРТ".equals(messageText)) {
                message.setText("Поздравляю! Ты запустил бота и забрал свой подарок 🎁");
            } else {
                message.setText("Я не понимаю тебя. Нажми кнопку СТАРТ, чтобы начать.");
            }

            return message;
        }

        return null;
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
