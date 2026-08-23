package com.amentcity.nvr.auth.jwt;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final String secret = "enterprise-nvr-secret";

    public String createToken(String username){
        return Jwts.builder()
                .setSubject(username)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String getUsername(String token){
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
