package com.t1f5.skib.auth.util;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.t1f5.skib.auth.exception.InvalidTokenException;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

@Component
public class JwtTokenProvider {

    private final String secretKey = "yourSecretKey";

    public String createToken(String identifier, String role) {
        Claims claims = Jwts.claims().setSubject(identifier);
        claims.put("role", role);

        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getIdentifier(String token) {
        return getClaims(token).getSubject();
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    private Claims getClaims(String token) {
        try {
            return Jwts
                .parserBuilder()
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

