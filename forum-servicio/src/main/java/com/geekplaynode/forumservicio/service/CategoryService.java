package com.geekplaynode.forumservicio.service;

import com.geekplaynode.forumservicio.dto.CategoryResponse;
import com.geekplaynode.forumservicio.dto.CreateCategoryRequest;
import com.geekplaynode.forumservicio.model.Category;
import com.geekplaynode.forumservicio.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 1. OBTENER TODAS LAS CATEGORÍAS
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromCategory)
                .collect(Collectors.toList());
    }

    // 2. OBTENER CATEGORÍA POR ID
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        return CategoryResponse.fromCategory(category);
    }

    // 3. OBTENER CATEGORÍA POR SLUG
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        return CategoryResponse.fromCategory(category);
    }

    // 4. CREAR CATEGORÍA (solo ADMIN)
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        // Verificar si ya existe
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Ya existe una categoría con ese slug");
        }

        Category category = Category.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .slug(request.getSlug())
                .build();

        Category saved = categoryRepository.save(category);
        return CategoryResponse.fromCategory(saved);
    }

    // 5. ACTUALIZAR CATEGORÍA (solo ADMIN)
    @Transactional
    public CategoryResponse updateCategory(Long id, CreateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        category.setNombre(request.getNombre());
        category.setDescripcion(request.getDescripcion());

        Category updated = categoryRepository.save(category);
        return CategoryResponse.fromCategory(updated);
    }

    // 6. ELIMINAR CATEGORÍA (solo ADMIN)
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada");
        }
        categoryRepository.deleteById(id);
    }
}