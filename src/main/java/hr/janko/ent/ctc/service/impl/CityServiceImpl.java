package hr.janko.ent.ctc.service.impl;

import hr.janko.ent.ctc.data.model.CityModel;
import hr.janko.ent.ctc.data.repository.CityRepository;
import hr.janko.ent.ctc.service.CityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    @Override
    public Optional<CityModel> fetchById(Long id) {
        return cityRepository.findById(id);
    }
}
