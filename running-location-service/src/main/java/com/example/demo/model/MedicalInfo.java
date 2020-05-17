package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MedicalInfo {
    private long bfr;
    private long fmi;

    public MedicalInfo() {
    }

    public MedicalInfo(long bfr, long fmi) {
        this.bfr = bfr;
        this.fmi = fmi;
    }
}
