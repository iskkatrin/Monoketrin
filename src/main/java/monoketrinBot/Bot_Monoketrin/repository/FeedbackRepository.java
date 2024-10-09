package monoketrinBot.Bot_Monoketrin.repository;

import monoketrinBot.Bot_Monoketrin.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
