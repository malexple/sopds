package com.sopds.service;

import com.sopds.domain.Series;
import com.sopds.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SeriesService {

    private final SeriesRepository seriesRepository;

    public Optional<Series> getById(Long id) {
        return seriesRepository.findById(id);
    }

    public Optional<Series> getBySer(String ser) {
        return seriesRepository.findBySer(ser);
    }

    public long count() {
        return seriesRepository.count();
    }

    public Page<Series> search(String query, int page, int size) {
        return seriesRepository.searchByNameContains(query, PageRequest.of(page, size));
    }

    public Page<Series> getAll(int page, int size) {
        return seriesRepository.findAllByOrderBySearchSer(PageRequest.of(page, size));
    }

    public List<Series> getAll() {
        return seriesRepository.findAll();
    }

    public Series save(Series series) {
        return seriesRepository.save(series);
    }
}
