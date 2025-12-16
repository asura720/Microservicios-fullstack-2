package com.geekplaynode.notificacionservicio.config;

import com.geekplaynode.notificacionservicio.model.Notification;
import com.geekplaynode.notificacionservicio.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Override
    public void run(String... args) {
        if (notificationRepository.count() == 0) {
            // Notificación de bienvenida para Ricardo (userId=1)
            Notification notif1 = new Notification();
            notif1.setUserId(1L);
            notif1.setTipo("BIENVENIDA");
            notif1.setTitulo("¡Bienvenido a GeekPlay!");
            notif1.setMensaje("Gracias por unirte a nuestra comunidad geek. Explora el foro y comparte tus pasiones.");
            notif1.setLeida(false);
            notificationRepository.save(notif1);
            
            // Notificación de info
            Notification notif2 = new Notification();
            notif2.setUserId(1L);
            notif2.setTipo("INFO");
            notif2.setTitulo("Nueva funcionalidad disponible");
            notif2.setMensaje("Ahora puedes recibir notificaciones en tiempo real de tus posts favoritos.");
            notif2.setLeida(false);
            notificationRepository.save(notif2);
            
            System.out.println("✅ Notificaciones de prueba cargadas");
        }
    }
}