package com.bugspointer.service;

import com.bugspointer.dto.*;
import com.bugspointer.entity.Company;

public interface ICompanyPreferences {

    CompanyPreferenceDTO getCompanyPreferenceDTO(Company company);
    Response updatePreference(CompanyPreferenceDTO dto, String action);
}
