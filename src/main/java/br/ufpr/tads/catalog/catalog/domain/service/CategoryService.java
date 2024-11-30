package br.ufpr.tads.catalog.catalog.domain.service;

import br.ufpr.tads.catalog.catalog.domain.client.ImgurClient;
import br.ufpr.tads.catalog.catalog.domain.model.Category;
import br.ufpr.tads.catalog.catalog.domain.repository.CategoryRepository;
import br.ufpr.tads.catalog.catalog.domain.request.CreateCategoryRequestDTO;
import br.ufpr.tads.catalog.catalog.domain.response.CategoryResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ImgurClient imgurClient;

    public CategoryResponseDTO createCategory(CreateCategoryRequestDTO createCategoryRequestDTO, MultipartFile image) {
        validateCategoryRequest(createCategoryRequestDTO);

        try {
            String urlImage = imgurClient.uploadImage(image);
            return mapToDTO(categoryRepository.save(mapToEntity(createCategoryRequestDTO, urlImage)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public CategoryResponseDTO updateCategory(Long id, CreateCategoryRequestDTO createCategoryRequestDTO, MultipartFile image) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);

        if (optionalCategory.isEmpty()) {
            throw new IllegalArgumentException("Categoria não encontrada.");
        }

        validateCategoryRequest(createCategoryRequestDTO);

        try {
            String urlImage = imgurClient.uploadImage(image);

            Category category = optionalCategory.get();
            category.setName(createCategoryRequestDTO.getName());
            category.setDescription(createCategoryRequestDTO.getDescription());
            categoryRepository.save(category);

            return mapToDTO(optionalCategory.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void validateCategoryRequest(CreateCategoryRequestDTO createCategoryRequestDTO) {
        if (categoryRepository.existsByName(createCategoryRequestDTO.getName())) {
            throw new IllegalArgumentException("Já existe uma categoria com o nome informado.");
        }
    }

    private Category mapToEntity(CreateCategoryRequestDTO createCategoryRequestDTO, String urlImage) {
        return Category.builder()
                .name(createCategoryRequestDTO.getName())
                .description(createCategoryRequestDTO.getDescription())
                .urlImage(urlImage)
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
