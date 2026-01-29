package com.vfu.backend;

import com.vfu.backend.retrieval.PolicyService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    private final PolicyService policyService;

    public BackendApplication(PolicyService policyService) {
        this.policyService = policyService;
    }

//    @Override
//    public void run(String... args) throws Exception {
//        List<String> policies = List.of(
//                "No pets allowed",
//                "No smoking inside the unit",
//                "Quiet hours after 10 PM"
//        );
//
//        policyService.preloadPolicies(policies);
//
//        System.out.println("Top policies for 'Can I bring my dog?':");
//        System.out.println(policyService.getTopKRelevantPolicies("Can I bring my dog?"));
//    }

}
