package com.pulse.pulseservices.service;

import com.pulse.pulseservices.model.FeedBack.FeedBack;
import com.pulse.pulseservices.repositories.FeedBackRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FeedBackService {
    private FeedBackRepository feedBackRepository;

    public Optional<List<FeedBack>> getAllFeedBack() {
        return Optional.of(feedBackRepository.findAll());
    }
}
