package com.logitrack.logitrack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EndpointsIntegrationTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate rest = new TestRestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private static String adminToken;
    private static String empleadoToken;
    private static Long createdBodegaId;
    private static Long createdProductoId;

    private String baseUrl() { return "http://localhost:" + port + "/api"; }

    @Test
    @Order(1)
    public void registerAdminAndEmpleado() throws Exception {
        // Register Admin
        String url = baseUrl() + "/auth/register";
        Map<String,Object> admin = Map.of(
                "nombre", "prueba-admin",
                "email", "prueba.admin@example.com",
                "password", "123456",
                "rol", "ADMIN"
        );

        ResponseEntity<String> r1 = rest.postForEntity(url, admin, String.class);
        assertTrue(r1.getStatusCode().is2xxSuccessful(), "Registro admin falló: " + r1.getBody());

        // Register Empleado
        Map<String,Object> emp = Map.of(
                "nombre", "prueba-empleado",
                "email", "prueba.empleado@example.com",
                "password", "123456",
                "rol", "EMPLEADO"
        );

        ResponseEntity<String> r2 = rest.postForEntity(url, emp, String.class);
        assertTrue(r2.getStatusCode().is2xxSuccessful(), "Registro empleado falló: " + r2.getBody());
    }

    @Test
    @Order(2)
    public void loginUsers() throws Exception {
        String url = baseUrl() + "/auth/login";

        Map<String,String> bodyAdmin = Map.of("email", "prueba.admin@example.com", "password", "123456");
        ResponseEntity<String> ra = rest.postForEntity(url, bodyAdmin, String.class);
        assertTrue(ra.getStatusCode().is2xxSuccessful(), "Login admin falló: " + ra.getBody());
        JsonNode ja = mapper.readTree(ra.getBody());
        adminToken = ja.get("token").asText();
        assertNotNull(adminToken);

        Map<String,String> bodyEmp = Map.of("email", "prueba.empleado@example.com", "password", "123456");
        ResponseEntity<String> re = rest.postForEntity(url, bodyEmp, String.class);
        assertTrue(re.getStatusCode().is2xxSuccessful(), "Login empleado falló: " + re.getBody());
        JsonNode je = mapper.readTree(re.getBody());
        empleadoToken = je.get("token").asText();
        assertNotNull(empleadoToken);
    }

    @Test
    @Order(3)
    public void testBodegaCrudWithAdmin() throws Exception {
        String url = baseUrl() + "/bodegas";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create bodega
        Map<String,Object> dto = Map.of(
                "nombre", "Bodega Test",
                "ubicacion", "Calle Test 1",
                "capacidad", 500,
                "encargadoId", 1,
                "activo", true
        );

        HttpEntity<Map<String,Object>> req = new HttpEntity<>(dto, headers);
        ResponseEntity<String> resp = rest.postForEntity(url, req, String.class);
        assertTrue(resp.getStatusCode().is2xxSuccessful() || resp.getStatusCode().value() == 201, "Crear bodega falló: " + resp.getBody());
        JsonNode j = mapper.readTree(resp.getBody());
        if (j.has("id")) createdBodegaId = j.get("id").asLong();

        // Get all bodegas
        HttpEntity<Void> getReq = new HttpEntity<>(headers);
        ResponseEntity<String> listResp = rest.exchange(url, HttpMethod.GET, getReq, String.class);
        assertTrue(listResp.getStatusCode().is2xxSuccessful(), "Listar bodegas falló: " + listResp.getBody());
    }

    @Test
    @Order(4)
    public void testProductoCrudWithAdmin() throws Exception {
        String url = baseUrl() + "/productos";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String,Object> prod = Map.of(
                "nombre", "Producto Test",
                "categoria", "Cat 1",
                "precio", new BigDecimal("12.50"),
                "activo", true
        );

        HttpEntity<Map<String,Object>> req = new HttpEntity<>(prod, headers);
        ResponseEntity<String> resp = rest.postForEntity(url, req, String.class);
        assertTrue(resp.getStatusCode().is2xxSuccessful() || resp.getStatusCode().value() == 201, "Crear producto falló: " + resp.getBody());
        JsonNode j = mapper.readTree(resp.getBody());
        if (j.has("id")) createdProductoId = j.get("id").asLong();

        // List productos
        HttpEntity<Void> getReq = new HttpEntity<>(headers);
        ResponseEntity<String> listResp = rest.exchange(url, HttpMethod.GET, getReq, String.class);
        assertTrue(listResp.getStatusCode().is2xxSuccessful(), "Listar productos falló: " + listResp.getBody());
    }

    @Test
    @Order(5)
    public void testProtectedEndpointsAccessibleForEmpleado() throws Exception {
        // Check GET /bodegas with empleado token
        String url = baseUrl() + "/bodegas";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(empleadoToken);
        HttpEntity<Void> getReq = new HttpEntity<>(headers);
        ResponseEntity<String> resp = rest.exchange(url, HttpMethod.GET, getReq, String.class);
        assertTrue(resp.getStatusCode().is2xxSuccessful(), "Empleado no puede acceder a /bodegas: " + resp.getStatusCode().value());
    }

    @Test
    @Order(99)
    public void cleanupCreatedResources() throws Exception {
        // Try delete created producto and bodega if IDs exist
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> req = new HttpEntity<>(headers);

        if (createdProductoId != null) {
            rest.exchange(baseUrl() + "/productos/" + createdProductoId, HttpMethod.DELETE, req, String.class);
        }
        if (createdBodegaId != null) {
            rest.exchange(baseUrl() + "/bodegas/" + createdBodegaId, HttpMethod.DELETE, req, String.class);
        }
    }
}
