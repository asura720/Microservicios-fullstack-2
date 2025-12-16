package com.geekplaynode.contactoservicio.controller;

import com.geekplaynode.contactoservicio.model.ContactMessage;
import com.geekplaynode.contactoservicio.service.ContactMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contacto")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Contacto", description = "API para gestión de mensajes de contacto")
public class ContactMessageController {

    @Autowired
    private ContactMessageService contactMessageService;

    @PostMapping("/guardar")
    @Operation(summary = "Guardar mensaje de contacto", description = "Guarda un nuevo mensaje de contacto enviado desde el formulario.")
    @ApiResponse(responseCode = "200", description = "Mensaje guardado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    public ContactMessage guardarContacto(@RequestBody ContactMessage contacto) {
        return contactMessageService.guardarContacto(contacto);
    }
}