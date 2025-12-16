package com.geekplaynode.contactoservicio.service;

import com.geekplaynode.contactoservicio.model.ContactMessage;
import com.geekplaynode.contactoservicio.repository.ContactMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactMessageServiceTest {

    @Mock
    private ContactMessageRepository contactMessageRepository;

    @InjectMocks
    private ContactMessageService contactMessageService;

    @Test
    void guardarContacto_DeberiaGuardar_CuandoDatosSonValidos() {
        // 1. Arrange
        ContactMessage contacto = new ContactMessage();
        contacto.setNombre("Juan Perez");
        contacto.setEmail("juan@test.com");
        contacto.setMensaje("Hola mundo");

        when(contactMessageRepository.save(any(ContactMessage.class))).thenReturn(contacto);

        // 2. Act
        ContactMessage resultado = contactMessageService.guardarContacto(contacto);

        // 3. Assert
        assertNotNull(resultado);
        assertEquals("Juan Perez", resultado.getNombre());
        verify(contactMessageRepository).save(contacto);
    }

    @Test
    void guardarContacto_DeberiaFallar_CuandoNombreEsNulo() {
        // 1. Arrange
        ContactMessage contacto = new ContactMessage();
        contacto.setNombre(null);

        // 2. Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            contactMessageService.guardarContacto(contacto);
        });

        assertEquals("El nombre no puede estar vacío", ex.getMessage());
        verify(contactMessageRepository, never()).save(any());
    }

    @Test
    void guardarContacto_DeberiaFallar_CuandoNombreEstaVacio() {
        // 1. Arrange
        ContactMessage contacto = new ContactMessage();
        contacto.setNombre("");

        // 2. Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            contactMessageService.guardarContacto(contacto);
        });
    }
}