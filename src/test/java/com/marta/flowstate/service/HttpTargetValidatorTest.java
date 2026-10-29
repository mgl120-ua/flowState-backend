package com.marta.flowstate.service;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

class HttpTargetValidatorTest {

    @Test
    void internalAddressesAreDetected() throws UnknownHostException {
        for (String ip : new String[]{"127.0.0.1", "10.1.2.3", "172.16.0.1", "192.168.1.1", "169.254.169.254",
                "0.0.0.0", "100.64.0.1", "::1", "fd00::1", "fe80::1", "224.0.0.1"}) {
            assertTrue(HttpTargetValidator.isInternal(InetAddress.getByName(ip)), ip);
        }
    }

    @Test
    void publicAddressesAreAllowed() throws UnknownHostException {
        for (String ip : new String[]{"8.8.8.8", "1.1.1.1", "172.32.0.1", "100.128.0.1", "2606:4700:4700::1111"}) {
            assertFalse(HttpTargetValidator.isInternal(InetAddress.getByName(ip)), ip);
        }
    }

    @Test
    void localhostIsRejectedUnlessAllowed() {
        assertThrows(IllegalArgumentException.class, () -> new HttpTargetValidator(false).validate("http://localhost:8085/x"));
        assertDoesNotThrow(() -> new HttpTargetValidator(true).validate("http://localhost:8085/x"));
    }

    @Test
    void onlyHttpSchemesAreAccepted() {
        HttpTargetValidator validator = new HttpTargetValidator(true);
        assertThrows(IllegalArgumentException.class, () -> validator.parse("file:///etc/passwd"));
        assertThrows(IllegalArgumentException.class, () -> validator.parse("gopher://x"));
        assertThrows(IllegalArgumentException.class, () -> validator.parse("http:///sinhost"));
    }
}
