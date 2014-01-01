package it.unisa.dia.gas.plaf.jpbc.field.poly;

import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.plaf.jpbc.field.base.AbstractFieldOver;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @author Angelo De Caro (angelo.decaro@gmail.com)
 */
public class PolyField<F extends Field> extends AbstractFieldOver<F, PolyElement> {


    /**
	 * 
	 */
	private static final long serialVersionUID = -4292073013961601576L;
	
	public PolyField(){}

	public PolyField(Random random, Field targetField) {
        super(random, (F) targetField);
    }

    public PolyField(F targetField) {
        super(new SecureRandom(), targetField);
    }


    public PolyElement newElement() {
        return new PolyElement(this);
    }

    public BigInteger getOrder() {
        throw new IllegalStateException("Not Implemented yet!!!");
    }

    public PolyElement getNqr() {
        throw new IllegalStateException("Not Implemented yet!!!");
    }

    public int getLengthInBytes() {
        throw new IllegalStateException("Not Implemented yet!!!");
    }

}
