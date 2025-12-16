package com.geekplaynode.forumservicio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekplaynode.forumservicio.client.ProfileServiceClient;
import com.geekplaynode.forumservicio.dto.CategoryResponse;
import com.geekplaynode.forumservicio.dto.CreateCategoryRequest;
import com.geekplaynode.forumservicio.service.CategoryService;
import com.geekplaynode.forumservicio.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CategoryService categoryService;

    // --- AGREGADOS: Necesarios para que cargue el contexto de seguridad ---
    @MockBean private JwtUtils jwtUtils;
    @MockBean private ProfileServiceClient profileServiceClient;
    // ---------------------------------------------------------------------

    @Test
    void getAllCategories_DeberiaRetornar200_EsPublico() throws Exception {
        // 1. Arrange
        when(categoryService.getAllCategories()).thenReturn(List.of(new CategoryResponse()));

        // 2. Act & Assert
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk());
    }

    @Test
    void getCategoryBySlug_DeberiaRetornar200_EsPublico() throws Exception {
        // 1. Arrange
        String slug = "java";
        CategoryResponse mockResponse = new CategoryResponse();
        mockResponse.setSlug(slug);
        
        when(categoryService.getCategoryBySlug(slug)).thenReturn(mockResponse);

        // 2. Act & Assert
        mockMvc.perform(get("/api/categories/slug/{slug}", slug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value(slug));
    }

    @Test
    void createCategory_DeberiaFuncionar_SiEsAdmin() throws Exception {
        // 1. Arrange
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .nombre("Nueva")
                .slug("nueva")
                .build();

        when(categoryService.createCategory(any(CreateCategoryRequest.class)))
                .thenReturn(new CategoryResponse());

        // 2. Act & Assert
        // Simulamos que el filtro de seguridad ya extrajo el rol y lo puso en el request attribute
        mockMvc.perform(post("/api/categories")
                        .requestAttr("userRole", "ADMIN") 
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void createCategory_DeberiaRetornar403_SiNoEsAdmin() throws Exception {
        // 1. Arrange
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .nombre("Hack")
                .build();

        // 2. Act & Assert
        mockMvc.perform(post("/api/categories")
                        .requestAttr("userRole", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCategory_DeberiaFuncionar_SiEsAdmin() throws Exception {
        // 1. Arrange
        Long id = 1L;

        // 2. Act & Assert
        mockMvc.perform(delete("/api/categories/{id}", id)
                        .requestAttr("userRole", "ADMIN"))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(id);
    }
}