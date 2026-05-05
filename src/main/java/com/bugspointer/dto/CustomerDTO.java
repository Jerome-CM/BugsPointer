package com.bugspointer.dto;

import com.bugspointer.entity.EnumPlan;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class CustomerDTO extends MetaDataMollieDTO{

    public interface Billing {}
    public interface Mandate {}

    @NotBlank(message = "L'e-mail est obligatoire", groups = Billing.class)
    @Email(message = "L'e-mail n'est pas valide", groups = Billing.class)
    private String mail;

    @NotBlank(message = "Le nom de l'entreprise est obligatoire", groups = Billing.class)
    private String companyName;

    @NotBlank(message = "L'adresse est obligatoire", groups = Billing.class)
    private String address1;

    private String address2;

    @NotBlank(message = "Le code postal est obligatoire", groups = Billing.class)
    private String cp;

    @NotBlank(message = "La ville est obligatoire", groups = Billing.class)
    private String city;

    @NotBlank(message = "Le pays est obligatoire", groups = Billing.class)
    private String country;

    @NotNull(message = "Le plan est obligatoire", groups = {Billing.class, Mandate.class})
    private EnumPlan plan;

    @AssertTrue(message = "Vous devez accepter les CGU et CGV", groups = Billing.class)
    private boolean cguAccepted;

    private String publicKey;

    @NotBlank(message = "L'IBAN est obligatoire", groups = Mandate.class)
    @Pattern(regexp = "(?i)^[A-Z]{2}[0-9A-Z ]{13,32}$", message = "L'IBAN n'est pas valide", groups = Mandate.class)
    private String iban;

    @NotBlank(message = "Le BIC est obligatoire", groups = Mandate.class)
    @Pattern(regexp = "(?i)^[A-Z0-9]{8}([A-Z0-9]{3})?$", message = "Le BIC n'est pas valide", groups = Mandate.class)
    private String bic;

    @AssertTrue(message = "Vous devez signer le mandat SEPA", groups = Mandate.class)
    private boolean signature;

}
