package com.sopds.repository;

import com.sopds.domain.Catalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogRepository extends JpaRepository<Catalog, Long> {

    Optional<Catalog> findByPath(String path);

    // Корневые каталоги
    @Query("SELECT c FROM Catalog c WHERE c.parent IS NULL ORDER BY c.catName")
    List<Catalog> findRootCatalogs();

    // Дочерние каталоги
    @Query("SELECT c FROM Catalog c WHERE c.parent.id = :parentId ORDER BY c.catName")
    List<Catalog> findByParentId(@Param("parentId") Long parentId);

    // Каталог по родителю null
    @Query("SELECT c FROM Catalog c WHERE c.parent IS NULL")
    Optional<Catalog> findRootCatalog();
}
