package com.teknisio.mobile.model.request;

import java.math.BigDecimal;

public class CompleteServiceRequestRequest {
    public BigDecimal finalCost;
    public String technicianNote;

    public CompleteServiceRequestRequest(BigDecimal finalCost, String technicianNote) {
        this.finalCost = finalCost;
        this.technicianNote = technicianNote;
    }
}
