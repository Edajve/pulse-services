package com.pulse.pulseservices.model;

import com.pulse.pulseservices.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Qr {
    private Long id;
    private byte[] imageBytes;
    private boolean isQrActive;
    private User user;
}
