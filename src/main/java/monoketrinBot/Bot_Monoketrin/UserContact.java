package monoketrinBot.Bot_Monoketrin;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_contacts2")
public class UserContact {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "occupation")
    private String occupation;

    // Кастомный конструктор без id
    public UserContact(Long userId, String firstName, String phoneNumber, String occupation) {
        this.userId = userId;
        this.firstName = firstName;
        this.phoneNumber = phoneNumber;
        this.occupation = occupation;}

    public UserContact() {} // Пустой конструктор для JPA
}
