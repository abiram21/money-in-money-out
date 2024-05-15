package com.transaction.modules.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table
public class Account {
    @Id
    @Column(name = "USER_ID")
    @Setter(AccessLevel.NONE)
    private String userId;

    @Column(name = "BALANCE")
    private Double balance;
}
