package com.bugspointer.service.implementation;

import com.bugspointer.dto.CompanyPreferenceDTO;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.CompanyPreferences;
import com.bugspointer.repository.CompanyPreferencesRepository;
import com.bugspointer.service.ICompanyPreferences;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class CompanyPreferencesService implements ICompanyPreferences {

    private final CompanyPreferencesRepository preferencesRepository;

    private final ModelMapper modelMapper;

    public CompanyPreferencesService(CompanyPreferencesRepository companyPreferencesRepository, ModelMapper modelMapper) {
        this.preferencesRepository = companyPreferencesRepository;
        this.modelMapper = modelMapper;
    }

    public CompanyPreferenceDTO getCompanyPreferenceDTO(Company company) {
        log.info("CompanyPreferenceDTO : company : {}", company);
        Optional<CompanyPreferences> preferencesOptional = preferencesRepository.findByCompany(company);
        CompanyPreferenceDTO dto;
        if (preferencesOptional.isPresent()){
            CompanyPreferences preferences = preferencesOptional.get();
            log.info("companyPreferences : {}", preferences);
            dto = modelMapper.map(preferences, CompanyPreferenceDTO.class);
        } else {
            dto = modelMapper.map(company, CompanyPreferenceDTO.class);
        }
        log.info("CompanyPreferenceDTO : dto : {}", dto);
        return dto;
    }


    public Response updatePreference(CompanyPreferenceDTO dto, String action) {
        log.info("updatePreference :");
        log.info("dto : {}", dto);

        Optional<CompanyPreferences> preferencesOptional = preferencesRepository.findByCompany_PublicKey(dto.getCompanyPublicKey());
        CompanyPreferences preferences;
        if (preferencesOptional.isPresent()){
            preferences = preferencesOptional.get();
            log.info("Preferences initial : {}", preferences);
            if (action.equals("updateMail")){
                preferences.setMailNewBug(dto.isMailNewBug());
                preferences.setMailInactivity(dto.isMailInactivity());
                preferences.setMailNewFeature(dto.isMailNewFeature());
            } else if (action.equals("updateSms")) {
                if (dto.getCompanyPhoneNumber().isEmpty()){
                    preferences.setSmsNewBug(false);
                    preferences.setSmsInactivity(false);
                    preferences.setSmsNewFeature(false);
                } else {
                    preferences.setSmsNewBug(dto.isSmsNewBug());
                    preferences.setSmsInactivity(dto.isSmsInactivity());
                    preferences.setSmsNewFeature(dto.isSmsNewFeature());
                }
            } else {
                return new Response(EnumStatus.ERROR, null, "Error to click on button");
            }
            log.info("Preferences at modify");

            try {
                CompanyPreferences savedPreferences = preferencesRepository.save(preferences);
                log.info("Preferences update : {}", savedPreferences);
                return new Response(EnumStatus.OK, null, "Preferences updated");
            }
            catch (Exception e){
                log.error("Error : {}", e.getMessage());
                return new Response(EnumStatus.ERROR, null, "Error in update");
            }
        }

        return new Response(EnumStatus.ERROR, null, "Error in the process");
    }


}
