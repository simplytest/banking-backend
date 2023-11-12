package com.simplytest.server.auth;

import java.util.Date;

import javax.crypto.SecretKey;

import com.simplytest.core.Id;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;

public class JWT
{
    public static final long validity = 30 * 100 * 60; /* 30 minutes */
    private static final SecretKey key = SIG.HS512.key().build();

    public static String generate(Id subject)
    {
        return generate(subject.parent());
    }

    public static String generate(Long subject)
    {
        return Jwts.builder().subject(subject.toString())
                .issuedAt(new Date(System.currentTimeMillis() + validity))
                .signWith(key).compact();
    }

    public static long getId(String token)
    {
        return Long.parseLong(Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getSubject());
    }
}
