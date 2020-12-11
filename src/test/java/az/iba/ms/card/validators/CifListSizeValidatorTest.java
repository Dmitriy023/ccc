package az.iba.ms.card.validators;

import static org.assertj.core.api.Assertions.assertThat;

import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

class CifListSizeValidatorTest {

    private CifListSizeValidator cifListSizeValidator = new CifListSizeValidator();

    @Mock
    private ConstraintValidatorContext ctx;

    @Test
    public void shouldReturnFalse_WhenListExceedsMaxSize() {

        ReflectionTestUtils.setField(cifListSizeValidator, "maxCifListSize", 2);

        boolean val = cifListSizeValidator.isValid("1,2,3,4", ctx);

        assertThat(val).isFalse();
    }

    @Test
    public void shouldReturnTrue_WhenListDoesntExceedMaxSize() {

        ReflectionTestUtils.setField(cifListSizeValidator, "maxCifListSize", 5);

        boolean val = cifListSizeValidator.isValid("1,2,3,4", ctx);

        assertThat(val).isTrue();

        val = cifListSizeValidator.isValid("1,2,3,4,5", ctx);

        assertThat(val).isTrue();
    }
}
