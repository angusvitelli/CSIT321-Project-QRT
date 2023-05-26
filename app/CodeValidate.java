import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CodeValidate {

    public CodeValidate() throws IOException {
    }

    //get and read the public key from the PEM file
    public static RSAPublicKey readPublicKey(File file) throws Exception {
        String pubKey = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());

        String pubKeyPEM = pubKey
                .replaceAll(System.lineSeparator(), "");

        byte[] encoded = Base64.getDecoder().decode(pubKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);

        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    Path binFile = ...; //decide path later

    //read the binary file containing the digital signature
    try (InputStream in = Files.newInputStream(binFile);
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

    String line = reader.readLine();
}
