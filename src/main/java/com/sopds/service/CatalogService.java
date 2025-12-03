package com.sopds.service;

import com.sopds.domain.Catalog;
import com.sopds.repository.CatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CatalogService {

    private final CatalogRepository catalogRepository;

    @Transactional(readOnly = true)
    public Optional<Catalog> getById(Long id) {
        return catalogRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Catalog> getByPath(String path) {
        return catalogRepository.findByPath(path);
    }

    @Transactional(readOnly = true)
    public List<Catalog> getRootCatalogs() {
        return catalogRepository.findRootCatalogs();
    }

    @Transactional(readOnly = true)
    public List<Catalog> getChildren(Long parentId) {
        return catalogRepository.findByParentId(parentId);
    }

    @Transactional(readOnly = true)
    public List<Catalog> getAll() {
        return catalogRepository.findAll();
    }

    public Catalog getOrCreate(String catName, String path, Integer catType, Long parentId) {
        return catalogRepository.findByPath(path)
                .orElseGet(() -> {
                    log.info("Creating catalog: {}", path);

                    Catalog parent = parentId != null
                            ? catalogRepository.findById(parentId).orElse(null)
                            : null;

                    Catalog catalog = Catalog.builder()
                            .catName(catName)
                            .path(path)
                            .catType(catType != null ? catType : 0)
                            .catSize(0)
                            .parent(parent)
                            .build();

                    return catalogRepository.save(catalog);
                });
    }

    public Catalog update(Long id, String catName, Integer catType, Integer catSize) {
        log.info("Updating catalog ID: {}", id);

        Catalog catalog = catalogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Catalog not found: " + id));

        if (catName != null) catalog.setCatName(catName);
        if (catType != null) catalog.setCatType(catType);
        if (catSize != null) catalog.setCatSize(catSize);

        return catalogRepository.save(catalog);
    }

    public void updateSize(Long id, Integer catSize) {
        Catalog catalog = catalogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Catalog not found: " + id));

        catalog.setCatSize(catSize);
        catalogRepository.save(catalog);
    }

    public void delete(Long id) {
        log.info("Deleting catalog ID: {}", id);
        catalogRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return catalogRepository.count();
    }
}
