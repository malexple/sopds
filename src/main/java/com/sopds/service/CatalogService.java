package com.sopds.service;

import com.sopds.domain.Catalog;
import com.sopds.repository.CatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CatalogRepository catalogRepository;

    public Optional<Catalog> getById(Long id) {
        return catalogRepository.findById(id);
    }

    public Optional<Catalog> getByPath(String path) {
        return catalogRepository.findByPath(path);
    }

    public long count() {
        return catalogRepository.count();
    }

    public List<Catalog> getRootCatalogs() {
        return catalogRepository.findRootCatalogs();
    }

    public List<Catalog> getChildren(Long parentId) {
        return catalogRepository.findByParentId(parentId);
    }

    public Catalog save(Catalog catalog) {
        return catalogRepository.save(catalog);
    }
}
