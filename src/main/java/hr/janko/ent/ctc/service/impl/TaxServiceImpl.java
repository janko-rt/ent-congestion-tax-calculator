package hr.janko.ent.ctc.service.impl;

import hr.janko.ent.ctc.service.TaxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaxServiceImpl implements TaxService {

    @Override
    @Transactional
    public void saveTax() {

    }
}
