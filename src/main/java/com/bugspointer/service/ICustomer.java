package com.bugspointer.service;

import com.bugspointer.dto.CustomerDTO;
import com.bugspointer.entity.Customer;

public interface ICustomer {

    CustomerDTO getCustomerDTO(Customer customer);
}
