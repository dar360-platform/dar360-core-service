package ae.dar360.user.helper;

import org.junit.Assert;
import org.junit.Test;

public class AppStatusTest {

    @Test
    public void testInit() {
        Assert.assertNotNull(new AppStatus("code", "message"));
        Assert.assertNotNull(new AppStatus("code", "message", false));
        Assert.assertNotNull(new AppStatus("code", "message", false, new Object()));
        Assert.assertNotNull(AppStatus.ofOk("code", new Object()));
        Assert.assertNotNull(AppStatus.ofInternalServerError("code", new Object()));
    }
}