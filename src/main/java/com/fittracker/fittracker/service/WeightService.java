package com.fittracker.fittracker.service;

import com.fittracker.fittracker.entity.Weight;
import com.fittracker.fittracker.exception.WeightAlreadyExistsException;
import com.fittracker.fittracker.exception.WeightNotFoundException;
import com.fittracker.fittracker.repository.WeightRepository;
import com.fittracker.fittracker.request.WeightRequest;
import com.fittracker.fittracker.response.WeightResponse;
import com.fittracker.fittracker.security.SecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class WeightService {

    private final WeightRepository weightRepository;

    @Autowired
    public WeightService(WeightRepository weightRepository) {
        this.weightRepository = weightRepository;
    }

    public WeightResponse save(WeightRequest weightRequest) {
        if (weightRepository.existsByDateAndUserId(weightRequest.date(), SecurityHelper.getUserId())) {
            throw new WeightAlreadyExistsException(weightRequest.date());
        }

        Weight weight = weightRequest.toWeight();
        weight.setUserId(SecurityHelper.getUserId());
        Weight dbWeight = weightRepository.save(weight);

        return WeightResponse.fromWeight(dbWeight);
    }

    public WeightResponse findByDate(LocalDate date) {
        return weightRepository.findByDateAndUserId(date, SecurityHelper.getUserId())
                .map(WeightResponse::fromWeight)
                .orElseThrow(() -> new WeightNotFoundException(date));
    }
}
