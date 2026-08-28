package com.tf2autobot.dbmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "settings")
@Getter @Setter @NoArgsConstructor
public class Setting {

    @Id
    @Column(name = "key", length = 100)
    private String key;

    @Column(columnDefinition = "TEXT")
    private String value;

    public Setting(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
