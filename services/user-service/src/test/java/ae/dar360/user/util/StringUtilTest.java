package ae.dar360.user.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mockito.InjectMocks;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StringUtilTest {

    @InjectMocks
    private StringUtil util;

    @Test
    public void getInstance() {
        assertNotNull(util.getInstance());
    }

    @Test
    public void containString() {
        assertTrue(StringUtil.containString("a", List.of("a", "b", "c")));
        assertFalse(StringUtil.containString("d", List.of("a", "b", "c")));
    }

    @Test
    public void toUpperCaseNoSpace() {
        assertEquals("___", StringUtil.toUpperCaseNoSpace("   "));
        assertEquals("ABC_DEF___", StringUtil.toUpperCaseNoSpace("abc deF   "));
    }

    @Test
    public void convertNumberToStringFullCharacter() {
        assertEquals(StringUtils.EMPTY, StringUtil.convertNumberToStringFullCharacter(null, 1, 1));
        assertEquals(StringUtils.EMPTY, StringUtil.convertNumberToStringFullCharacter(1, null, 1));
        assertEquals("12", StringUtil.convertNumberToStringFullCharacter(2, 3, 10));
    }

    @Test
    public void validateEmail() {
        assertFalse(StringUtil.validateEmail(""));
        assertTrue(StringUtil.validateEmail("abc@def.com"));
    }

    @Test
    public void isContainLeastString() {
        assertFalse(StringUtil.isContainLeastString(""));
        assertTrue(StringUtil.isContainLeastString("a"));
    }

    public @Test
    void validatePhone() {
        assertTrue(StringUtil.validatePhone("+97112345678"));
        assertFalse(StringUtil.validatePhone("+97112345678901"));
    }

    @Test
    public void validPassword() {
        assertFalse(StringUtil.validPassword("ABCdef97112345678"));
    }
}