package com.pulse.pulseservices.model.FeedBack;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class FeedBack {
    @Getter
    @Id
    private Long id;
    private String comment;
    private int rating;
    private Long user_id;
    private boolean has_been_read;
    private boolean flagged;

    public void setId(Long id) {
        this.id = id;
    }

}
