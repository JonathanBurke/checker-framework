package dataflow.cfg.block;

import dataflow.analysis.Store;
import dataflow.cfg.node.Node;

/**
 * Represents a conditional basic block that contains exactly one boolean
 * {@link Node}.
 *
 * @author Stefan Heule
 *
 */
public interface ConditionalBlock extends Block {

    /**
     * @return The entry block of the then branch.
     */
    Block getThenSuccessor();

    /**
     * @return The entry block of the else branch.
     */
    Block getElseSuccessor();

    /**
     * @return The flow rule for information flowing from
     * this block to its then successor.
     */
    Store.FlowRule getThenFlowRule();

    /**
     * @return The flow rule for information flowing from
     * this block to its else successor.
     */
    Store.FlowRule getElseFlowRule();

    /**
     * Set the flow rule for information flowing from this block to
     * its then successor.
     */
    void setThenFlowRule(Store.FlowRule rule);

    /**
     * Set the flow rule for information flowing from this block to
     * its else successor.
     */
    void setElseFlowRule(Store.FlowRule rule);
}