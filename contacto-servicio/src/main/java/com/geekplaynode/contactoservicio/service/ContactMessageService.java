package com.geekplaynode.contactoservicio.service;

import com.geekplaynode.contactoservicio.model.ContactMessage;
import com.geekplaynode.contactoservicio.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContactMessageService {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    public ContactMessage guardarContacto(ContactMessage contacto) {
        if (contacto.getNombre() == null || contacto.getNombre().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        return contactMessageRepository.save(contacto);
    }
}