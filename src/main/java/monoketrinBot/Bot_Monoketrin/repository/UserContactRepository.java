package monoketrinBot.Bot_Monoketrin.repository;

import monoketrinBot.Bot_Monoketrin.entity.UserContact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserContactRepository extends JpaRepository<UserContact, Long> {
}
