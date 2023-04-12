package com.bugspointer.service;

import com.bugspointer.entity.Company;

public interface ICompany {

    public Company getCompanyByMail(String mail);
}
