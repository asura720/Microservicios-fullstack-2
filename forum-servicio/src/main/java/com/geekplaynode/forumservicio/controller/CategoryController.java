package com.geekplaynode.forumservicio.controller;

import com.geekplaynode.forumservicio.dto.CategoryResponse;
import com.geekplaynode.forumservicio.dto.CreateCategoryRequest;
import com.geekplaynode.forumservicio.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "API para gestión de categorías del foro")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Listar categorías", description = "Obtiene todas las categorías del foro (público).")
    @ApiResponse(responseCode = "200", description = "Categorías obtenidas exitosamente")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID", description = "Obtiene una categoría específica por su ID (público).")
    @ApiResponse(responseCode = "200", description = "Categoría obtenida exitosamente")
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Obtener categoría por slug", description = "Obtiene una categoría específica por su slug (público).")
    @ApiResponse(responseCode = "200", description = "Categoría obtenida exitosamente")
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
    }

    @PostMapping
    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría del foro (solo ADMIN).")
    @ApiResponse(responseCode = "200", description = "Categoría creada exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Prohibido - Solo administradores")
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody CreateCategoryRequest request,
            HttpServletRequest httpRequest
    ) {
        String role = (String) httpRequest.getAttribute("userRole");

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría", description = "Actualiza una categoría existente (solo ADMIN).")
    @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Prohibido - Solo administradores")
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody CreateCategoryRequest request,
            HttpServletRequest httpRequest
    ) {
        String role = (String) httpRequest.getAttribute("userRole");

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría del foro (solo ADMIN).")
    @ApiResponse(responseCode = "204", description = "Categoría eliminada exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Prohibido - Solo administradores")
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        String role = (String) httpRequest.getAttribute("userRole");

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
