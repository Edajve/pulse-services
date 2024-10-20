package com.pulse.pulseservices.controller;

import com.pulse.pulseservices.model.FeedBack.FeedBack;
import com.pulse.pulseservices.service.FeedBackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
public class RatingController {

    private final FeedBackService feedbackService;

    @GetMapping("")
    public ResponseEntity<?> getAllFeedback() {
        try {
            Optional<List<FeedBack>> feedBack = feedbackService.getAllFeedBack();

            if (feedBack.isPresent()) return ResponseEntity.ok(feedBack.get());
            else
                return ResponseEntity.status(HttpStatus.OK).body("Currently No Feedback");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

}
