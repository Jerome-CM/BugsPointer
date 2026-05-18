package com.bugspointer.service.implementation;

import be.woutschoovaerts.mollie.Client;
import be.woutschoovaerts.mollie.data.customer.CustomerResponse;
import be.woutschoovaerts.mollie.data.mandate.MandateResponse;
import be.woutschoovaerts.mollie.data.mandate.MandateStatus;
import be.woutschoovaerts.mollie.exception.MollieException;
import com.bugspointer.configuration.CustomExceptions;
import com.bugspointer.dto.CustomerDTO;
import com.bugspointer.dto.MandateDTO;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.Customer;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.service.ICustomer;
import com.bugspointer.utility.Utility;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CustomerService implements ICustomer {

    private static final DateTimeFormatter MANDATE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CompanyRepository companyRepository;

    private final ModelMapper modelMapper;

    private final Client client;

    public CustomerService(CompanyRepository companyRepository, ModelMapper modelMapper, Client client) {
        this.companyRepository = companyRepository;
        this.modelMapper = modelMapper;
        this.client = client;
    }

    public CustomerDTO getCustomerDTO(Customer customer) {
        CustomerDTO dto;
        dto = modelMapper.map(customer, CustomerDTO.class);
        return dto;
    }

    /**
     * For mail new mandate
     * @param customer
     * @return
     * @throws MollieException
     */
    public HashMap<String, String> getDataToMandateForCustomer(CustomerDTO customer) throws MollieException {
        HashMap<String, String> contentData = new HashMap();

        Company company = companyRepository.findByPublicKey(customer.getPublicKey()).get();
        if (company != null) {
            List<MandateResponse> mandates = client.mandates().listMandates(company.getCustomer().getCustomerId()).getEmbedded().getMandates();

            if (mandates.size() > 0) {
                MandateResponse mandateResponse = client.mandates().getMandate(company.getCustomer().getCustomerId(), mandates.get(0).getId());
                if (mandateResponse.getStatus().equals(MandateStatus.VALID)) {
                    LocalDate signatureDate = mandateResponse.getSignatureDate();

                    contentData.put("status", "OK");
                    contentData.put("reference", formatMandateReference(mandateResponse.getMandateReference(), signatureDate));
                    contentData.put("dateSignature", formatMandateDate(signatureDate));
                    contentData.put("dateExpiration", formatMandateDate(signatureDate == null ? null : signatureDate.plusYears(4)));
                    contentData.put("dateNextPayment", formatMandateDate(signatureDate == null ? null : signatureDate.plusYears(1)));
                    contentData.put("iban", maskIban(mandateResponse.getDetails().getConsumerAccount().orElse("")));
                    contentData.put("bic", maskBic(mandateResponse.getDetails().getConsumerBic().orElse("")));

                } else {
                    contentData.put("status", "ERROR");
                }

            } else {
                contentData.put("status", "ERROR");
            }

        } else {
            contentData.put("status", "ERROR");
        }

        return contentData;
    }

    private String formatMandateDate(LocalDate date) {
        if (date == null) {
            return "--";
        }
        return date.format(MANDATE_DATE_FORMAT);
    }

    private String formatMandateReference(String reference, LocalDate signatureDate) {
        if (signatureDate != null && reference != null && reference.contains("-Mandate-BugsPointer-directdebit-")) {
            return "Mandat BugsPointer - " + formatMandateDate(signatureDate);
        }
        if (reference == null || reference.trim().isEmpty()) {
            return "Mandat BugsPointer - " + formatMandateDate(signatureDate);
        }
        return reference;
    }

    private String maskIban(String iban) {
        String normalized = iban == null ? "" : iban.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        return groupByFour(maskMiddle(normalized, 4, 4));
    }

    private String maskBic(String bic) {
        String normalized = bic == null ? "" : bic.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        return maskMiddle(normalized, 4, 2);
    }

    private String maskMiddle(String value, int visibleStart, int visibleEnd) {
        if (value == null || value.isEmpty()) {
            return "--";
        }
        if (value.length() <= visibleStart + visibleEnd) {
            return value.charAt(0) + "****" + value.charAt(value.length() - 1);
        }
        return value.substring(0, visibleStart) + "****" + value.substring(value.length() - visibleEnd);
    }

    private String groupByFour(String value) {
        if (value == null || value.equals("--")) {
            return "--";
        }
        return value.replaceAll("(.{4})(?=.)", "$1 ");
    }

    public CustomerDTO getMetadata(CustomerDTO customer, String customerId) throws MollieException {

        CustomerResponse customerResponse = client.customers().getCustomer(customerId);

        customer.setAddress1(customerResponse.getMetadata().get("address1") != null
                ? (String) customerResponse.getMetadata().get("address1")
                : null);

        customer.setAddress2(customerResponse.getMetadata().get("address2") != null
                ? (String) customerResponse.getMetadata().get("address2")
                : null);

        customer.setCp(customerResponse.getMetadata().get("cp") != null
                ? (String) customerResponse.getMetadata().get("cp")
                : null);

        customer.setCity(customerResponse.getMetadata().get("city") != null
                ? (String) customerResponse.getMetadata().get("city")
                : null);

        customer.setCountry(customerResponse.getMetadata().get("country") != null
                ? (String) customerResponse.getMetadata().get("country")
                : null);

        return customer;

    }

    public CustomerDTO getBankAccount(CustomerDTO customer, String customerId) throws MollieException {

        List<MandateResponse> mandates = client.mandates().listMandates(customerId).getEmbedded().getMandates();

        if (mandates.size() > 0) {
            MandateResponse mandateResponse = client.mandates().getMandate(customerId, mandates.get(0).getId());
            Optional<String> iban = mandateResponse.getDetails().getConsumerAccount();
            Optional<String> bic = mandateResponse.getDetails().getConsumerBic();

            if (iban.isPresent() && bic.isPresent()) {
                customer.setIban(iban.get());
                customer.setBic(bic.get());
            }
        }

        return customer;

    }

    public boolean haveAndValidMandateWithIban(CustomerDTO customer) throws MollieException {

        Company company = companyRepository.findByPublicKey(customer.getPublicKey()).get();
        if (company != null) {
            List<MandateResponse> mandates = client.mandates().listMandates(company.getCustomer().getCustomerId()).getEmbedded().getMandates();

            if (mandates.size() > 0) {
                MandateResponse mandateResponse = client.mandates().getMandate(company.getCustomer().getCustomerId(), mandates.get(0).getId());
                if (mandateResponse.getStatus().equals(MandateStatus.VALID)) {
                    Optional<String> iban = mandateResponse.getDetails().getConsumerAccount();
                    Optional<String> bic = mandateResponse.getDetails().getConsumerBic();
                    if (iban.get().equals(customer.getIban()) && bic.get().equals(customer.getBic())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<MandateDTO> getMandateList(Company company) throws MollieException {

        List<MandateDTO> mandatesListDTO = new ArrayList<>();
        if (company != null && company.getCustomer() != null) {
            List<MandateResponse> mandates = client.mandates().listMandates(company.getCustomer().getCustomerId()).getEmbedded().getMandates();

            mandatesListDTO = mandates.stream()
                    .map(mandateResponse -> {
                        LocalDate dateSignature = mandateResponse.getSignatureDate();
                        LocalDate dateValid = dateSignature.plusYears(4);
                        Date date = Date.from(dateValid.atStartOfDay(ZoneId.systemDefault()).toInstant());

                        MandateDTO mandateDTO = new MandateDTO();
                        mandateDTO.setMandateId(mandateResponse.getId());
                        mandateDTO.setCustomerId(company.getCustomer().getCustomerId());
                        mandateDTO.setValidDate(Utility.dateFormator(date, "dd/MM/yyyy"));
                        mandateDTO.setStatus(String.valueOf(mandateResponse.getStatus()));
                        mandateDTO.setIban(String.valueOf(mandateResponse.getDetails().getConsumerAccount().orElse("")));
                        mandateDTO.setBic(String.valueOf(mandateResponse.getDetails().getConsumerBic().orElse("")));

                        if(mandateDTO.getMandateId() != null && mandateDTO.getCustomerId() != null){
                            return mandateDTO;
                        } else {
                             try {
                                throw new CustomExceptions.GetDeleteMandateException("Il manque le customerId ou mandateId pour supprimer le mandat");
                            } catch (CustomExceptions.GetDeleteMandateException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    })
                    .collect(Collectors.toList());
        }
        return mandatesListDTO;
    }

}
