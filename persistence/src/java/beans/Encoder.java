/*
 * @(#)Encoder.java	1.3 00/11/15
 *
 * Copyright 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package java.beans;

import java.io.*; 
import java.util.*; 


/**
 * An <code>Encoder</code> is a class which can be used to create 
 * files or streams that encode the state of a collection of 
 * JavaBeans in terms of their public APIs. The <code>Encoder</code>, 
 * in conjunction with its persistence delegates, is responsible for 
 * breaking the object graph down into a series of <code>Statements</code>s 
 * and <code>Expression</code>s which can be used to create it. 
 * A subclass typically provides a syntax for these expressions 
 * using some human readable form - like Java source code or XML. 
 * 
 * @since 1.4
 *
 * @version 1.3 11/15/00
 * @author Philip Milne
 */
        
public class Encoder { 
    private HashMap bindings = new IdentityHashtable(); 
    private ExceptionListener exceptionListener; 
    boolean executeStatements = true; 

    /** 
     * Write the specified object to the output stream. 
     * The serialized form will denote a series of 
     * expressions, the combined effect of which will create 
     * an equivalent object when the input stream is read. 
     * By default, the object is assumed to be a <em>JavaBean</em> 
     * with a nullary constructor, whose state is defined by 
     * the matching pairs of "setter" and "getter" methods 
     * returned by the Introspector. 
     *
     * @param o The object to be written to the stream. 
     * 
     * @see XMLDecoder#readObject
     */ 
    protected void writeObject(Object o) { 
    	// System.out.println("Encoder::writeObject: " + NameGenerator.instanceName(o));
    	if (o == this) { 
    	    return; 
        }
        PersistenceDelegate info = getPersistenceDelegate(o == null ? null : o.getClass()); 
        info.writeObject(o, this); 
    } 
    
    /** 
     * Sets the exception handler for this stream to <code>exceptionListener</code>. 
     * The exception handler is notified when this stream catches recoverable 
     * exceptions.
     * 
     * @param exceptionListener The exception handler for this stream. 
     *
     * @see #getExceptionListener
     */ 
    public void setExceptionListener(ExceptionListener exceptionListener) { 
        this.exceptionListener = exceptionListener; 
    } 
    
    /**
     * Gets the exception handler for this stream. 
     * 
     * @return The exception handler for this stream. 
     *
     * @see #setExceptionListener
     */ 
    public ExceptionListener getExceptionListener() { 
        return (exceptionListener != null) ? exceptionListener : Statement.defaultExceptionListener;
    } 
    
    Object getValue(Expression exp) { 
        try { 
            return (exp == null) ? null : exp.getValue(); 
        }
        catch (Exception e) { 
            getExceptionListener().exceptionThrown(e); 
            throw new RuntimeException("failed to evaluate: " + exp.toString()); 
        }
    }
        
    void execute(Statement smt) { 
        try { 
            smt.execute(); 
        }
        catch (Exception e) { 
            getExceptionListener().exceptionThrown(new Exception("discarding statement " + smt)); 
        }
    }
        
    /**
     * Returns the persistence delegate for the given type. 
     * The persistence delegate is calculated 
     * by applying the following of rules in order:   
     * <ul>
     * <li>
     * If the type is an array, an internal persistence 
     * delegate is returned which will instantiate an 
     * array of the appropriate type and length, initializing  
     * each of its elements as if they are properties. 
     * <li>
     * If the type is a proxy, an internal persistence 
     * delegate is returned which will instantiate a 
     * new proxy instance using the static 
     * "newProxyInstance" method defined in the 
     * Proxy class. 
     * <li>
     * If the BeanInfo for this type has a <code>BeanDescriptor</code> 
     * which defined a "persistenceDelegate" property, this 
     * value is returned. 
     * <li>
     * In all other cases the default persistence delegate 
     * is returned. The default persistence delegate assumes 
     * the type is a <em>JavaBean</em>, implying that it has a nullary constructor 
     * and that its state may be characterized by the matching pairs 
     * of "setter" and "getter" methods returned by the Introspector. 
     * </ul>
     * 
     * @param  type The type of the object. 
     * @return The persistence delegate for this type of object. 
     *
     * @see #setPersistenceDelegate
     * @see java.beans.Introspector#getBeanInfo
     * @see java.beans.BeanInfo#getBeanDescriptor
     */    
    public PersistenceDelegate getPersistenceDelegate(Class type) { 
        return MetaData.getPersistenceDelegate(type); 
    } 
    
