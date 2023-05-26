import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.nio.file.attribute.FileTime;
import java.io.*;
import java.security.*;
import java.time.Instant;

public class CodeMaker {

    //generate the QR code
    public static BufferedImage generateQRCodeImage(String barcodeText) throws Exception {
        FileTime QRcode = null;
        ByteArrayOutputStream stream = QRcode
                .from(Instant.parse(barcodeText))
                .withSize(250,250)
                .stream();
        ByteArrayInputStream bis = new ByteArrayInputStream(stream.toByteArray());

        return ImageIO.read(bis);
    }

    //generate signature
    if (args.length != 1) {
        System.out.println("Usage: GenSig nameOfFileToSign");
    }
    else try {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");

        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(1024, random);

        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey priv = pair.getPrivate();
        PublicKey pub = pair.getPublic();

        Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");
        dsa.initSign(priv);

        FileInputStream fis = new FileInputStream(args[0]);
        BufferedInputStream bufin = new BufferedInputStream(fis);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = bufin.read(buffer)) >= 0) {
            dsa.update(buffer, 0, len);
        };
        bufin.close();

        byte[] realSig = dsa.sign();

    } catch (Exception e) {
        System.err.println("Caught exception " + e.toString());
        }
}
