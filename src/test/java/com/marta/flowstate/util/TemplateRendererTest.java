package com.marta.flowstate.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TemplateRendererTest {

    private final TemplateRenderer renderer = new TemplateRenderer(new ObjectMapper());

    @Test
    void replacesSimpleAndNestedKeys() {
        Map<String, Object> data = Map.of("nombre", "Ana", "pedido", Map.of("id", 7, "lineas", List.of(Map.of("sku", "A1"))));

        assertEquals("Hola Ana, pedido 7 (A1)", renderer.render("Hola {{nombre}}, pedido {{ pedido.id }} ({{pedido.lineas.0.sku}})", data));
    }

    @Test
    void missingKeysBecomeEmptyAndSpecialCharactersAreKept() {
        assertEquals("Total: $5 \\ ", renderer.render("Total: {{importe}} \\ {{nada}}", Map.of("importe", "$5")));
    }

    @Test
    void mapsAndListsAreWrittenAsJson() {
        assertEquals("{\"a\":1} [1,2]", renderer.render("{{obj}} {{lista}}", Map.of("obj", Map.of("a", 1), "lista", List.of(1, 2))));
    }
}
