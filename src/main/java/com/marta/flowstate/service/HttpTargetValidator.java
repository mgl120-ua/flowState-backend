package com.marta.flowstate.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * Protección SSRF: una acción HTTP no puede llamar a la red interna del servidor
 * (loopback, redes privadas, link-local como 169.254.169.254, multicast...).
 * Se puede desactivar con flowstate.http.allow-private-hosts=true para pruebas locales.
 */
@Component
public class HttpTargetValidator {

    private final boolean allowPrivateHosts;

    public HttpTargetValidator(@Value("${flowstate.http.allow-private-hosts:false}") boolean allowPrivateHosts) {
        this.allowPrivateHosts = allowPrivateHosts;
    }

    /** Valida esquema y host, resolviendo el DNS. Lanza IllegalArgumentException si no se permite. */
    public URI validate(String url) {
        URI uri = parse(url);
        if (allowPrivateHosts) return uri;
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(uri.getHost());
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("No se puede resolver el host " + uri.getHost());
        }
        for (InetAddress address : addresses) {
            if (isInternal(address)) {
                throw new IllegalArgumentException("No se permiten llamadas a direcciones internas: " + uri.getHost());
            }
        }
        return uri;
    }

    /** Solo comprueba la forma de la URL (http/https y host), sin resolver el DNS. */
    public URI parse(String url) {
        URI uri;
        try {
            uri = URI.create(url.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("URL no válida: " + url);
        }
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("Solo se permiten URLs http o https");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("La URL no tiene host: " + url);
        }
        return uri;
    }

    static boolean isInternal(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return true;
        }
        byte[] bytes = address.getAddress();
        if (address instanceof Inet6Address) {
            return (bytes[0] & 0xfe) == 0xfc; //fc00::/7, direcciones locales únicas
        }
        int first = bytes[0] & 0xff;
        int second = bytes[1] & 0xff;
        return first == 0 || (first == 100 && second >= 64 && second <= 127); //0.0.0.0/8 y 100.64.0.0/10 (CGNAT)
    }
}
