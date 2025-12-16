package com.geekplaynode.forumservicio.config;

import com.geekplaynode.forumservicio.model.Category;
import com.geekplaynode.forumservicio.model.Post;
import com.geekplaynode.forumservicio.repository.CategoryRepository;
import com.geekplaynode.forumservicio.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            System.out.println("🔄 Cargando categorías y posts de ejemplo...");

            // Crear categorías
            Category videojuegos = Category.builder()
                    .nombre("Videojuegos")
                    .descripcion("Todo sobre videojuegos, consolas y gaming")
                    .slug("videojuegos")
                    .build();
            categoryRepository.save(videojuegos);

            Category peliculas = Category.builder()
                    .nombre("Películas & Series")
                    .descripcion("Discusiones sobre cine, series y streaming")
                    .slug("peliculas-series")
                    .build();
            categoryRepository.save(peliculas);

            Category tecnologia = Category.builder()
                    .nombre("Tecnología")
                    .descripcion("Noticias y novedades del mundo tech")
                    .slug("tecnologia")
                    .build();
            categoryRepository.save(tecnologia);

            // Posts de Videojuegos
            Post post1 = Post.builder()
                    .titulo("Elden Ring DLC Shadow of the Erdtree")
                    .contenido("El nuevo DLC de Elden Ring es BRUTAL. Los nuevos jefes están al nivel de Malenia. Mesmer el Empalador me costó 40 intentos. ¿Alguien más lo está jugando?")
                    .category(videojuegos)
                    .autorId(2L)
                    .autorNombre("Ricardo Gamer")
                    .autorAvatar("https://i.pravatar.cc/150?img=12")
                    .build();
            postRepository.save(post1);

            Post post2 = Post.builder()
                    .titulo("Zelda TOTK vs Elden Ring - ¿Cuál prefieren?")
                    .contenido("Debate del año. Para mí TOTK tiene mejor exploración, pero Elden Ring tiene mejor combate y atmosfera. ¿Qué opinan ustedes?")
                    .category(videojuegos)
                    .autorId(2L)
                    .autorNombre("Ricardo Gamer")
                    .autorAvatar("https://i.pravatar.cc/150?img=12")
                    .build();
            postRepository.save(post2);

            // Posts de Películas & Series
            Post post3 = Post.builder()
                    .titulo("One Piece Live Action - Temporada 2 confirmada")
                    .contenido("Netflix confirmó la segunda temporada. Según rumores adaptarán el arco de Arabasta. Crocodile va a estar ÉPICO en live action.")
                    .category(peliculas)
                    .autorId(3L)
                    .autorNombre("Maria Otaku")
                    .autorAvatar("https://i.pravatar.cc/150?img=5")
                    .build();
            postRepository.save(post3);

            Post post4 = Post.builder()
                    .titulo("Breaking Bad vs Better Call Saul")
                    .contenido("Debate eterno. Para mí Better Call Saul tiene mejor desarrollo de personajes, pero Breaking Bad tiene más tensión. ¿Ustedes qué prefieren?")
                    .category(peliculas)
                    .autorId(3L)
                    .autorNombre("Maria Otaku")
                    .autorAvatar("https://i.pravatar.cc/150?img=5")
                    .build();
            postRepository.save(post4);

            // Posts de Tecnología
            Post post5 = Post.builder()
                    .titulo("ChatGPT Vision - Game Changer")
                    .contenido("La nueva versión de ChatGPT que puede ver imágenes es increíble. Le pasé un screenshot de mi código con bug y me lo identificó al toque. ¿Ya lo probaron?")
                    .category(tecnologia)
                    .autorId(2L)
                    .autorNombre("Ricardo Gamer")
                    .autorAvatar("https://i.pravatar.cc/150?img=12")
                    .build();
            postRepository.save(post5);

            Post post6 = Post.builder()
                    .titulo("iPhone 16 Pro Max - ¿Vale la pena en Chile?")
                    .contenido("Con el precio del iPhone 16 Pro Max en Chile (más de 1 millón de pesos), ¿realmente vale la pena? La cámara es brutal pero el precio duele.")
                    .category(tecnologia)
                    .autorId(3L)
                    .autorNombre("Maria Otaku")
                    .autorAvatar("https://i.pravatar.cc/150?img=5")
                    .build();
            postRepository.save(post6);

            System.out.println("✅ Categorías y posts de ejemplo cargados");
        }
    }
}