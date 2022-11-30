package dartsgame.dto.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TargetScoreValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface TargetScoreConstraint {
    String message() default "Wrong target score!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
