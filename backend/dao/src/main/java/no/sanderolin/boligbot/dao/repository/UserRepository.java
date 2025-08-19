package no.sanderolin.boligbot.dao.repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import no.sanderolin.boligbot.dao.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Long> {
    boolean existsByEmail(@Email(message = "Email should be valid") @NotBlank(message = "Email is required") String email);
    Optional<UserModel> findByEmail(@Email(message = "Email should be valid") @NotBlank(message = "Email is required") String email);
}
