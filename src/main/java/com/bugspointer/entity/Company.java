package com.bugspointer.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Data
public class Company {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long companyId;

    private String companyName;

    private String mail;

    private String password;

    private EnumIndicatif indicatif = EnumIndicatif.FRANCE;

    private String phoneNumber;

    private String domaine;

    private EnumPlan plan = EnumPlan.FREE;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateLineFacturePlan;

    @Column(unique = true, updatable = false)
    private String publicKey = null;

    @NotNull
    @Column(updatable = false)
    private Date dateCreation = new Date();

    private Date lastVisit = new Date();

    private boolean isEnable; //Compte actif ou non ?

    private EnumMotif motifEnable; //Si isEnable = true => motifEnable = VALIDATE, Sinon indiquer le motif

    private Date dateCloture; //Ajoute la date quand le compte passe en inactif

    private String role = "ROLE_USER";

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private Customer customer;

    @Override
    public String toString() {

        if (customer != null) {
            return "Company{" +
                    "companyId=" + companyId +
                    ", companyName='" + companyName + '\'' +
                    ", mail='" + mail + '\'' +
                    ", password='" + password + '\'' +
                    ", indicatif=" + indicatif +
                    ", phoneNumber='" + phoneNumber + '\'' +
                    ", domaine='" + domaine + '\'' +
                    ", plan=" + plan +
                    ", dateLineFacturePlan=" + dateLineFacturePlan +
                    ", publicKey='" + publicKey + '\'' +
                    ", dateCreation=" + dateCreation +
                    ", lastVisit=" + lastVisit +
                    ", isEnable=" + isEnable +
                    ", motifEnable=" + motifEnable +
                    ", dateCloture=" + dateCloture +
                    ", role='" + role + '\'' +
                    ", Customer(id='" + customer.getId() + '\'' +
                    ", customer_id='" + customer.getCustomerId() + '\'' +
                    ")}";
        } else {
            return "Company{" +
                    "companyId=" + companyId +
                    ", companyName='" + companyName + '\'' +
                    ", mail='" + mail + '\'' +
                    ", password='" + password + '\'' +
                    ", indicatif=" + indicatif +
                    ", phoneNumber='" + phoneNumber + '\'' +
                    ", domaine='" + domaine + '\'' +
                    ", plan=" + plan +
                    ", dateLineFacturePlan=" + dateLineFacturePlan +
                    ", publicKey='" + publicKey + '\'' +
                    ", dateCreation=" + dateCreation +
                    ", lastVisit=" + lastVisit +
                    ", isEnable=" + isEnable +
                    ", motifEnable=" + motifEnable +
                    ", dateCloture=" + dateCloture +
                    ", role='" + role + '\'' +
                    ")}";
        }
    }
}
