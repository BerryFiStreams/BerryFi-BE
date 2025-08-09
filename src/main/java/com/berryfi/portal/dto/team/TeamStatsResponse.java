package com.berryfi.portal.dto.team;

/**
 * Response DTO for team statistics.
 */
public class TeamStatsResponse {
    private boolean success = true;
    private TeamStatsData data;

    public TeamStatsResponse() {
        this.data = new TeamStatsData();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public TeamStatsData getData() {
        return data;
    }

    public void setData(TeamStatsData data) {
        this.data = data;
    }

    /**
     * Inner class for team statistics data.
     */
    public static class TeamStatsData {
        private int totalMembers;
        private int activeMembers;
        private int totalCampaigns;
        private int activeCampaigns;
        private int totalLeads;
        private int qualifiedLeads;
        private int convertedLeads;
        private double conversionRate;
        private double avgLeadsPerCampaign;
        private double avgConversionTime;

        // Getters and setters
        public int getTotalMembers() {
            return totalMembers;
        }

        public void setTotalMembers(int totalMembers) {
            this.totalMembers = totalMembers;
        }

        public int getActiveMembers() {
            return activeMembers;
        }

        public void setActiveMembers(int activeMembers) {
            this.activeMembers = activeMembers;
        }

        public int getTotalCampaigns() {
            return totalCampaigns;
        }

        public void setTotalCampaigns(int totalCampaigns) {
            this.totalCampaigns = totalCampaigns;
        }

        public int getActiveCampaigns() {
            return activeCampaigns;
        }

        public void setActiveCampaigns(int activeCampaigns) {
            this.activeCampaigns = activeCampaigns;
        }

        public int getTotalLeads() {
            return totalLeads;
        }

        public void setTotalLeads(int totalLeads) {
            this.totalLeads = totalLeads;
        }

        public int getQualifiedLeads() {
            return qualifiedLeads;
        }

        public void setQualifiedLeads(int qualifiedLeads) {
            this.qualifiedLeads = qualifiedLeads;
        }

        public int getConvertedLeads() {
            return convertedLeads;
        }

        public void setConvertedLeads(int convertedLeads) {
            this.convertedLeads = convertedLeads;
        }

        public double getConversionRate() {
            return conversionRate;
        }

        public void setConversionRate(double conversionRate) {
            this.conversionRate = conversionRate;
        }

        public double getAvgLeadsPerCampaign() {
            return avgLeadsPerCampaign;
        }

        public void setAvgLeadsPerCampaign(double avgLeadsPerCampaign) {
            this.avgLeadsPerCampaign = avgLeadsPerCampaign;
        }

        public double getAvgConversionTime() {
            return avgConversionTime;
        }

        public void setAvgConversionTime(double avgConversionTime) {
            this.avgConversionTime = avgConversionTime;
        }
    }
}
