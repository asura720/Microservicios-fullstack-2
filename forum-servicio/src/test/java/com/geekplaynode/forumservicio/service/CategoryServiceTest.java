package com.geekplaynode.forumservicio.service;

import com.geekplaynode.forumservicio.dto.CategoryResponse;
import com.geekplaynode.forumservicio.dto.CreateCategoryRequest;
import com.geekplaynode.forumservicio.model.Category;
import com.geekplaynode.forumservicio.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_DeberiaCrear_CuandoSlugNoExiste() {
        // 1. Arrange
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setNombre("Java");
        request.setSlug("java-lang");
        request.setDescripcion("Todo sobre Java");

        when(categoryRepository.existsBySlug("java-lang")).thenReturn(false);
        
        // Simulamos que al guardar retorna la entidad con ID
        Category savedCategory = Category.builder()
                .id(1L)
                .nombre("Java")
                .slug("java-lang")
                .build();
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // 2. Act
        CategoryResponse response = categoryService.createCategory(request);

        // 3. Assert
        assertNotNull(response);
        assertEquals("Java", response.getNombre());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_DeberiaFallar_CuandoSlugYaExiste() {
        // 1. Arrange
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setSlug("java-repetido");

        when(categoryRepository.existsBySlug("java-repetido")).thenReturn(true);

        // 2. Act & Assert
        assertThrows(RuntimeException.class, () -> {
            categoryService.createCategory(request);
        });

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getCategoryById_DeberiaRetornarCategoria_SiExiste() {
        // 1. Arrange
        Long id = 1L;
        Category category = Category.builder().id(id).nombre("Test").build();
        
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        // 2. Act
        CategoryResponse response = categoryService.getCategoryById(id);

        // 3. Assert
        assertEquals("Test", response.getNombre());
    }

    @Test
    void deleteCategory_DeberiaFallar_SiNoExiste() {
        // 1. Arrange
        Long id = 99L;
        when(categoryRepository.existsById(id)).thenReturn(false);

        // 2. Act & Assert
        assertThrows(RuntimeException.class, () -> categoryService.deleteCategory(id));
        verify(categoryRepository, never()).deleteById(any());
    }
}