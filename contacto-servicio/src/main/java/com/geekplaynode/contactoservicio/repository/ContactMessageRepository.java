package com.geekplaynode.contactoservicio.repository;

import com.geekplaynode.contactoservicio.model.ContactMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactMessageRepository extends CrudRepository<ContactMessage, Long> {
}