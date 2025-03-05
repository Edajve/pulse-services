package com.pulse.pulseservices.service;

import com.pulse.pulseservices.entity.Qr;
import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.model.Token;
import com.pulse.pulseservices.repositories.QrRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;

@Service
public class QrService {

    private final QrRepository qrRepository;
    private static final Logger logger = LoggerFactory.getLogger(QrService.class);


    @Autowired
    public QrService(QrRepository qrRepository) {
        this.qrRepository = qrRepository;
    }

    public UUID generateUUID() {
        return UUID.randomUUID();
    }

    public void saveQrToDatabaseAndAssignToUser(UUID uuid, User user) {
        new Qr();
        qrRepository.save(
                Qr.builder()
                        .user(user)
                        .isQrActive(true)
                        .generatedQrID(uuid)
                        .build()
        );
        logger.info("Successfully assigned QR code {} to User ID = {}", uuid, user.getId());
    }

    public boolean isUuidValid(Long scannedUserId, Token requestBody) {
        byte[] uuidBytes = qrRepository.getUUIDById(Math.toIntExact(scannedUserId));
        UUID dbUuid = bytesToUUID(uuidBytes);
        UUID userUuid = requestBody.getUuid();
        return dbUuid.equals(userUuid);
    }

    public UUID bytesToUUID(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new UUID(high, low);
    }

    public byte[] getUUIDByUserId(Long userId) {
        byte[] uuid = qrRepository.getUUIDById(Math.toIntExact(userId));
        return Objects.isNull(uuid) ? null : uuid;
    }
}
