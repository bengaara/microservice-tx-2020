package net.tospay.transaction;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import net.tospay.transaction.util.CipherUtility;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;


//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = Application.class)
@RunWith(SpringRunner.class)
@SpringBootTest
public class CipherUtilityTest {

  @Autowired
  private CipherUtility cipherUtility;

  @Test
  public void cipherTest() {

    String plain = "Happy day!";

    // Initialization of key pair for encryption and decryption.
    KeyPair keyPair = cipherUtility.getKeyPair();

    try {
      // Get public key from the key pair.
      PublicKey pubKey = keyPair.getPublic();

      // Get private key from the key pair.
      PrivateKey privKey = keyPair.getPrivate();

      // Try to encode public key as a string.
      String pubKeyStr = cipherUtility.encodeKey(pubKey);
      // Assertion of 'pubKey' and the public key decoded by 'pubKeyStr'.
      Assert.assertEquals(pubKey, cipherUtility.decodePublicKey(pubKeyStr));

      // Try to encode private key as a string.
      String privKeyStr = cipherUtility.encodeKey(privKey);
      // Assertion of 'privKey' and the private key decoded by 'privKeyStr'.
      Assert.assertEquals(privKey, cipherUtility.decodePrivateKey(privKeyStr));



      // Encrypt plain as a cipher.
      String cipherContent = cipherUtility.encrypt(plain, pubKey);
      // Decrypt cipher to original plain.
      String decryptResult = cipherUtility.decrypt(cipherContent, privKey);
      // Assertion of 'plain' and 'decryptResult'.
      Assert.assertEquals(plain, decryptResult);


      PublicKey pubKey1= cipherUtility.decodePublicKey( "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAt7r1HFULxCHWIqRyZtsvrr8Njw2r0MRo9OO/s2Hwv6zo8Jo50TtVvnCdPoDdjk9c/Zet30/9NB8CLHUQdRnVyJfbk8OA1ZM/6AfH0RLIVJDAlIP7rv/hIZqkZME9IAuWXNq5jzKwURSZ8R+i4ldFVG6aD9Clz51VXYQDwNw4GvHckg27stMisYvNdsslOkcCS59e1Iw/gTZaoYpdB8FHdZsoymqcR48wfnT3pj93iWJIx+q7BY7DTEnJwLE8GWmHNynJbylymoz1O13BTrehJ/RRlGLfdr6wk6X8XyeJNljQzlmXqUgZ563gHxMMQtojofrjfSETxxVtDnSm5u0wvwIDAQAB");
      PrivateKey privKey1 = cipherUtility.decodePrivateKey("MIIEpgIBAAKCAQEAyrPMXe0hxdtcsd+XlVGHlHrXkOuTT1Kg59OY6BCYYMOIJxN6Nzc9B5hi7EG2y8yt51OZcUkDIMSA39vE7nZji4ECGpwZ2JSLmbn+i10P9ankZ+ifX+cfa7EJl62x/RGi/udDdz2Kr4ZSNobwHtCJZ1NMXTaXTJo13GBu5n5RAuCXK/Gc1H4FOZKO1ndSTGSnDUIJl6ogX46Dos17XpFqvQd2BHYlvVh+HuUqBvQ2WLxHqexaKivKoa8bIr9G0pc3Z+j9eYGp8TOU1JkuIqWGLN26wgoK418phuhkfGfdeY6LVLIOjHkE0ASwYko/3t9FW3f/PA7/o2XjS26eHOR5tQIDAQABAoIBAQCF1oGx4sjR4y0pJQDpTpO+rf13apQxY9VqPIRRdeOmwHQ9mNaxbn+VbECkzh7sIZERlFlUO2kdUaHmLS2yZekES4IgH8HRBwPF1NEFI8VN54cwNKBGqkxMxvAWflFvTx3YQMqDCNdxXbca2a3iKr8OwilBwrHTml7Fy8Zt2imTQ5p9y2+oYe6rhSFSaztWY2zh0jb3Gx7Kx26GLf4qpyfJsbSVx/ANFrknWnlJZt9U/GJbOOUBcPgzo8K/Fz5Gg5wmuAxUHG46rjUscZWvmk1GrSbyebdz47/wYghzTlEJnf8FABWyLl8wsv3r1WRdX6mXhWUl/mcUbB91BSeDMehhAoGBAO0VSco4klaHNENzSXR09cKHwH60UYlYyjj8hd42wp3uAvooi2sSYOXs5HkxnY6SD43GAfqIZ1+JkDcCGKi4EpqrIGq0f6HnM1kK5uGfxd2eYIvNfIrdSmhyqBI3VbhU7dCVEZ8zPdFDzQcS/jU+LyqDTqDi4UuwyEbvtJj3Ol6TAoGBANrgPXF35OuHazvdx8rOTlvOQVxC/BM6mh06cEpr4c/JC5fhbXagg8rA6dPJRM7k2SvoNAPxVqddDg09ObrwnwF8cG34fzOWKWkErNfvHTQ3oHUNEKLTCr7Ia1aLjOO8Ri+t+nw1YSaVz+zm3uaYJRtqypbWrDq4/3aVWHL+nCuXAoGBAKNCh5/TuSIQ1PKlzy185UfiR7J45bUZCZiYfG77cqVio3Y0uV0wOL3clbDHc+Ibvs44TmCB0f2+MytOLEDzEQylTxtr1IBfP8LPeRJh/dWRHJbpGBxgKr8mRYrVxBe9lE6QeKQSmz0h3WxDrf5oJf62kUzFL7P5ttDyfIDhrrY1AoGBALod1CGYgeCF1nmhrTKXG6qVSJ7blt4DBumzt6iQ9ANsQZ904U/uTiMqx6p/izMjBkh/uztfDQBj8H7xgRWlZpAs8K2SkRecqLnUb+VuPrcYVikyYuMR8iEo6508LSG6BzSkDEtyWJom6hfYedI7rdISjRKWmP2WMiTz+bJPLx0hAoGBAMxN4uRDwop4A80v7/qJBO0RI7E53lwFUHj8YyJXKI5Kj/L8OaGXP6truTsmEup5dMP5gAmtIb6cgdWikvQkTLU5LitN5LJiF0feay+msLOmWOJ145cQYyrDbkCuyUoY4wltefwP+BxkRXaONFWBmGs+ILZ0Kvx9rwNudCWhZHjN" );

      String cipherContent1 = cipherUtility.encrypt(plain, pubKey1);
      // Decrypt cipher to original plain.
      String decryptResult1 = cipherUtility.decrypt(cipherContent1, privKey1);
      // Assertion of 'plain' and 'decryptResult'.
      Assert.assertEquals(plain, decryptResult1);




    } catch (InvalidKeyException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchProviderException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchPaddingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalBlockSizeException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (BadPaddingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InvalidKeySpecException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}