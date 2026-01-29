package com.vfu.backend.api.controller;

import com.vfu.backend.llm.service.IEmbeddingService;
import com.vfu.backend.retrieval.PolicyService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final IEmbeddingService embeddingService;
    private final PolicyService policyService;

    public AdminController(IEmbeddingService embeddingService, PolicyService policyService) {
        this.embeddingService = embeddingService;
        this.policyService = policyService;
    }

    @PostMapping("/policies/load")
    public String loadPolicies() {
        String policies = """
                    STANDARD CANCELLATION POLICY
                    • Full refund if cancelled 14+ days before check-in
                    • 50% refund if cancelled 7-13 days before check-in
                    • No refund if cancelled within 7 days of check-in
                    • Refund processing takes 5-7 business days
                    • Cleaning fees and service fees are non-refundable
                    • Host can modify policy up to 30 days before booking
                    
                    
                    HOUSE RULES - STRICT ENFORCEMENT
                    • No smoking anywhere on property (including balcony)
                    • No pets without prior written approval (+$50 fee)
                    • Check-in: 4PM-8PM only. Late check-in requires 24hr notice
                    • Check-out: 11AM sharp. Late checkout charged $50/hr
                    • Quiet hours: 10PM-8AM. Noise complaints = immediate eviction
                    • Max 6 guests (including children). Extra guests = $25/night each
                    • No parties or events. Security cameras monitor front door/parking
                    
                    
                    GUEST RESPONSIBILITIES
                    • Report damages within 24 hours or forfeit security deposit
                    • Leave property clean (dishes washed, trash removed, linens in hamper)
                    • No unauthorized guests or visitors after 10PM
                    • Lock all doors/windows when leaving. Host not responsible for theft
                    • Internet password changes require host approval
                    • Parking: 2 vehicles max. Street parking violations towed at guest expense
                    
                    
                    HOST PROTECTION POLICY
                    • $500 security deposit held 14 days post-checkout
                    • Security cameras record front door/parking 24/7 (no audio)
                    • Guest screening: 25+ years old, verified ID required
                    • Damage claims processed through resolution center
                    • Early termination for policy violations (no refund)
                """;

        policyService.preloadPolicies(policies);
        return "Policies loaded into vector DB";
    }

//    @GetMapping("/policies/search")
//    public List<RetrievedPolicy> search(@RequestParam String q) {
//        return policyService.retrieveRelevantPolicies(q, 3);
//    }
}