    /**
     * Sets the "persistenceDelegate" property of the <code>BeanDescriptor</code> 
     * associated with <code>type</code> to <code>persistenceDelegate</code>. 
     * 
     * @param  type The class of objects that <code>persistenceDelegate</code> applies to. 
     * @param  persistenceDelegate The persistence delegate for instances of <code>type</code>. 
     * 
     * @see #getPersistenceDelegate
     * @see java.beans.Introspector#getBeanInfo
     * @see java.beans.BeanInfo#getBeanDescriptor
     */
    public void setPersistenceDelegate(Class type, PersistenceDelegate persistenceDelegate) {
        MetaData.setPersistenceDelegate(type, persistenceDelegate); 
    } 
    
    /**
     * Removes the entry for this instance, returning the old entry.  
     * 
     * @param oldInstance The entry that should be removed. 
     * @return The entry that was removed. 
     *
     * @see #get 
     */
    public Object remove(Object oldInstance) { 
        Expression exp = (Expression)bindings.remove(oldInstance); 
        return getValue(exp); 
    } 
    
    /**
     * Returns a tentative value for <code>oldInstance</code> in 
     * the environment created by this stream. A persistence 
     * delegate can use its <code>mutatesTo</code> method to 
     * determine whether this value may be initialized to 
     * form the equivalent object at the output or whether 
     * a new object must be instantiated afresh. If the 
     * stream has not yet seen this value, null is returned.  
     * 
     * @param  oldInstance The instance to be looked up. 
     * @return The object, null if the object has not been seen before. 
     */
    public Object get(Object oldInstance) { 
        if (oldInstance == null || oldInstance == this || oldInstance.getClass() == String.class) { 
            return oldInstance; 
        }
        Expression exp = (Expression)bindings.get(oldInstance); 
        return getValue(exp); 
    } 
    
    private Object writeObject1(Object oldInstance) { 
        Object o = get(oldInstance); 
        if (o == null) {  
            writeObject(oldInstance);   
            o = get(oldInstance);
        }        
        return o; 
    } 
    
    private Statement cloneStatement(Statement oldExp) { 
        Object oldTarget = oldExp.getTarget(); 
        Object newTarget = writeObject1(oldTarget); 
                
        Object[] oldArgs = oldExp.getArguments(); 
        Object[] newArgs = new Object[oldArgs.length]; 
        for (int i = 0; i < oldArgs.length; i++) { 
            newArgs[i] = writeObject1(oldArgs[i]);
        }
        if (oldExp.getClass() == Statement.class) { 
            return new Statement(newTarget, oldExp.getMethodName(), newArgs); 
        }
        else { 
            return new Expression(newTarget, oldExp.getMethodName(), newArgs); 
        }
    } 
    
    /**
     * Writes statement <code>oldStm</code> to the stream. 
     * The <code>oldStm</code> should be written entirely 
     * in terms of the callers environment, i.e. the 
     * target and all arguments should be part of the 
     * object graph being written. These expressions 
     * represent a series of "what happened" expressions 
     * which tell the output stream how to produce an 
     * object graph like the original. 
     * <p>
     * The implementation of this method will produce 
     * a second expression to represent the same expression in 
     * an environment that will exist when the stream is read. 
     * This is achieved simply by calling <code>writeObject</code> 
     * on the target and all the arguments and building a new 
     * expression with the results. 
     * 
     * @param oldExp The expression to be written to the stream.
     */
    public void writeStatement(Statement oldStm) { 
        // System.out.println("writeStatement: " + oldExp); 
        Statement newStm = cloneStatement(oldStm); 
        if (oldStm.getTarget() != this && executeStatements) { 
            execute(newStm); 
        }
    } 
    
    /**
     * The implementation first checks to see if an 
     * expression with this value has already been written. 
     * If not, the expression is cloned, using 
     * the same procedure as <code>writeStatement</code>, 
     * and the value of this expression is reconciled 
     * with the value of the cloned expression   
     * by calling <code>writeObject</code>. 
     * 
     * @param oldExp The expression to be written to the stream.
     */
    public void writeExpression(Expression oldExp) { 
        // System.out.println("Encoder::writeExpression: " + oldExp); 
        Object oldValue = getValue(oldExp); 
        if (get(oldValue) != null) {  
            return; 
        }
        bindings.put(oldValue, (Expression)cloneStatement(oldExp)); 
        writeObject(oldValue); 
    } 
    
    void clear() { 
        bindings.clear(); 
    } 
}













































