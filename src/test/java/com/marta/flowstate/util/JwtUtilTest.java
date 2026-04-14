package com.marta.flowstate.util;

import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.model.Rol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private final String secret = "01234567890123456789012345678901";
    private final JwtUtil jwtUtil = new JwtUtil(secret);

    @Test
    void generateTokenExtractsUsernameAndValidates() {
        AppUser user = new AppUser();
        user.setEmail("test@example.com");
        Company company = new Company();
        company.setId(1L);
        user.setCompany(company);
        Rol rol = new Rol();
        rol.setName("ADMIN");
        user.setRol(rol);

        String token = jwtUtil.generateToken(user);

        assertNotNull(token);
        assertEquals("test@example.com", jwtUtil.extractUsername(token));
        assertTrue(jwtUtil.isTokenValid(token, "test@example.com"));
        assertFalse(jwtUtil.isTokenValid(token, "other@example.com"));
    }

    @Test
    void invalidTokenReturnsFalse() {
        assertFalse(jwtUtil.isTokenValid("invalid.token.value", "test@example.com"));
    }
}
