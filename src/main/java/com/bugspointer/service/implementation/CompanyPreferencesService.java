package com.bugspointer.service.implementation;

import com.bugspointer.dto.CompanyPreferenceDTO;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.dto.WidgetConfigDTO;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.CompanyPreferences;
import com.bugspointer.entity.EnumPlan;
import com.bugspointer.entity.enumLogger.Action;
import com.bugspointer.entity.enumLogger.What;
import com.bugspointer.repository.CompanyRepository;
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

    private final CompanyRepository companyRepository;

    private final ModelMapper modelMapper;

    public CompanyPreferencesService(CompanyPreferencesRepository companyPreferencesRepository, CompanyRepository companyRepository, ModelMapper modelMapper) {
        this.preferencesRepository = companyPreferencesRepository;
        this.companyRepository = companyRepository;
        this.modelMapper = modelMapper;
    }

    public CompanyPreferenceDTO getCompanyPreferenceDTO(Company company) {
        CompanyPreferences preferences = getOrCreatePreferences(company);
        CompanyPreferenceDTO dto = modelMapper.map(preferences, CompanyPreferenceDTO.class);
        dto.setCompanyPublicKey(company.getPublicKey());
        dto.setCompanyPhoneNumber(company.getPhoneNumber());
        dto.setWidgetPrimaryColor(safeColor(preferences.getWidgetPrimaryColor()));
        dto.setWidgetModalBackgroundColor(safeColor(preferences.getWidgetModalBackgroundColor(), "#FFFFFF"));
        dto.setWidgetModalTextColor(safeColor(preferences.getWidgetModalTextColor(), "#24233D"));
        dto.setWidgetTitleColor(safeColor(preferences.getWidgetTitleColor(), "#24233D"));
        dto.setWidgetLinkTextColor(safeColor(preferences.getWidgetLinkTextColor()));
        dto.setWidgetLinkUnderline(preferences.isWidgetLinkUnderline());
        dto.setWidgetButtonText(safeText(preferences.getWidgetButtonText(), "Signaler un bug", 60));
        dto.setWidgetButtonStyle(safeButtonStyle(preferences.getWidgetButtonStyle()));
        dto.setWidgetButtonSize(safeButtonSize(preferences.getWidgetButtonSize()));
        dto.setWidgetTitle(safeText(preferences.getWidgetTitle(), "Signaler un nouveau bug", 80));
        dto.setWidgetDescriptionLabel(safeText(preferences.getWidgetDescriptionLabel(), "Description du bug", 80));
        dto.setWidgetPosition(safePosition(preferences.getWidgetPosition()));
        dto.setWidgetMarginX(safeMargin(preferences.getWidgetMarginX()));
        dto.setWidgetMarginY(safeMargin(preferences.getWidgetMarginY()));
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
                if (dto.getCompanyPhoneNumber() == null || dto.getCompanyPhoneNumber().isEmpty()){
                    preferences.setSmsNewBug(false);
                    preferences.setSmsInactivity(false);
                    preferences.setSmsNewFeature(false);
                } else {
                    preferences.setSmsNewBug(dto.isSmsNewBug());
                    preferences.setSmsInactivity(dto.isSmsInactivity());
                    preferences.setSmsNewFeature(dto.isSmsNewFeature());
                }
            } else if (action.equals("updateWidget")) {
                preferences.setWidgetPrimaryColor(safeColor(dto.getWidgetPrimaryColor()));
                preferences.setWidgetModalBackgroundColor(safeColor(dto.getWidgetModalBackgroundColor(), "#FFFFFF"));
                preferences.setWidgetModalTextColor(safeColor(dto.getWidgetModalTextColor(), "#24233D"));
                preferences.setWidgetTitleColor(safeColor(dto.getWidgetTitleColor(), "#24233D"));
                preferences.setWidgetLinkTextColor(safeColor(dto.getWidgetLinkTextColor()));
                preferences.setWidgetLinkUnderline(dto.isWidgetLinkUnderline());
                preferences.setWidgetButtonText(safeText(dto.getWidgetButtonText(), "Signaler un bug", 60));
                preferences.setWidgetButtonStyle(safeButtonStyle(dto.getWidgetButtonStyle()));
                preferences.setWidgetButtonSize(safeButtonSize(dto.getWidgetButtonSize()));
                preferences.setWidgetTitle(safeText(dto.getWidgetTitle(), "Signaler un nouveau bug", 80));
                preferences.setWidgetDescriptionLabel(safeText(dto.getWidgetDescriptionLabel(), "Description du bug", 80));
                preferences.setWidgetPosition(safePosition(dto.getWidgetPosition()));
                preferences.setWidgetMarginX(safeMargin(dto.getWidgetMarginX()));
                preferences.setWidgetMarginY(safeMargin(dto.getWidgetMarginY()));
            } else if (action.equals("updateWidgetFree")) {
                preferences.setWidgetPrimaryColor(safeColor(dto.getWidgetPrimaryColor()));
                preferences.setWidgetLinkTextColor(safeColor(dto.getWidgetLinkTextColor()));
                preferences.setWidgetLinkUnderline(dto.isWidgetLinkUnderline());
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

    public WidgetConfigDTO getWidgetConfigDTO(String publicKey) {
        Company company = companyRepository.findByPublicKey(publicKey).orElse(null);
        WidgetConfigDTO dto = new WidgetConfigDTO();
        dto.setPublicKey(publicKey);
        if (company == null) {
            return dto;
        }

        CompanyPreferences preferences = getOrCreatePreferences(company);
        dto.setTargetPlan(company.getPlan() != EnumPlan.FREE);
        dto.setPrimaryColor(safeColor(preferences.getWidgetPrimaryColor()));
        dto.setLinkTextColor(safeColor(preferences.getWidgetLinkTextColor()));
        dto.setLinkUnderline(preferences.isWidgetLinkUnderline());
        dto.setButtonStyle("button");
        if (company.getPlan() == EnumPlan.FREE) {
            return dto;
        }

        dto.setModalBackgroundColor(safeColor(preferences.getWidgetModalBackgroundColor(), "#FFFFFF"));
        dto.setModalTextColor(safeColor(preferences.getWidgetModalTextColor(), "#24233D"));
        dto.setTitleColor(safeColor(preferences.getWidgetTitleColor(), "#24233D"));
        dto.setButtonText(safeText(preferences.getWidgetButtonText(), "Signaler un bug", 60));
        dto.setButtonSize(safeButtonSize(preferences.getWidgetButtonSize()));
        dto.setTitle(safeText(preferences.getWidgetTitle(), "Signaler un nouveau bug", 80));
        dto.setDescriptionLabel(safeText(preferences.getWidgetDescriptionLabel(), "Description du bug", 80));
        dto.setPosition(safePosition(preferences.getWidgetPosition()));
        dto.setMarginX(safeMargin(preferences.getWidgetMarginX()));
        dto.setMarginY(safeMargin(preferences.getWidgetMarginY()));
        return dto;
    }

    private CompanyPreferences getOrCreatePreferences(Company company) {
        Optional<CompanyPreferences> preferencesOptional = preferencesRepository.findByCompany(company);
        if (preferencesOptional.isPresent()) {
            return preferencesOptional.get();
        }

        CompanyPreferences preferences = new CompanyPreferences();
        preferences.setCompany(company);
        preferences.setMailNewBug(true);
        preferences.setMailInactivity(true);
        return preferencesRepository.save(preferences);
    }

    private String safeColor(String color) {
        return safeColor(color, "#27215F");
    }

    private String safeColor(String color, String fallback) {
        if (color != null && color.matches("^#[0-9a-fA-F]{6}$")) {
            return color;
        }
        return fallback;
    }

    private String safeText(String value, String fallback, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }

    private String safePosition(String position) {
        if ("bottom-left".equals(position) || "top-right".equals(position) || "top-left".equals(position)) {
            return position;
        }
        return "bottom-right";
    }

    private String safeButtonStyle(String style) {
        if ("custom".equals(style)) {
            return "custom";
        }
        return "button";
    }

    private Integer safeMargin(Integer margin) {
        if (margin == null) {
            return 15;
        }
        if (margin < 0) {
            return 0;
        }
        return Math.min(margin, 120);
    }

    private Integer safeButtonSize(Integer size) {
        if (size == null) {
            return 56;
        }
        if (size < 44) {
            return 44;
        }
        return Math.min(size, 96);
    }

}
