package monoketrinBot.Bot_Monoketrin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    @Bean
    public MyTelegramBot myTelegramBot() {
        // Укажите ваш токен, имя бота и новый путь вебхука.
        return new MyTelegramBot(
                "7341071576:AAFG0FzcLsHMa676-l7pqWFTUk-gRV026Ok",
                "MonoketrinBot",
                "https://a5ba-2a0d-5600-73-1000-e0f2-8a77-2258-fd1e.ngrok-free.app/webhook"
        );
    }
}
