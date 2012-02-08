package model;

// A specialized component class that is used to 
// represent a component split
public class DCSplitComponent extends DCComponent {
	
	public DCSplitComponent() {
		super();
		splitDirection = new DCTriple(0,0,0);
	}
   
	public DCTriple splitDirection;
}
