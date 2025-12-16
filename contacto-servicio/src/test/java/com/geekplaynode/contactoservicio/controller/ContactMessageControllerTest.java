package com.geekplaynode.contactoservicio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekplaynode.contactoservicio.model.ContactMessage;
import com.geekplaynode.contactoservicio.service.ContactMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactMessageController.class)
@AutoConfigureMockMvc(addFilters = false) // Desactivamos seguridad para simplificar
class ContactMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContactMessageService contactMessageService;

    @Test
    void guardarContacto_DeberiaRetornar200_YGuardarMensaje() throws Exception {
        // 1. Arrange
        ContactMessage contacto = new ContactMessage();
        contacto.setNombre("Maria");
        contacto.setEmail("maria@test.com");
        contacto.setMensaje("Consulta sobre precios");

        // Simulamos que el servicio devuelve el mismo objeto (o uno con ID)
        ContactMessage saved = new ContactMessage();
        saved.setId(1L); // Asumiendo que tiene ID
        saved.setNombre("Maria");
        
        when(contactMessageService.guardarContacto(any(ContactMessage.class))).thenReturn(saved);

        // 2. Act & Assert
        mockMvc.perform(post("/contacto/guardar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contacto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Maria"));
    }
}