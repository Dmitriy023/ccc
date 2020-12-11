package az.iba.ms.card.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

public class CifListSizeValidator implements ConstraintValidator<CifListParamConstraint, String> {

    @Value("${validation.maxCifListSize}")
    private int maxCifListSize;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value.split(",").length <= maxCifListSize;
    }
}
