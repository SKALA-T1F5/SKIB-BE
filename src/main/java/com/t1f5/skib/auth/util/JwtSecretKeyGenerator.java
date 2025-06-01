package com.t1f5.skib.auth.util;

import java.security.SecureRandom;
import java.util.Base64;

public class JwtSecretKeyGenerator {
  public static void main(String[] args) {
    byte[] keyBytes = new byte[64]; // 512-bit key
    new SecureRandom().nextBytes(keyBytes);
    String secretKey = Base64.getEncoder().encodeToString(keyBytes);
    System.out.println("Your JWT Secret Key:\n" + secretKey);
  }
}
