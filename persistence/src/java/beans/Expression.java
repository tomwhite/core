/*
 * @(#)Expression.java	1.3 00/11/15
 *
 * Copyright 1997, 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 */

package java.beans; 

import java.lang.reflect.*; 

/**
 * An <code>Expression</code> object represents a primitive expression 
 * in which a single method is applied to a target and a set of 
 * arguments to return a result - as in <code>"a.getFoo()"</code>. 
 * <p> 
 * In addition to the properties of the super class, the 
 * <code>Expression</code> object provides a <em>value</em> which 
 * is the object returned when this expression is evaluated. 
 * The return value is typically not provided by the caller and 
 * is instead computed by dynamically finding the method and invoking 
 * it when the first call to <code>getValue</code> is made. 
 * 
 * @see #getValue
 * @see #setValue
 *
 * @since 1.4
 * 
 * @version 1.3 11/15/00
 * @author Philip Milne
 */

public class Expression extends Statement { 

    private static Object unbound = new Object(); 
    
    private Object value = unbound; 
    
    /**
     * Creates a new <code>Statement</code> object with a <code>target</code>, 
     * <code>methodName</code> and <code>arguments</code> as per the parameters. 
     *
     * @param target The target of this expression. 
     * @param methodName The methodName of this expression. 
     * @param arguments The arguments of this expression. 
     *     
     * @see #getValue
     */
    public Expression(Object target, String methodName, Object[] arguments) { 
        super(target, methodName, arguments); 
    } 
    
    /**
     * Creates a new <code>Expression</code> object for a method 
     * that returns a result. The result will never be calculated 
     * however, since this constructor uses the <code>value</code>  
     * parameter to set the value property by calling the 
     * <code>setValue</code> method.
     *
     * @param value The value of this expression. 
     * @param target The target of this expression. 
     * @param methodName The methodName of this expression. 
     * @param arguments The arguments of this expression. 
     *
     * @see #setValue
     */
    public Expression(Object value, Object target, String methodName, Object[] arguments) { 
        this(target, methodName, arguments); 
        setValue(value); 
    } 
    
    /**
     * If the value property of this instance is not already set, 
     * this method dynamically finds the method with the specified 
     * methodName on this target with these arguments and calls it. 
     * The result of the method invocation is first copied 
     * into the value property of this expression and then returned 
     * as the result of <code>getValue</code>. If the value property 
     * was already set, either by a call to <code>setValue</code> 
     * or a previous call to <code>getValue</code> then the value 
     * property is returned without either looking up or calling the method.
     * <p>
     * The value property of an <code>Expression</code> is set to 
     * a unique private (non-<code>null</code>) value by default and 
     * this value is used as an internal indication that the method 
     * has not yet been called. A return value of <code>null</code> 
     * replaces this default value in the same way that any other value 
     * would, ensuring that expressions are never evaluated more than once. 
     * <p>
     * See the <code>excecute<code> method for details on how 
     * methods are chosen using the dynamic types of the target 
     * and arguments. 
     * 
     * @see Statement#execute
     * @see #setValue
     * 
     * @return The result of applying this method to these arguments.  
     */
    public Object getValue() throws Exception { 
        if (value == unbound) { 
            setValue(invoke());
        }
        return value;
    }
    
    /**
     * Sets the value of this expression to <code>value</code>. 
     * This value will be returned by the getValue method 
     * without calling the method associated with this 
     * expression. 
     * 
     * @param value The value of this expression. 
     * 
     * @see #getValue
     */
    public void setValue(Object value) { 
        this.value = value;
    } 
    
    private String instanceName(Object instance) { 
        return instance == unbound ? "<unbound>" : NameGenerator.instanceName(instance); 
    } 
    
    /**
     * Prints the value of this expression using a Java-style syntax. 
     */
    public String toString() { 
        return instanceName(value) + "=" + super.toString(); 
    } 
}














































