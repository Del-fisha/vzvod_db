package com.company.vzvod.validation;

import com.company.vzvod.service.VehicleStateNumberService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class VehicleStateNumberValidator implements ConstraintValidator<ValidVehicleStateNumber, String> {

    private final VehicleStateNumberService vehicleStateNumberService;

    public VehicleStateNumberValidator(VehicleStateNumberService vehicleStateNumberService) {
        this.vehicleStateNumberService = vehicleStateNumberService;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // let NotBlank/NotEmpty handle requiredness if needed
        }
        return vehicleStateNumberService.isValid(value);
    }
}

