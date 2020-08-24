package com.techpassel.servinja.util;

import com.techpassel.servinja.model.AuthUserDetails;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

@Component
public class JwtUtil implements Serializable {
    @Value("${techpassel.auth.jwt_secretkey}")
    private String jwtSecret;

    @Value("${techpassel.auth.jwt_expiration}")
    private int jwtExpirationTime;

    //Function to generate token
    public String generateToken(AuthUserDetails userDetails){
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    //Actual implementation of generateToken function
    private String createToken(Map<String, Object> claims, String subject){
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationTime))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    //Common function to retrive any particular data from token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    //Function to retrieve all information from token
    private Claims getAllClaimsFromToken(String token) {
        try{
            return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
            //Handle exceptions as per your requirement.
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired ");
        } catch (SignatureException e) {
            System.out.println("Invalid Token");
        } catch(Exception e){
            System.out.println("Some other exception in JWT parsing ");
        }
        return null;
    }

    //To validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    //To extract the username from token
    public String extractUsername(String token){
        try{
            return extractClaim(token, Claims::getSubject);
        } catch(NullPointerException e) {
            return null;
        }
    }

    //Check if the token has expired
    private Boolean isTokenExpired(String token) {
        final Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    //To extract the expiration time of token
    private Date extractExpiration(String token) {
        try{
            return extractClaim(token, Claims::getExpiration);
        } catch(NullPointerException e) {
            return null;
        }
    }
}
