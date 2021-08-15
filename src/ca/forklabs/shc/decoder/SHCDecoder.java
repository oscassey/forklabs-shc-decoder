package ca.forklabs.shc.decoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class SHCDecoder {

    // https://www.reddit.com/r/Quebec/comments/ndz2uz/how_the_covid_vaccination_qr_code_works_and_what/
    // https://github.com/dvci/health-cards-walkthrough/blob/main/SMART%20Health%20Cards.ipynb

    // https://smarthealth.cards/

    // https://github.com/fproulx/shc-covid19-decoder/blob/main/src/shc.js

    // https://developer.okta.com/blog/2018/10/31/jwts-with-java
    // https://developer.okta.com/blog/2018/10/16/token-auth-for-java

    // https://github.com/jwtk/jjwt

    public static void main(String... args) throws IOException {
        //var file = Path.of("2021-05-12 - Vaccin Pfizer 1ere dose.txt");
        //var file = Path.of("2021-07-22 - Vaccin Pfizer 2e dose.txt");
        var file = Path.of("2021-07-26 - Vaccin Pfizer 2e dose.txt");

        var content = Files.readString(file);
        content = content.trim();
        System.out.println(content);

        var rawPayload = content.substring(5);
        System.out.println(rawPayload);

        var rawPayloadLen = rawPayload.length();
        System.out.println("Initial content is " + content.length() + " characters long.");
        System.out.println("Raw payload is " + rawPayloadLen + " characters long.");
        if (rawPayloadLen % 2 != 0) {
            System.out.println("Raw payload array must be even, it is not!");
            return;
        }

        var chunksLen = rawPayloadLen / 2;
        var chunks = new String[chunksLen];
        for (var i = 0; i < chunksLen; i++) {
            var beginIndex = i * 2;
            var endIndex = beginIndex + 2;
            chunks[i] = rawPayload.substring(beginIndex, endIndex);
        }

        System.out.println(Arrays.toString(chunks));

        var bytes = new byte[chunksLen];
        for (var i = 0; i < chunksLen; i++) {
            var code = Integer.parseInt(chunks[i]);
            var betterCode = code + 45;
            bytes[i] = (byte) betterCode;
        }

        var jwt = new String(bytes);
        System.out.println(new String(bytes));

        // https://developer.okta.com/blog/2018/10/16/token-auth-for-java
        var sections = jwt.split("\\.");
        for (var section : sections) {
            System.out.println(section);
        }

        var header = sections[0];
        var payload = sections[1];
        var signature = sections[2];

        var base64Decoder = Base64.getUrlDecoder();

        System.out.println(new String(base64Decoder.decode(header)));
        System.out.println(new String(base64Decoder.decode(payload)));
        System.out.println(new String(base64Decoder.decode(signature)));

        var deflatedBytes = base64Decoder.decode(payload);

        var inflater = new Inflater(true);
        var is = new InflaterInputStream(new ByteArrayInputStream(deflatedBytes), inflater);
        var inflatedBytes = is.readAllBytes();

        System.out.println(Arrays.toString(inflatedBytes));

        var inflated = new String(inflatedBytes, Charset.forName("UTF-8"));
        System.out.println(inflated);
    }

}
