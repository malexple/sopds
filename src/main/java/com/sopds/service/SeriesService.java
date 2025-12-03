package com.sopds.service;

import com.sopds.domain.Series;
import com.sopds.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SeriesService {

    private final SeriesRepository seriesRepository;

    @Transactional(readOnly = true)
    public Optional<Series> getById(Long id) {
        return seriesRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Series> getBySer(String ser) {
        return seriesRepository.findBySer(ser);
    }

    @Transactional(readOnly = true)
    public List<Series> search(String query) {
        log.debug("Searching series: {}", query);
        return seriesRepository.searchBySer(query);
    }

    @Transactional(readOnly = true)
    public List<Series> getByLangCode(Integer langCode) {
        return seriesRepository.findByLangCode(langCode);
    }

    @Transactional(readOnly = true)
    public Page<Series> getAll(int page, int size) {
        return seriesRepository.findAllByOrderBySerAsc(PageRequest.of(page, size));
    }

    public Series getOrCreate(String ser, Integer langCode) {
        return seriesRepository.findBySer(ser)
                .orElseGet(() -> {
                    log.info("Creating series: {}", ser);
                    Series series = Series.builder()
                            .ser(ser)
                            .searchSer(ser.toLowerCase())
                            .langCode(langCode != null ? langCode : 9)
                            .build();
                    return seriesRepository.save(series);
                });
    }

    public Series update(Long id, String ser, Integer langCode) {
        log.info("Updating series ID: {}", id);

        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Series not found: " + id));

        if (ser != null) {
            series.setSer(ser);
            series.setSearchSer(ser.toLowerCase());
        }
        if (langCode != null) {
            series.setLangCode(langCode);
        }

        return seriesRepository.save(series);
    }

    public void delete(Long id) {
        log.info("Deleting series ID: {}", id);
        seriesRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return seriesRepository.count();
    }
}
