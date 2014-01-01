package it.unisa.dia.gas.plaf.jpbc.field.base;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.FieldOver;

import java.util.Random;

/**
 * @author Angelo De Caro (angelo.decaro@gmail.com)
 */
public abstract class AbstractFieldOver<F extends Field, E extends Element> extends AbstractField<E> implements FieldOver<F, E> {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4740947187372749207L;
	protected F targetField;


    protected AbstractFieldOver(Random random, F targetField) {
        super(random);
        this.targetField = targetField;
    }


    public AbstractFieldOver() {
		super();
	}


	public F getTargetField() {
        return targetField;
    }

}
