package br.ufpr.tads.catalog.catalog.domain.service;

import br.ufpr.tads.catalog.catalog.domain.model.Category;
import br.ufpr.tads.catalog.catalog.domain.repository.CategoryRepository;
import br.ufpr.tads.catalog.catalog.domain.request.CreateCategoryRequestDTO;
import br.ufpr.tads.catalog.catalog.domain.response.CategoryResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public CategoryResponseDTO createCategory(CreateCategoryRequestDTO createCategoryRequestDTO) {

        validateCategoryRequest(createCategoryRequestDTO);

        return mapToDTO(categoryRepository.save(mapToEntity(createCategoryRequestDTO)));
    }

    public SliceImpl<CategoryResponseDTO> getCategories(Pageable pageable) {
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        List<CategoryResponseDTO> categoryResponseDTOS = categoryPage.getContent().stream()
                .map(this::mapToDTO)
                .toList();
        return new SliceImpl<>(categoryResponseDTOS, categoryPage.getPageable(), categoryPage.hasNext());
    }

    public void deleteCategory(Long id) {
        categoryRepository.findById(id).ifPresent(categoryRepository::delete);
    }

    public CategoryResponseDTO updateCategory(Long id, CreateCategoryRequestDTO createCategoryRequestDTO) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);

        if (optionalCategory.isEmpty()) {
            throw new IllegalArgumentException("Categoria não encontrada.");
        }

        validateCategoryRequest(createCategoryRequestDTO);

        Category category = optionalCategory.get();
        category.setName(createCategoryRequestDTO.getName());
        category.setDescription(createCategoryRequestDTO.getDescription());
        category.setUrlImage(createCategoryRequestDTO.getUrlImage());
        categoryRepository.save(category);

        return mapToDTO(optionalCategory.get());
    }

    private void validateCategoryRequest(CreateCategoryRequestDTO createCategoryRequestDTO) {
        if (categoryRepository.existsByName(createCategoryRequestDTO.getName())) {
            throw new IllegalArgumentException("Já existe uma categoria com o nome informado.");
        }
    }

    private Category mapToEntity(CreateCategoryRequestDTO createCategoryRequestDTO) {
        return Category.builder()
                .name(createCategoryRequestDTO.getName())
                .description(createCategoryRequestDTO.getDescription())
                .urlImage(createCategoryRequestDTO.getUrlImage())
                .build();
    }

    private CategoryResponseDTO mapToDTO(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .urlImage(category.getUrlImage())
                .build();
    }

    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));
    }
}
