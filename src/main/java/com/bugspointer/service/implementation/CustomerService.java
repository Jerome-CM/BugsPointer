package com.bugspointer.service.implementation;

import com.bugspointer.dto.CustomerDTO;
import com.bugspointer.entity.Customer;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.service.ICustomer;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomerService implements ICustomer {

    private final CompanyRepository companyRepository;

    private final ModelMapper modelMapper;

    public CustomerService(CompanyRepository companyRepository, ModelMapper modelMapper) {
        this.companyRepository = companyRepository;
        this.modelMapper = modelMapper;
    }

    public CustomerDTO getCustomerDTO(Customer customer) {
        CustomerDTO dto;
        dto = modelMapper.map(customer, CustomerDTO.class);
        return dto;
    }
}
