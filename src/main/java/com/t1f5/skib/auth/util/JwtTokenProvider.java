package com.t1f5.skib.auth.util;

import com.t1f5.skib.auth.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  @Value("${jwt.secret-key}")
  private String secretKey;

  public String createToken(String identifier, String role) {
    Claims claims = Jwts.claims().setSubject(identifier);
    claims.put("role", role);

    Key key = Keys.hmacShaKeyFor(secretKey.getBytes());

    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000L)) // 2일
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public String getIdentifier(String token) {
    return getClaims(token).getSubject();
  }

  public String getRole(String token) {
    return getClaims(token).get("role", String.class);
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
          .build()
          .parseClaimsJws(token.replace("Bearer ", ""));
      return true;
    } catch (ExpiredJwtException e) {
      throw new InvalidTokenException("Token has expired");
    } catch (JwtException | IllegalArgumentException e) {
      throw new InvalidTokenException("Invalid JWT token");
    }
  }

  private Claims getClaims(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
          .build()
          .parseClaimsJws(token.replace("Bearer ", ""))
          .getBody();
    } catch (ExpiredJwtException e) {
      throw new InvalidTokenException("Token has expired");
    } catch (JwtException | IllegalArgumentException e) {
      throw new InvalidTokenException("Invalid JWT token");
    }
  }
}
