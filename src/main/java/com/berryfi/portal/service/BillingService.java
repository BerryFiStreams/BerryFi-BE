package com.berryfi.portal.service;

import com.berryfi.portal.dto.billing.*;
import com.berryfi.portal.entity.*;
import com.berryfi.portal.enums.*;
import com.berryfi.portal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing billing operations.
 */
@Service
@Transactional
public class BillingService {

    @Autowired
    private BillingTransactionRepository billingTransactionRepository;

    @Autowired
    private BillingInvoiceRepository billingInvoiceRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private PricingService pricingService;

    /**
     * Get billing balance for organization
     */
    public BillingBalanceDto getBillingBalance(String organizationId) {
        // Get current balance from latest transaction
        Optional<BillingTransaction> lastTransaction = billingTransactionRepository
                .findFirstByOrganizationIdOrderByDateDesc(organizationId);
        
        Double currentBalance = lastTransaction.map(BillingTransaction::getResultingBalance).orElse(0.0);

        // Calculate monthly spending
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);
        
        Double monthlySpend = billingTransactionRepository.sumAmountByOrganizationAndDateRange(
                organizationId, startOfMonth, endOfMonth);
        if (monthlySpend == null) monthlySpend = 0.0;

        BillingBalanceDto balance = new BillingBalanceDto(
                organizationId, currentBalance, currentBalance, 0.0, currentBalance);
        balance.setMonthlySpend(monthlySpend);
        
        if (lastTransaction.isPresent()) {
            balance.setLastTransactionAmount(lastTransaction.get().getAmount());
        }

        // Check for overage
        if (currentBalance < 0) {
            balance.setIsOverage(true);
            balance.setOverageAmount(Math.abs(currentBalance));
        }

