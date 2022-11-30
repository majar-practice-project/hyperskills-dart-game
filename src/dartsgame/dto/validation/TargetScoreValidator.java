package dartsgame.dto.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TargetScoreValidator implements ConstraintValidator<TargetScoreConstraint, Integer> {
    @Override
    public void initialize(TargetScoreConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value==101 || value==301 || value==501;
    }
}
