package me.ayunami2000.psbot;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.apache.commons.codec.binary.Hex;

public class SteganographyUtil {
    private static final String key = "76b8d31786ab34e4f8e53b01ee9c5743f6bd73e50a2ed5d6db8aeab2c266ecd2b18bc35a53acca80d5806dbc6049e1a31de33c8a12dd7ad54ecf3b0d855afc1e";
    private static final Map<String, String> colorToHex = Map.ofEntries(new Map.Entry[]{Map.entry("black", "0"), Map.entry("dark_blue", "1"), Map.entry("dark_green", "2"), Map.entry("dark_aqua", "3"), Map.entry("dark_red", "4"), Map.entry("dark_purple", "5"), Map.entry("gold", "6"), Map.entry("gray", "7"), Map.entry("dark_gray", "8"), Map.entry("blue", "9"), Map.entry("green", "a"), Map.entry("aqua", "b"), Map.entry("red", "c"), Map.entry("light_purple", "d"), Map.entry("yellow", "e"), Map.entry("white", "f")});
    //private static Map<String, String> alreadyDecrypted = new HashMap();

    public static String decryptMessage(Text input) {
        StringBuilder hex = new StringBuilder();
        String aChar;
        if (input.getString().contains("⎮")) {
            if (input.getString().contains("§")) {
                String[] chars = input.getString().split("");
                String[] var3 = chars;
                int var4 = chars.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    aChar = var3[var5];
                    if (!Objects.equals(aChar, "⎮") && !Objects.equals(aChar, "§")) {
                        hex.append(aChar);
                    }
                }
            } else {
                try {
                    List<Text> list = input.getWithStyle(Style.EMPTY);
                    Iterator var16 = list.iterator();

                    while(var16.hasNext()) {
                        Text text = (Text)var16.next();
                        String[] chars = text.getString().split("");
                        String[] var22 = chars;
                        int var7 = chars.length;

                        for(int var8 = 0; var8 < var7; ++var8) {
                            aChar = var22[var8];
                            if (Objects.equals(aChar, "⎮")) {
                                String name = text.getStyle().getColor().getName();
                                String hexValue = (String)colorToHex.get(name == null ? "white" : name);
                                hex.append(hexValue);
                            }
                        }
                    }
                } catch (Exception var13) {
                }
            }
        }

        try {
            byte[] iv = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(key.toCharArray(), "DEVIOUSMODISAWESOME".getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(2, secretKey, ivspec);
            aChar = new String(cipher.doFinal(Hex.decodeHex(hex.toString())));
            //alreadyDecrypted.put(input.getString(), aChar);
            return aChar;
        } catch (Exception var12) {
            return "Unable to decrypt";
        }
    }

    public static String encrypt(String input) {
        try {
            byte[] iv = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(key.toCharArray(), "DEVIOUSMODISAWESOME".getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(1, secretKey, ivspec);
            char[] hex = Hex.encodeHex(cipher.doFinal(input.getBytes(StandardCharsets.UTF_8)));
            StringBuilder toSay = new StringBuilder();
            char[] var10 = hex;
            int var11 = hex.length;

            for(int var12 = 0; var12 < var11; ++var12) {
                char s = var10[var12];
                toSay.append(String.format("&%s⎮", s));
            }

            return toSay.toString();
        } catch (Exception var14) {
            return "Unable to encrypt";
        }
    }
}