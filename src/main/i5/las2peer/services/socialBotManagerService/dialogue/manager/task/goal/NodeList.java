package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.model.Slot;

public class NodeList extends ArrayList<Node> {

	private static final long serialVersionUID = 1L;

	public NodeList() {

	}

	public NodeList(Node node) {
		this.add(node);
	}

	/**
	 * Get nodes that are fillable
	 * 
	 * @return list of nodes that can be filled with a value
	 */
	List<Fillable> getFillableNodes() {

		List<Fillable> res = new ArrayList<Fillable>();
		for (Node node : this) {
			if (node instanceof Fillable)
				res.add((Fillable) node);
		}

		return res;
	}

	/**
	 * Get nodes with filled values
	 * 
	 * @return list of nodes that have a filled value (non-empty nodes)
	 */
	public List<Fillable> getFilledNode() {

		List<Fillable> res = new ArrayList<Fillable>();
		for (Node node : this) {
			if (node instanceof Fillable)
				if (((Fillable) node).isFilled())
					res.add((Fillable) node);
		}

		return res;
	}

	/**
	 * Returns if list contains a node
	 * 
	 * @param name of the slot of the node
	 * @return list contains a node (true) or not (false)
	 */
	public boolean contains(String name) {

		return this.get(name) != null;
	}

	public Slotable get(String name) {

		// by name
		Slotable res = this.getByName(name);
		if (res != null)
			return res;

		// by intent
		res = this.getByIntent(name);
		if (res != null)
			return res;

		// by id
		res = this.getById(name);
		if (res != null)
			return res;

		return null;

	}

	public Slotable getByIntent(String name) {
		assert name != null : "name is null";

		for (Node node : this) {
			if (node instanceof Slotable && node.getSlot() != null) {
				Slot slot = ((Slotable) node).getSlot();
				assert slot != null;
				assert slot.getAPIName() != null;
				assert slot.getName() != null;
				assert slot.getIntents() != null;
				if (slot.getIntents().contains(name))
					return (node);
			}
		}
		return null;
	}

	public Slotable getByName(String name) {
		assert name != null : "name is null";

		for (Node node : this) {
			if (node instanceof Slotable && node.getSlot() != null) {
				Slot slot = ((Slotable) node).getSlot();
				assert slot != null;
				assert slot.getAPIName() != null;
				assert slot.getName() != null;
				if (name.equalsIgnoreCase(slot.getAPIName()) || name.equalsIgnoreCase(slot.getName()))
					return (node);
			}
		}
		return null;
	}

	public Slotable getById(String name) {
		assert name != null : "name is null";

		for (Node node : this) {
			if (node instanceof Slotable && node.getSlot() != null) {
				Slot slot = ((Slotable) node).getSlot();
				assert slot != null;
				assert slot.getAPIName() != null;
				assert slot.getName() != null;
				if (name.contentEquals(slot.getID()))
					return (node);
				if (slot.hasParameter() && name.contentEquals(slot.getParameter().getIdName()))
					return (node);
			}
		}
		return null;
	}

	public Collection<Fillable> getByEntity(String entityName) {

		Collection<Fillable> res = new ArrayList<>();
		for (Node node : this) {
			if (node instanceof Fillable) {
				Fillable fillable = (Fillable) node;
				if (fillable.hasEntity(entityName))
					res.add(fillable);
			}
		}

		return res;
	}

	/**
	 * Get list of intents
	 * 
	 * @return list of intents of nodes
	 */
	public Collection<String> getIntents() {

		Collection<String> intents = new ArrayList<String>();
		for (Fillable node : getFillableNodes()) {
			Slot slot = node.getSlot();
			intents.addAll(slot.getIntents());
		}

		return intents;
	}

	public NodeList Fillables() {

		NodeList res = new NodeList();
		for (Node node : this) {
			if (node instanceof Fillable)
				res.add(node);
		}
		return res;

	}

	public List<Slotable> Slotable() {

		List<Slotable> res = new ArrayList<>();
		for (Node node : this) {
			if (node instanceof Slotable)
				res.add(node);
		}
		return res;

	}

	public void print() {
		for (Node node : this) {
			if (node instanceof Fillable)
				System.out.println(node.getClass() + ": " + ((Fillable) node).getSlot().getName());
			else if (node instanceof RepetitionNode)
				System.out.println(node.getClass() + ": " + ((RepetitionNode) node).getSlot().getName());
			else
				System.out.println(node.getClass());
		}
	}

}
