package monoketrinBot.Bot_Monoketrin;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_contacts")
public class UserContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "occupation")
    private String occupation;

    // Кастомный конструктор без id
    public UserContact(Long userId, String firstName, String phoneNumber, String occupation) {
        this.userId = userId;
        this.firstName = firstName;
        this.phoneNumber = phoneNumber;
        this.occupation = occupation;
    }

    public UserContact() {} // Пустой конструктор для JPA
}
