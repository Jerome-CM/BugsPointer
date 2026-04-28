package com.bugspointer.dto;

import com.bugspointer.entity.Customer;
import com.bugspointer.entity.EnumIndicatif;
import com.bugspointer.entity.EnumPlan;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class AccountDTO {

    public interface PasswordLost {}
    public interface ResetPassword {}
    public interface Domain {}
    public interface Delete {}

    private Long id;

    private String customerId;

    @NotBlank(message = "L'e-mail est obligatoire", groups = PasswordLost.class)
    @Email(message = "L'e-mail n'est pas valide")
    private String mail;

    @NotBlank(message = "Le mot de passe est obligatoire", groups = {Delete.class, ResetPassword.class})
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères", groups = ResetPassword.class)
    private String password;

    private String newPassword;

    @NotBlank(message = "La confirmation du mot de passe est obligatoire", groups = ResetPassword.class)
    private String confirmPassword;

    private EnumIndicatif indicatif;

    @Pattern(regexp = "^$|[0-9]{10}", message = "Le numéro doit contenir 10 chiffres")
    private String phoneNumber;

    private String publicKey;

    private EnumPlan plan;

    @NotBlank(message = "Le domaine est obligatoire", groups = Domain.class)
    @Pattern(regexp = "^(https?://)?(www\\.)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}/?$", message = "Le domaine doit ressembler à exemple.fr ou https://www.exemple.fr", groups = Domain.class)
    private String domaine;

    private boolean domainVerified;
}
