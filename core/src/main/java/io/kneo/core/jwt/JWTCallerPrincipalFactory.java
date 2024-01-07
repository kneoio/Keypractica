package io.kneo.core.jwt;

import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@ApplicationScoped
@Alternative
@Priority(1)
public class JWTCallerPrincipalFactory extends io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory {
    @Override
    public JWTCallerPrincipal parse(String token, JWTAuthContextInfo authContextInfo) throws ParseException {
        try {
            String json = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
            return new DefaultJWTCallerPrincipal(JwtClaims.parse(json));
        } catch (InvalidJwtException | ArrayIndexOutOfBoundsException ex) {
            throw new ParseException(ex.getMessage());
        }
    }
}
