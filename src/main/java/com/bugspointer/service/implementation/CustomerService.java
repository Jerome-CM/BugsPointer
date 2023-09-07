package com.bugspointer.service.implementation;

import be.woutschoovaerts.mollie.Client;
import be.woutschoovaerts.mollie.ClientBuilder;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CustomerService implements ICustomer {

    private final CompanyRepository companyRepository;

    private final ModelMapper modelMapper;

    public CustomerService(CompanyRepository companyRepository, ModelMapper modelMapper) {
        this.companyRepository = companyRepository;
        this.modelMapper = modelMapper;
    }

    Client client = new ClientBuilder()
            .withApiKey("test_v6adWpq2Uke8cJbtkDvkqCeaUPwBv9")
            .build();

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

                    contentData.put("status", "OK");
                    contentData.put("reference", mandateResponse.getMandateReference());
                    contentData.put("dateSignature", String.valueOf(mandateResponse.getSignatureDate()));
                    contentData.put("dateExpiration", Utility.handlerDateForYear(String.valueOf(mandateResponse.getSignatureDate()), 4));
                    contentData.put("dateNextPayment", Utility.handlerDateForYear(String.valueOf(mandateResponse.getSignatureDate()), 1));
                    contentData.put("iban", mandateResponse.getDetails().getConsumerAccount().get());
                    contentData.put("bic", mandateResponse.getDetails().getConsumerBic().get());

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
