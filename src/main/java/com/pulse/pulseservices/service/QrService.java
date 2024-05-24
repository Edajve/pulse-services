package com.pulse.pulseservices.service;

import com.pulse.pulseservices.entity.Qr;
import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.repositories.QrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.UUID;

@Service
public class QrService {

    private final QrRepository qrRepository;

    @Autowired
    public QrService(QrRepository qrRepository) {
        this.qrRepository = qrRepository;
    }

    public UUID generateUUID() {
        return  UUID.randomUUID();
    }

    public Qr saveQrToDatabaseAndAssignToUser(UUID uuid, User user) {
        return qrRepository.save(
                new Qr().builder()
                        .user(user)
                        .isQrActive(true)
                        .generatedQrID(uuid)
                        .build()
        );
    }

    public UUID bytesToUUID(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new UUID(high, low);
    }
}
