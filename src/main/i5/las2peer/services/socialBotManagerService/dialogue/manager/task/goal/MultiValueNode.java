package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.model.Slot;

public class MultiValueNode extends Node implements Fillable {

    private Slot slot;
    private List<String> values;
    private boolean confirmed;

    public MultiValueNode(Slot slot) {

	assert slot != null : "slot is null";

	this.slot = slot;
	this.values = new ArrayList<String>();
	this.confirmed = false;

	invariant();
    }

    @Override
    public void fill(String value) {

	assert value != null : "value parameter is null";
	invariant();
	assert this.slot.validate(value) : "value '" + value + "' is not valid for slot " + this.slot.getName();

	this.values.add(value);

    }

    @Override
    public boolean validate(String value) {

	assert value != null : "value parameter is null";
	invariant();

	return this.slot.validate(value);
    }

    @Override
    public void confirm() {

	invariant();
	assert this.isFilled() : "node value not filled yet";

	this.confirmed = true;

    }

    @Override
    public void clear() {
	invariant();

	this.values = new ArrayList<String>();
	this.confirmed = false;

    }

    @Override
    public boolean isFilled() {
	invariant();
	return !this.values.isEmpty();
    }

    @Override
    public boolean isReady() {
	invariant();
	return this.isFilled();
    }

    @Override
    public boolean isConfirmed() {
	invariant();
	return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
	this.confirmed = confirmed;
    }

    @Override
    public Slot getSlot() {
	return slot;
    }

    public void setSlot(Slot slot) {
	this.slot = slot;
    }

    public List<String> getValues() {
	return values;
    }

    public void setValue(List<String> values) {
	this.values = values;
    }

    @Override
    public String getValue() {
	String res = "";
	res = res.concat(this.getValues().get(0));
	if (this.getValues().size() > 0) {
	    for (String value : this.values.subList(1, this.values.size())) {
		res = res.concat(", ").concat(value);
	    }
	}
	return res;

    }

    @Override
    public NodeList getAll() {
	return new NodeList(this);
    }

    @Override
    public void invariant() {
	assert this.slot != null : "slot of value node is null";
	assert this.values != null : "values list is null";
	if (this.isFilled())
	    for (String value : this.values) {
		assert this.slot.validate(value) : "slot " + this.slot.getName() + " filled with invalid value "
			+ value;
	    }
    }

}
