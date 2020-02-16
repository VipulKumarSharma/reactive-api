package io.home.reactiveapi.model;

import io.home.reactiveapi.persistence.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEvent {

    private Employee employee;
    private LocalDateTime date;
}
