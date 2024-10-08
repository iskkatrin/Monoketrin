package monoketrinBot.Bot_Monoketrin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class WebhookController {
    private final MyTelegramBot telegramBot;

    public WebhookController(MyTelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostMapping("/webhook")
    public ResponseEntity<BotApiMethod<?>> onUpdateReceived(@RequestBody Update update) {
        // Логирование полученного обновления
        System.out.println("Получено обновление: " + update);

        BotApiMethod<?> response = telegramBot.onWebhookUpdateReceived(update);
        return ResponseEntity.ok(response);
    }
}
