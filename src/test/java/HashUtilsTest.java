import me.theentropyshard.teslauncher.utils.HashUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class HashUtilsTest {
    @Test
    public void sha1_test_1() throws IOException {
        String sha1 = HashUtils.sha1(Paths.get("src", "test", "resources", "hash_data_1.txt"));
        Assertions.assertEquals(sha1, "63ca849ac64fad6ab635727971529bc9a1c193aa");
    }

    @Test
    public void sha1_test_2() throws IOException {
        String sha1 = HashUtils.sha1(Paths.get("src", "test", "resources", "hash_data_2.txt"));
        Assertions.assertEquals(sha1, "33cc10c5cc7f9f64c2ebfacf77054df09d91eea4");
    }

    @Test
    public void sha1_test_3() throws IOException {
        String sha1 = HashUtils.sha1(Paths.get("src", "test", "resources", "hash_data_3.txt"));
        Assertions.assertEquals(sha1, "8dbbd7e82ccacec0c062b23061bc6223ab192333");
    }
}
