package com.berryfi.portal.controller;

import com.berryfi.portal.dto.billing.*;
import com.berryfi.portal.enums.*;
import com.berryfi.portal.service.BillingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for billing operations.
 */
@RestController
@RequestMapping("/api/billing")
@CrossOrigin(origins = "*")
@Tag(name = "Billing", description = "Billing and payment operations")
public class BillingController {

    @Autowired
    private BillingService billingService;

    /**
     * Get billing balance for organization
     * GET /billing/balance?organizationId=xxx
     */
    @GetMapping("/balance")
    public ResponseEntity<BillingBalanceDto> getBillingBalance(
            @RequestParam String organizationId) {
        try {
            BillingBalanceDto balance = billingService.getBillingBalance(organizationId);
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get billing transactions for organization
     * GET /billing/transactions?organizationId=xxx&type=xxx&startDate=xxx&endDate=xxx&page=0&size=20
     */
    @GetMapping("/transactions")
    public ResponseEntity<Page<BillingTransactionDto>> getBillingTransactions(
            @RequestParam String organizationId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<BillingTransactionDto> transactions = billingService.getBillingTransactions(
                    organizationId, type, startDate, endDate, page, size);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Add credits to organization
     * POST /billing/credits
     */
    @PostMapping("/credits")
    public ResponseEntity<BillingTransactionDto> addCredits(
            @RequestBody AddCreditsRequest request) {
        try {
            BillingTransactionDto transaction = billingService.addCredits(
                    request.getOrganizationId(),
                    request.getAmount(),
                    request.getDescription());
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Record usage transaction
     * POST /billing/usage
     */
    @PostMapping("/usage")
    public ResponseEntity<BillingTransactionDto> recordUsage(
            @RequestBody RecordUsageRequest request) {
        try {
            BillingTransactionDto transaction = billingService.recordUsage(
                    request.getOrganizationId(),
                    request.getAmount(),
                    request.getDescription());
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Process INR recharge and convert to credits (Admin function for Step 3)
     * POST /billing/recharge/inr
     */
    @PostMapping("/recharge/inr")
    public ResponseEntity<BillingTransactionDto> processINRRecharge(
            @RequestBody INRRechargeRequest request) {
        try {
            BillingTransactionDto transaction = billingService.processINRRecharge(
                    request.getOrganizationId(),
                    request.getInrAmount(),
                    request.getAdminName(),
                    request.getDescription());
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Record VM usage transaction (For Step 4 - automatic VM billing)
     * POST /billing/usage/vm
     */
    @PostMapping("/usage/vm")
    public ResponseEntity<BillingTransactionDto> recordVmUsage(
            @RequestBody VmUsageRequest request) {
        try {
            BillingTransactionDto transaction = billingService.recordVmUsage(
                    request.getOrganizationId(),
                    request.getVmType(),
                    request.getDurationInSeconds(),
                    request.getSessionId());
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all subscription plans
     * GET /billing/plans
     */
    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlanDto>> getSubscriptionPlans() {
        try {
            List<SubscriptionPlanDto> plans = billingService.getSubscriptionPlans();
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get subscription plan by ID
     * GET /billing/plans/{planId}
     */
    @GetMapping("/plans/{planId}")
    public ResponseEntity<SubscriptionPlanDto> getSubscriptionPlan(
            @PathVariable String planId) {
        try {
            Optional<SubscriptionPlanDto> plan = billingService.getSubscriptionPlan(planId);
            return plan.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get billing invoices for organization
     * GET /billing/invoices?organizationId=xxx&status=xxx&startDate=xxx&endDate=xxx&page=0&size=20
     */
    @GetMapping("/invoices")
    public ResponseEntity<Page<BillingInvoiceDto>> getBillingInvoices(
            @RequestParam String organizationId,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<BillingInvoiceDto> invoices = billingService.getBillingInvoices(
                    organizationId, status, startDate, endDate, page, size);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get invoice by ID
     * GET /billing/invoices/{invoiceId}
     */
    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<BillingInvoiceDto> getInvoice(
            @PathVariable String invoiceId) {
        try {
            Optional<BillingInvoiceDto> invoice = billingService.getInvoice(invoiceId);
            return invoice.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new invoice
     * POST /billing/invoices
     */
    @PostMapping("/invoices")
    public ResponseEntity<BillingInvoiceDto> createInvoice(
            @RequestBody CreateInvoiceRequest request) {
        try {
            BillingInvoiceDto invoice = billingService.createInvoice(
                    request.getOrganizationId(),
                    request.getAmount(),
                    request.getDescription());
            return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark invoice as paid
     * PUT /billing/invoices/{invoiceId}/pay
     */
    @PutMapping("/invoices/{invoiceId}/pay")
    public ResponseEntity<BillingInvoiceDto> markInvoiceAsPaid(
            @PathVariable String invoiceId) {
        try {
            BillingInvoiceDto invoice = billingService.markInvoiceAsPaid(invoiceId);
            return ResponseEntity.ok(invoice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get outstanding balance for organization
     * GET /billing/outstanding-balance?organizationId=xxx
     */
    @GetMapping("/outstanding-balance")
    public ResponseEntity<Double> getOutstandingBalance(
            @RequestParam String organizationId) {
        try {
            Double outstanding = billingService.getOutstandingBalance(organizationId);
            return ResponseEntity.ok(outstanding);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get billing usage statistics.
     * GET /api/billing/usage
     */
    @GetMapping("/usage")
    public ResponseEntity<BillingUsageDto> getBillingUsage(
            @RequestParam String userRole,
            @RequestParam(required = false) String accountId) {
        try {
            BillingUsageDto usage = billingService.getBillingUsage(userRole, accountId);
            return ResponseEntity.ok(usage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get billing overview.
     * GET /api/billing/overview
     */
    @GetMapping("/overview")
    public ResponseEntity<BillingOverviewDto> getBillingOverview(
            @RequestParam String userRole,
            @RequestParam(required = false) String accountId) {
        try {
            BillingOverviewDto overview = billingService.getBillingOverview(userRole, accountId);
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get payment methods.
     * GET /api/billing/payment-methods
     */
    @GetMapping("/payment-methods")
    public ResponseEntity<List<PaymentMethodDto>> getPaymentMethods(
            @RequestParam String userRole,
            @RequestParam(required = false) String accountId) {
        try {
            List<PaymentMethodDto> paymentMethods = billingService.getPaymentMethods(userRole, accountId);
            return ResponseEntity.ok(paymentMethods);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Request DTOs for POST endpoints
    public static class AddCreditsRequest {
        private String organizationId;
        private Double amount;
        private String description;

        public String getOrganizationId() { return organizationId; }
        public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class RecordUsageRequest {
        private String organizationId;
        private Double amount;
        private String description;

        public String getOrganizationId() { return organizationId; }
        public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class CreateInvoiceRequest {
        private String organizationId;
        private Double amount;
        private String description;

        public String getOrganizationId() { return organizationId; }
        public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class INRRechargeRequest {
        private String organizationId;
        private Double inrAmount;
        private String adminName;
        private String description;

        public String getOrganizationId() { return organizationId; }
        public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
        public Double getInrAmount() { return inrAmount; }
        public void setInrAmount(Double inrAmount) { this.inrAmount = inrAmount; }
        public String getAdminName() { return adminName; }
        public void setAdminName(String adminName) { this.adminName = adminName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class VmUsageRequest {
        private String organizationId;
        private String vmType;
        private Double durationInSeconds;
        private String sessionId;

        public String getOrganizationId() { return organizationId; }
        public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
        public String getVmType() { return vmType; }
        public void setVmType(String vmType) { this.vmType = vmType; }
        public Double getDurationInSeconds() { return durationInSeconds; }
        public void setDurationInSeconds(Double durationInSeconds) { this.durationInSeconds = durationInSeconds; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }
}
