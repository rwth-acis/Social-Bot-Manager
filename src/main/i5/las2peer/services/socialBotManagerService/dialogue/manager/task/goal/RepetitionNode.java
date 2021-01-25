package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.model.Slot;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class RepetitionNode extends Node implements Slotable {

	private Slot slot;
	private List<Node> valueChildren;
	boolean confirmed;
	boolean open;
	boolean fixed;

	public RepetitionNode(Slot slot) {

		assert slot != null : "slot parameter is null";
		assert slot.hasChildren() : "slot has no children";

		this.slot = slot;
		this.confirmed = false;
		this.open = true;
		this.valueChildren = new ArrayList<Node>();

		System.out.println("Repetition Node: " + slot.getName() + ", enumlist: " + (this.slot.hasEnumList() && !this.slot.getEnumList().isEmpty()));
		if (this.slot.hasEnumList() && !this.slot.getEnumList().isEmpty())
			extendEnums();
		else
			extend();

		invariant();
	}

	private void extendEnums() {
		invariant();
		assert this.slot.hasEnumList();

		System.out.println("Repetition Node " + this.getName() + " with enum list detected");
		String key = this.slot.getParameter().getContentFill();
		assert key != null;
		for (String value : this.slot.getEnumList()) {
			extend();
			Node node = this.valueChildren.get(this.valueChildren.size() - 1);
			NodeList nodes = node.getAll();
			Slotable targetNode = nodes.getByName(key);
			if (targetNode != null && targetNode instanceof Fillable) {
				((Fillable) targetNode).fill(value);
				this.fixed = true;
			} else
				System.out.println("target node " + key + " not found");
		}

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

		this.open = true;
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
		this.open = true;

	}

	public void close() {
		invariant();
		this.open = false;
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
		
		if (!this.getSlot().isRequired()) {
			System.out.println(this.getName() + " ready cause not required");
			return true; 
			
		}
		
		if (this.open == true) {
			System.out.println(this.getName() + " not ready cause still open");
			return false;
			
		}

		if (!this.isFilled()) {
			System.out.println(this.getName() + " not ready cause not filled yet");
			return false;
		}

		for (Node node : this.valueChildren) {
			if (!node.isReady()) {
				if(node instanceof Fillable)
					System.out.println(this.getName() + " not ready cause child " + ((Fillable) node).getName()+ " not ready yet");
				else 
					System.out.println(this.getName() + " not ready cause child not ready yet");
				return false;
			}
		}

		System.out.println("node " + this.getName() + "ready");
		return true;
	}

	@Override
	public boolean isFull() {
		invariant();
		if (this.open == true) {			
			System.out.println(this.getName() +  " not full because open");
			return false;
		}

		if (!this.isFilled()) {
			System.out.println(this.getName() + " not full because not filled");
			return false;
		}

		for (Node node : this.valueChildren) {
			if (!node.isFull()) {
				if(node instanceof Fillable)
					System.out.println(this.getName() +  "not full not full child " + ((Fillable) node).getName());
				else
					System.out.println(this.getName() +  "not full not full child");
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

	@Override
	public Slot getSlot() {
		return slot;
	}

	public void setSlot(Slot slot) {
		this.slot = slot;
	}

	@Override
	public String getName() {
		invariant();
		return this.slot.getName();
	}

	@Override
	public Node next() {
		invariant();

		for (Node node : this.valueChildren) {
			if (!node.isReady()) {
				return node.next();
			}			
		}

		return this;

	}

	@Override
	public NodeList getAll() {
		NodeList nodes = new NodeList(this);
		nodes.addAll(this.valueChildren.get(this.valueChildren.size() - 1).getAll());
		return nodes;
	}
	
	public int size() {
		return this.valueChildren.size();
	}

	@Override
	protected void invariant() {
		assert this.slot != null : "slot is null";
		assert this.valueChildren != null : "valueChildren are null";
		for (Node node : this.valueChildren) {
			node.invariant();
		}
	}

	@Override
	public JSONObject toBodyJSON() {
		invariant();

		JSONObject res = new JSONObject();
		JSONArray ar = new JSONArray();
		for (Node node : this.valueChildren) {
			JSONObject jsonNode = node.toBodyJSON();
			if (jsonNode != null && !jsonNode.isEmpty())
				ar.add(node.toBodyJSON());
		}
		if (!ar.isEmpty())
			res.put(this.getAPIName(), ar);
		
		return res;
	}

}