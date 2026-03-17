package org.purchases.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "purchase")
@Getter
@Setter
public class Purchase {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "amt")
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase amount must be positive")
    private Double amt;

    @NotNull(message = "Purchase date must be specified")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @Column(name = "date")
    private LocalDate date;

    @NotBlank(message = "Purchase category must be specified")
    @Column(name = "category")
    private String category;

    @Column(name = "comments")
    private String comments;
}
