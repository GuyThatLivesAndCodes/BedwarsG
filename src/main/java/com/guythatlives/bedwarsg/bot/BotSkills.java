package com.guythatlives.bedwarsg.bot;

/**
 * Represents the skill levels for a bot at a specific difficulty
 */
public class BotSkills {
    private final double accuracy;
    private final double blockPlacingSpeed;
    private final double pvpSkill;
    private final double decisionSpeed;
    private final double teamwork;

    public BotSkills(double accuracy, double blockPlacingSpeed, double pvpSkill,
                     double decisionSpeed, double teamwork) {
        this.accuracy = Math.max(0.0, Math.min(1.0, accuracy));
        this.blockPlacingSpeed = Math.max(0.0, Math.min(1.0, blockPlacingSpeed));
        this.pvpSkill = Math.max(0.0, Math.min(1.0, pvpSkill));
        this.decisionSpeed = Math.max(0.0, Math.min(1.0, decisionSpeed));
        this.teamwork = Math.max(0.0, Math.min(1.0, teamwork));
    }

    /**
     * Accuracy affects how well bots aim in combat (0.0 - 1.0)
     */
    public double getAccuracy() {
        return accuracy;
    }

    /**
     * Block placing speed affects how fast bots build (0.0 - 1.0)
     */
    public double getBlockPlacingSpeed() {
        return blockPlacingSpeed;
    }

    /**
     * PvP skill affects combat effectiveness (0.0 - 1.0)
     */
    public double getPvpSkill() {
        return pvpSkill;
    }

    /**
     * Decision speed affects how quickly bots make tactical decisions (0.0 - 1.0)
     */
    public double getDecisionSpeed() {
        return decisionSpeed;
    }

    /**
     * Teamwork affects coordination with teammates (0.0 - 1.0)
     */
    public double getTeamwork() {
        return teamwork;
    }
}
