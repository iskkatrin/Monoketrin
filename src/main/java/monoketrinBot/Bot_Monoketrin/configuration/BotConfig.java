package monoketrinBot.Bot_Monoketrin.configuration;

import monoketrinBot.Bot_Monoketrin.MyTelegramBot;
import monoketrinBot.Bot_Monoketrin.repository.FeedbackRepository;
import monoketrinBot.Bot_Monoketrin.repository.UserContactRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class BotConfig {

    private final UserContactRepository userContactRepository;

    @Autowired
    public BotConfig(UserContactRepository userContactRepository) {
        this.userContactRepository = userContactRepository;
    }

    @Bean
    public MyTelegramBot myTelegramBot(FeedbackRepository feedbackRepository) {
        // Передаем репозиторий в конструктор
        return new MyTelegramBot(
                "7341071576:AAFG0FzcLsHMa676-l7pqWFTUk-gRV026Ok",
                "MonoketrinBot",
                "https://a5ba-2a0d-5600-73-1000-e0f2-8a77-2258-fd1e.ngrok-free.app/webhook",
                userContactRepository,
                feedbackRepository
        );
    }
}
