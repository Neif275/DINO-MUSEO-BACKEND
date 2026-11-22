package com.dinomuseo.backend.security;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Prueba de integracion: levanta el backend completo (con H2) y hace peticiones HTTP reales
// para comprobar el login, el token JWT y los permisos de cada rol.
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    private String login(String correo, String password, String campo) throws Exception {
        String respuesta = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$." + campo);
    }

    @Test
    void loginCorrectoEntregaTokenYNoDevuelveLaContrasena() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@dinomuseo.cl\",\"password\":\"Admin123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.usuario.rol").value("ADMIN"))
                .andExpect(jsonPath("$.usuario.password").doesNotExist());
    }

    @Test
    void loginConContrasenaIncorrectaResponde401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@dinomuseo.cl\",\"password\":\"otra\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinTokenNoSePuedeEntrar() throws Exception {
        mockMvc.perform(get("/api/v1/auth/perfil"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void soloElAdministradorGestionaUsuarios() throws Exception {
        String cliente = login("cliente@dinomuseo.cl", "Cliente123!", "token");
        String vendedor = login("vendedor@dinomuseo.cl", "Vendedor123!", "token");
        String admin = login("admin@dinomuseo.cl", "Admin123!", "token");

        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + cliente))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + vendedor))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
    }

    @Test
    void elRefreshTokenEntregaUnTokenNuevo() throws Exception {
        String refresh = login("cliente@dinomuseo.cl", "Cliente123!", "refreshToken");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        // el refresh token no sirve para entrar a un endpoint protegido
        mockMvc.perform(get("/api/v1/auth/perfil").header("Authorization", "Bearer " + refresh))
                .andExpect(status().isUnauthorized());
    }
}
