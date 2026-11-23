package com.dinomuseo.backend.service;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NegocioIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    private String token(String correo, String password) throws Exception {
        String respuesta = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"password\":\"" + password + "\"}"))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(respuesta, "$.token");
    }

    @Test
    void cualquieraPuedeVerLasEntradasSinIniciarSesion() throws Exception {
        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("General"));
    }

    @Test
    void elClienteCompraEntradasYElTotalSeCalculaEnElBackend() throws Exception {
        String cliente = token("cliente@dinomuseo.cl", "Cliente123!");
        String compra = "{\"fechaVisita\":\"" + LocalDate.now().plusDays(1)
                + "\",\"items\":[{\"productoId\":1,\"cantidad\":2},{\"productoId\":2,\"cantidad\":1}]}";

        mockMvc.perform(post("/api/v1/boletas").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON).content(compra))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(25000))
                .andExpect(jsonPath("$.detalles.length()").value(2));

        mockMvc.perform(get("/api/v1/boletas/mis-boletas").header("Authorization", cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].total").value(25000));
    }

    @Test
    void elVendedorVeLasBoletasPeroNoPuedeCrearEntradas() throws Exception {
        String cliente = token("cliente@dinomuseo.cl", "Cliente123!");
        String vendedor = token("vendedor@dinomuseo.cl", "Vendedor123!");

        mockMvc.perform(get("/api/v1/boletas").header("Authorization", cliente))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/boletas").header("Authorization", vendedor))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/productos").header("Authorization", vendedor)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }
}