        return balance;
    }

    /**
     * Get billing transactions for organization
     */
    public Page<BillingTransactionDto> getBillingTransactions(String organizationId, 
                                                             TransactionType type, 
                                                             LocalDateTime startDate,
                                                             LocalDateTime endDate,
                                                             int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BillingTransaction> transactions;

        if (type != null && startDate != null && endDate != null) {
            transactions = billingTransactionRepository.findByOrganizationIdAndDateRange(
                    organizationId, startDate, endDate, pageable);
        } else if (type != null) {
            transactions = billingTransactionRepository.findByOrganizationIdAndTypeOrderByDateDesc(
                    organizationId, type, pageable);
        } else if (startDate != null && endDate != null) {
            transactions = billingTransactionRepository.findByOrganizationIdAndDateRange(
                    organizationId, startDate, endDate, pageable);
        } else {
            transactions = billingTransactionRepository.findByOrganizationIdOrderByDateDesc(
                    organizationId, pageable);
        }

        return transactions.map(this::convertToTransactionDto);
    }

    /**
     * Create a new billing transaction
     */
    public BillingTransactionDto createTransaction(String organizationId, TransactionType type,
                                                  Double amount, Double resultingBalance, 
                                                  String description, String reference) {
        BillingTransaction transaction = new BillingTransaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setOrganizationId(organizationId);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setResultingBalance(resultingBalance);
        transaction.setDescription(description);
        transaction.setReference(reference);
        transaction.setDate(LocalDateTime.now());
        transaction.setStatus("completed");

        transaction = billingTransactionRepository.save(transaction);
        return convertToTransactionDto(transaction);
    }

    /**
     * Add credits to organization
     */
    public BillingTransactionDto addCredits(String organizationId, Double amount, String description) {
        // Get current balance
        Double currentBalance = getCurrentBalance(organizationId);
        Double newBalance = currentBalance + amount;
        
        return createTransaction(organizationId, TransactionType.CREDIT_ADDED, 
                               amount, newBalance, description, null);
    }

    /**
     * Process INR recharge and convert to credits (Step 3 in your process)
     * This is the main method admins will use for manual recharges
     */
    public BillingTransactionDto processINRRecharge(String organizationId, Double inrAmount, 
                                                  String adminName, String description) {
        try {
            // Calculate credits from INR amount using current conversion rate from database
            Double credits = pricingService.convertINRToCredits(inrAmount);
            Double conversionRate = pricingService.getCurrentINRPerCreditRate();
            
            // Get current balance
            Double currentBalance = getCurrentBalance(organizationId);
            Double newBalance = currentBalance + credits;
            
            // Create enhanced description for audit trail
            String enhancedDescription = String.format("Manual recharge by %s: INR %.2f converted to %.2f credits (Rate: %.2f INR/credit). %s", 
                    adminName, inrAmount, credits, conversionRate, description != null ? description : "");
            
            // Create transaction with recharge type and additional metadata
            BillingTransaction transaction = new BillingTransaction();
            transaction.setId(UUID.randomUUID().toString());
            transaction.setOrganizationId(organizationId);
            transaction.setType(TransactionType.RECHARGE);
            transaction.setAmount(credits);
            transaction.setResultingBalance(newBalance);
            transaction.setDescription(enhancedDescription);
            transaction.setProcessedBy(adminName);
            transaction.setInrAmount(inrAmount);
            transaction.setConversionRate(conversionRate);
            transaction.setDate(LocalDateTime.now());
            transaction.setStatus("completed");

            transaction = billingTransactionRepository.save(transaction);
            return convertToTransactionDto(transaction);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to process INR recharge: " + e.getMessage(), e);
        }
    }

    /**
     * Record usage transaction
     */
    public BillingTransactionDto recordUsage(String organizationId, Double amount, String description) {
        // Get current balance
        Double currentBalance = getCurrentBalance(organizationId);
        Double newBalance = currentBalance - amount;
        
        return createTransaction(organizationId, TransactionType.USAGE, 
                               -amount, newBalance, description, null);
    }

    /**
     * Record VM usage transaction based on VM type and duration (Step 4 in your process)
     * This method automatically calculates credits based on VM type and duration
     */
    public BillingTransactionDto recordVmUsage(String organizationId, String vmType, 
                                             Double durationInSeconds, String sessionId) {
        try {
            // Calculate credits for VM usage using database configuration
            Double creditsUsed = pricingService.calculateVmUsageCredits(vmType, durationInSeconds);
            Double creditsPerMinute = pricingService.getCurrentCreditsPerMinuteForVm(vmType);
            
            // Get current balance
            Double currentBalance = getCurrentBalance(organizationId);
            Double newBalance = currentBalance - creditsUsed;
            
            // Create detailed description for audit trail
            double minutes = durationInSeconds / 60.0;
            String description = String.format("VM Usage - %s: %.2f minutes (%.0f seconds) @ %.2f credits/minute = %.2f credits. Session: %s", 
                    vmType, minutes, durationInSeconds, creditsPerMinute, creditsUsed, sessionId);
            
            // Create usage transaction
            BillingTransaction transaction = new BillingTransaction();
            transaction.setId(UUID.randomUUID().toString());
            transaction.setOrganizationId(organizationId);
            transaction.setType(TransactionType.USAGE);
            transaction.setAmount(-creditsUsed);
            transaction.setResultingBalance(newBalance);
            transaction.setDescription(description);
            transaction.setReference(sessionId);
            transaction.setVmType(vmType);
            transaction.setDurationSeconds(durationInSeconds);
            transaction.setCreditsPerMinute(creditsPerMinute);
            transaction.setDate(LocalDateTime.now());
            transaction.setStatus("completed");

            transaction = billingTransactionRepository.save(transaction);
            return convertToTransactionDto(transaction);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to record VM usage: " + e.getMessage(), e);
        }
    }

    /**
     * Get current balance for organization
     */
    private Double getCurrentBalance(String organizationId) {
        Optional<BillingTransaction> lastTransaction = billingTransactionRepository
                .findFirstByOrganizationIdOrderByDateDesc(organizationId);
        return lastTransaction.map(BillingTransaction::getResultingBalance).orElse(0.0);
    }

    /**
     * Get all subscription plans
     */
    public List<SubscriptionPlanDto> getSubscriptionPlans() {
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findByActiveTrueOrderByPriceAsc();
        return plans.stream().map(this::convertToPlanDto).collect(Collectors.toList());
    }

    /**
     * Get subscription plan by ID
     */
    public Optional<SubscriptionPlanDto> getSubscriptionPlan(String planId) {
        return subscriptionPlanRepository.findById(planId)
                .map(this::convertToPlanDto);
    }

    /**
     * Get billing invoices for organization
     */
    public Page<BillingInvoiceDto> getBillingInvoices(String organizationId, 
                                                     InvoiceStatus status,
                                                     LocalDate startDate,
                                                     LocalDate endDate,
                                                     int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BillingInvoice> invoices;

        if (status != null && startDate != null && endDate != null) {
            invoices = billingInvoiceRepository.findByOrganizationIdAndDateRange(
                    organizationId, startDate, endDate, pageable);
        } else if (status != null) {
            invoices = billingInvoiceRepository.findByOrganizationIdAndStatusOrderByDateDesc(
                    organizationId, status, pageable);
        } else if (startDate != null && endDate != null) {
            invoices = billingInvoiceRepository.findByOrganizationIdAndDateRange(
                    organizationId, startDate, endDate, pageable);
        } else {
            invoices = billingInvoiceRepository.findByOrganizationIdOrderByDateDesc(
                    organizationId, pageable);
        }

        return invoices.map(this::convertToInvoiceDto);
    }

    /**
     * Get invoice by ID
     */
    public Optional<BillingInvoiceDto> getInvoice(String invoiceId) {
        return billingInvoiceRepository.findById(invoiceId)
                .map(this::convertToInvoiceDto);
    }

    /**
     * Create a new invoice
     */
    public BillingInvoiceDto createInvoice(String organizationId, Double amount, String description) {
        BillingInvoice invoice = new BillingInvoice();
        invoice.setId(UUID.randomUUID().toString());
        invoice.setOrganizationId(organizationId);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setAmount(amount);
        invoice.setDescription(description);
        invoice.setDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30)); // 30 days payment terms

        invoice = billingInvoiceRepository.save(invoice);
        return convertToInvoiceDto(invoice);
    }

    /**
     * Mark invoice as paid
     */
    public BillingInvoiceDto markInvoiceAsPaid(String invoiceId) {
        Optional<BillingInvoice> invoiceOpt = billingInvoiceRepository.findById(invoiceId);
        if (invoiceOpt.isEmpty()) {
            throw new RuntimeException("Invoice not found: " + invoiceId);
        }

        BillingInvoice invoice = invoiceOpt.get();
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidDate(LocalDate.now());

        invoice = billingInvoiceRepository.save(invoice);
        return convertToInvoiceDto(invoice);
    }

    /**
     * Get outstanding balance for organization
     */
    public Double getOutstandingBalance(String organizationId) {
        Double outstanding = billingInvoiceRepository.getOutstandingBalanceByOrganization(organizationId);
        return outstanding != null ? outstanding : 0.0;
    }

    /**
     * Convert BillingTransaction entity to DTO
     */
    private BillingTransactionDto convertToTransactionDto(BillingTransaction transaction) {
        BillingTransactionDto dto = new BillingTransactionDto();
        dto.setId(transaction.getId());
        dto.setOrganizationId(transaction.getOrganizationId());
        dto.setType(transaction.getType());
        dto.setAmount(transaction.getAmount());
        dto.setDescription(transaction.getDescription());
        dto.setReference(transaction.getReference());
        dto.setTransactionDate(transaction.getDate());
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }

    /**
     * Convert SubscriptionPlan entity to DTO
     */
    private SubscriptionPlanDto convertToPlanDto(SubscriptionPlan plan) {
        SubscriptionPlanDto dto = new SubscriptionPlanDto();
        dto.setId(plan.getId());
        dto.setName(plan.getName());
        dto.setPrice(plan.getPrice());
        dto.setCreditsIncluded(plan.getCredits());
        dto.setMaxUsers(plan.getMaxCCU()); // Using maxCCU as maxUsers
        dto.setIsActive(plan.getActive());
        dto.setIsPopular(plan.getIsPopular());
        dto.setCreatedAt(plan.getCreatedAt());
        return dto;
    }

    /**
     * Get billing usage information.
     */
    public BillingUsageDto getBillingUsage(String userRole, String accountId) {
        BillingUsageDto usage = new BillingUsageDto();
        
        // Mock implementation for now
        usage.setCurrentMonth(850.0);
        usage.setPreviousMonth(720.0);
        usage.setProjected(950.0);
        usage.setSpendingChange(18.1); // Percentage increase
        
        return usage;
    }

    /**
     * Get billing overview information.
     */
    public BillingOverviewDto getBillingOverview(String userRole, String accountId) {
        BillingOverviewDto overview = new BillingOverviewDto();
        
        // Mock implementation for now
        overview.setAccountId(accountId);
        overview.setAccountName("Main Account");
        overview.setAccountType("premium");
        overview.setStatus("active");
        overview.setCredits(7500.0);
        overview.setMonthlyBudget(1000.0);
        overview.setBillingCycle("monthly");
        overview.setNextBillingDate("2024-02-15");
        overview.setMainAccount(true);
        overview.setWorkspaceId("main-workspace");
        overview.setWorkspaceName("Main Workspace");
        
        return overview;
    }

    /**
     * Get payment methods.
     */
    public List<PaymentMethodDto> getPaymentMethods(String userRole, String accountId) {
        // Mock implementation for now
        PaymentMethodDto method1 = new PaymentMethodDto();
        method1.setId("pm_1");
        method1.setType("card");
        method1.setBrand("visa");
        method1.setLast4("4242");
        method1.setExpiryMonth(12);
        method1.setExpiryYear(2025);
        method1.setDefault(true);
        
        PaymentMethodDto method2 = new PaymentMethodDto();
        method2.setId("pm_2");
        method2.setType("bank_account");
        method2.setBrand("bank");
        method2.setLast4("6789");
        method2.setDefault(false);
        
        return List.of(method1, method2);
    }

    /**
     * Convert BillingInvoice entity to DTO
     */
    private BillingInvoiceDto convertToInvoiceDto(BillingInvoice invoice) {
        BillingInvoiceDto dto = new BillingInvoiceDto();
        dto.setId(invoice.getId());
        dto.setOrganizationId(invoice.getOrganizationId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setStatus(invoice.getStatus());
        dto.setAmount(invoice.getAmount());
        dto.setTotalAmount(invoice.getAmount()); // No separate total field in entity
        dto.setIssueDate(invoice.getDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setPaidDate(invoice.getPaidDate());
        dto.setCreatedAt(invoice.getCreatedAt());
        return dto;
    }

    /**
     * Generate unique invoice number
     */
    private String generateInvoiceNumber() {
        return "INV-" + LocalDate.now().getYear() + "-" + 
               String.format("%06d", System.currentTimeMillis() % 1000000);
    }
}
