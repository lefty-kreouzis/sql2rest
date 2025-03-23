package gr.rtfm.sql2rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import gr.rtfm.sql2rest.utils.AESUtils;
import gr.rtfm.sql2rest.utils.KeystoreUtils;

@SpringBootTest
class Sql2restApplicationTests {

	@Autowired
	KeystoreUtils keystoreUtils;

	@Autowired
	AESUtils aesUtils;

	@Test
	void contextLoads() {
	}

	@Test
	void testPasswd()
	{
		String passwd = keystoreUtils.getPassword("slm");
		System.out.println("Password: " + passwd);
	}


	@Test
	void testAES()
	{
		String passwd = "test";
		String encrypted = aesUtils.encrypt(passwd);
		System.out.println("Encrypted: " + encrypted);
		String decrypted = aesUtils.decrypt(encrypted);
		System.out.println("Decrypted: " + decrypted);
		Assert.isTrue(passwd.equals(decrypted), "Password is not the same");
	}

}
