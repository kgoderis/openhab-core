package org.openhab.core.ai.reasoning;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Risk assessment result for a decision.
 *
 * <p>Contains risk score, risk level, factors, and mitigation strategies.</p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class RiskAssessmentResult {
    private double riskScore;
    private RiskLevel riskLevel;
    private List<String> riskFactors = new ArrayList<>();
    private List<String> mitigationStrategies = new ArrayList<>();

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<String> getRiskFactors() {
        return new ArrayList<>(riskFactors);
    }

    public void setRiskFactors(List<String> riskFactors) {
        this.riskFactors = new ArrayList<>(riskFactors);
    }

    public List<String> getMitigationStrategies() {
        return new ArrayList<>(mitigationStrategies);
    }

    public void setMitigationStrategies(List<String> mitigationStrategies) {
        this.mitigationStrategies = new ArrayList<>(mitigationStrategies);
    }
}


