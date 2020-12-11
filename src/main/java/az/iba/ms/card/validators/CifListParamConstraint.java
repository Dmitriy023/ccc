package az.iba.ms.card.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = CifListSizeValidator.class)
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CifListParamConstraint {

    /**
     * Error message to display.
     *
     * @return
     */
    String message() default "Exceeds max allowed cif list size";

    /**
     * Error grouping.
     *
     * @return
     */
    Class<?>[] groups() default {};

    /**
     * Metadata information.
     *
     * @return
     */
    Class<? extends Payload>[] payload() default {};
}
