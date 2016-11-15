package cn.alphabets.light.security;

import org.junit.Assert;
import org.junit.Test;

/**
 * Config Test
 * Created by lilin on 2016/11/3.
 */
public class CryptoTest {

    @Test
    public void testSha256() throws Exception {

        String result = Crypto.sha256("1qaz2wsx", "light");
        Assert.assertEquals("1f7f77b31ee95f1ac079b9f99f77684e7c9b900ba9cc4ea8d94c6d9d0c49c8ea", result);
    }

}
