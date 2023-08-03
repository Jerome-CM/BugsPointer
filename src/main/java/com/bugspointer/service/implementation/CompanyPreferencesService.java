package com.bugspointer.service.implementation;

import com.bugspointer.dto.CompanyPreferenceDTO;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.CompanyPreferences;
import com.bugspointer.entity.enumLogger.Action;
import com.bugspointer.entity.enumLogger.What;
import com.bugspointer.repository.CompanyPreferencesRepository;
import com.bugspointer.service.ICompanyPreferences;
import com.bugspointer.utility.Utility;
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
        Optional<CompanyPreferences> preferencesOptional = preferencesRepository.findByCompany(company);
        CompanyPreferenceDTO dto;
        if (preferencesOptional.isPresent()){
            CompanyPreferences preferences = preferencesOptional.get();
            dto = modelMapper.map(preferences, CompanyPreferenceDTO.class);
        } else {
            dto = modelMapper.map(company, CompanyPreferenceDTO.class);
        }
        return dto;
    }


    public Response updatePreference(CompanyPreferenceDTO dto, String action) {
        Optional<CompanyPreferences> preferencesOptional = preferencesRepository.findByCompany_PublicKey(dto.getCompanyPublicKey());
        CompanyPreferences preferences;
        if (preferencesOptional.isPresent()){
            preferences = preferencesOptional.get();
            log.info("Preferences initial for company #{}, MNB:{}, MI:{}, MF:{}, SMSNB:{}, SMSI:{}, SMSF:{}", preferences.getCompany().getCompanyId(), preferences.isMailNewBug(), preferences.isMailInactivity(), preferences.isMailNewFeature(), preferences.isSmsNewBug(), preferences.isSmsInactivity(), preferences.isSmsNewFeature());
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
                return new Response(EnumStatus.ERROR, null, "Une erreur s'est produite");
            }

            try {
                CompanyPreferences savedPreferences = preferencesRepository.save(preferences);
                Utility.saveLog(savedPreferences.getCompany().getCompanyId(), Action.UPDATE, What.NOTIFICATION, null, null , null);
                log.info("Preferences after updated for company #{}, MNB:{}, MI:{}, MF:{}, SMSNB:{}, SMSI:{}, SMSF:{}", savedPreferences.getCompany().getCompanyId(), savedPreferences.isMailNewBug(), savedPreferences.isMailInactivity(), savedPreferences.isMailNewFeature(), savedPreferences.isSmsNewBug(), savedPreferences.isSmsInactivity(), savedPreferences.isSmsNewFeature());
                return new Response(EnumStatus.OK, null, "Préférences enregistrées avec succès");
            }
            catch (Exception e){
                log.error("Error to update preferences: {}", e.getMessage());
                return new Response(EnumStatus.ERROR, null, "Erreur lors de l'enregistrement");
            }
        }

        return new Response(EnumStatus.ERROR, null, "Error inconnue");
    }


}
