
/**
 * A knowledge node is the basic entity that can be either a fact or a rule.
 * It contains the logic behind activation.
 * @author Michael Noseworthy
 *
 */
public class KnowledgeNodeBase {
	protected int active;
	protected int threshold;
	
	KnowledgeNodeBase(int t) {
		this.threshold = t;
		active = 0;
	}
	
	public int getThreshold() { return this.threshold; }
	public void incrementActive() { this.active += 1; }
	public void resetActive() { this.active = 0; }
	public boolean isActivated() { return this.active >= this.threshold; }
}
