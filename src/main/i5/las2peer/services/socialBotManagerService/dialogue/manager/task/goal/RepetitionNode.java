package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.model.Slot;

public class RepetitionNode extends Node {

    private Slot slot;
    private List<Node> valueChildren;
    boolean confirmed;

    public RepetitionNode(Slot slot) {

	assert slot != null : "slot parameter is null";
	assert slot.hasChildren() : "slot has no children";

	this.slot = slot;
	this.confirmed = false;
	this.valueChildren = new ArrayList<Node>();
	extend();

	invariant();
    }

    public void extend() {

	invariant();
	assert this.slot.hasChildren();

	Node node = null;
	if (this.slot.getChildren().size() == 1)
	    node = NodeFactory.create(slot.getChildren().get(0));

	if (this.slot.getChildren().size() > 1)
	    node = NodeFactory.createIgnoreArray(slot);

	if (node != null)
	    this.valueChildren.add(node);

    }

    public void confirm() {
	invariant();
	assert this.isFilled() : "node value not filled yet";

	this.confirmed = true;

    }

    public void clear() {
	invariant();

	this.valueChildren = new ArrayList<Node>();
	extend();
	this.confirmed = false;

    }

    public boolean isFilled() {
	invariant();
	if (this.valueChildren.isEmpty())
	    return false;
	return true;
    }

    @Override
    public boolean isReady() {
	invariant();
	if (!this.isFilled())
	    return false;

	for (Node node : this.valueChildren) {
	    if (!node.isReady()) {
		return false;
	    }
	}

	return true;
    }

    @Override
    public boolean isFull() {
	invariant();
	if (!this.isFilled())
	    return false;

	for (Node node : this.valueChildren) {
	    if (!node.isFull()) {
		return false;
	    }
	}

	return true;
    }

    @Override
    public boolean isConfirmed() {
	return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
	this.confirmed = confirmed;
    }

    public List<Node> getValueChildren() {
	return valueChildren;
    }

    public void setValueChildren(List<Node> valueChildren) {
	this.valueChildren = valueChildren;
    }

    public Slot getSlot() {
	return slot;
    }

    public void setSlot(Slot slot) {
	this.slot = slot;
    }

    @Override
    public NodeList getAll() {
	NodeList nodes = new NodeList(this);
	nodes.addAll(this.valueChildren.get(this.valueChildren.size() - 1).getAll());
	return nodes;
    }

    @Override
    public void invariant() {
	assert this.slot != null : "slot is null";
	assert this.valueChildren != null : "valueChildren are null";
	for (Node node : this.valueChildren) {
	    node.invariant();
	}
    }

}
